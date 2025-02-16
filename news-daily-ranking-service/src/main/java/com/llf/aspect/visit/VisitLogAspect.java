package com.llf.aspect.visit;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.llf.dao.entity.VisitLogDO;
import com.llf.dao.repository.VisitLogRepository;
import com.llf.util.AddressUtil;
import com.llf.util.HttpContextUtil;
import com.llf.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

import static com.llf.util.DeviceUtil.isFromMobile;

@Slf4j
@Aspect
@Component
public class VisitLogAspect {

    @Autowired
    private VisitLogRepository visitLogRepository;

    @Pointcut("@annotation(com.llf.aspect.visit.VisitLog)")
    public void pointcut() {
        // do nothing
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取request
        HttpServletRequest request = HttpContextUtil.getHttpServletRequest();
        // 请求的类名
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getName();
        // 请求的方法名
        String methodName = signature.getName();
        String ip = IpUtil.getIpAddr(request);
        String address = AddressUtil.getAddress(ip);
        VisitLogDO visitLogDO = VisitLogDO.builder().deviceType(isFromMobile(request) ? "手机" : "电脑").method(
                className + "." + methodName + "()").ip(ip).address(AddressUtil.getAddress(address)).build();
        // 请求的方法参数值
        Object[] args = joinPoint.getArgs();
        // 请求的方法参数名称
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        if (args != null && paramNames != null) {
            // 创建 key-value 映射用于生成 JSON 字符串
            Map<String, Object> paramMap = new LinkedHashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                if (args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse) {
                    continue;
                }
                paramMap.put(paramNames[i], args[i]);
            }
            // 使用 Fastjson 将参数映射转换为 JSON 字符串
            String paramsJson = JSON.toJSONString(paramMap);
            visitLogDO.setParams(paramsJson);
        }
        long beginTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long end = System.currentTimeMillis();
        visitLogDO.setTime((int)(end - beginTime));
        visitLogRepository.save(visitLogDO);
        return proceed;
    }

    /**
     * 参数构造器¬
     *
     * @param params
     * @param args
     * @param paramNames
     * @return
     * @throws JsonProcessingException
     */
    private StringBuilder handleParams(StringBuilder params, Object[] args, List paramNames)
            throws JsonProcessingException {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Map) {
                Set set = ((Map)args[i]).keySet();
                List<Object> list = new ArrayList<>();
                List<Object> paramList = new ArrayList<>();
                for (Object key : set) {
                    list.add(((Map)args[i]).get(key));
                    paramList.add(key);
                }
                return handleParams(params, list.toArray(), paramList);
            } else {
                if (args[i] instanceof Serializable) {
                    Class<?> aClass = args[i].getClass();
                    try {
                        aClass.getDeclaredMethod("toString", new Class[] {null});
                        // 如果不抛出 NoSuchMethodException 异常则存在 toString 方法 ，安全的 writeValueAsString ，否则 走 Object的
                        // toString方法
                        params.append(" ").append(paramNames.get(i)).append(": ").append(
                                JSONObject.toJSONString(args[i]));
                    } catch (NoSuchMethodException e) {
                        params.append(" ").append(paramNames.get(i)).append(": ").append(
                                JSONObject.toJSONString(args[i].toString()));
                    }
                } else if (args[i] instanceof MultipartFile) {
                    MultipartFile file = (MultipartFile)args[i];
                    params.append(" ").append(paramNames.get(i)).append(": ").append(file.getName());
                } else {
                    params.append(" ").append(paramNames.get(i)).append(": ").append(args[i]);
                }
            }
        }
        return params;
    }
}
