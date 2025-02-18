package com.llf.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description:指定FastJson的MessageConverter，解决内切""被转移成&quot</p>
 */
@Configuration
public class WebConfig {

    static final String ORIGINS[] = new String[] {"GET", "POST", "PUT", "DELETE", "OPTIONS"};

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        List<MediaType> list = new ArrayList<>();
        list.add(MediaType.APPLICATION_JSON);
        FastJsonHttpMessageConverter fastJsonConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        config.setCharset(StandardCharsets.UTF_8);
        //设置允许返回为null的属性
        config.setSerializerFeatures(SerializerFeature.PrettyFormat);
        fastJsonConverter.setFastJsonConfig(config);
        fastJsonConverter.setSupportedMediaTypes(list);
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setSupportedMediaTypes(list);
        //此处需要先放入字符串解析器，再放入fastjson解析器，否则字符串返回会自动带上双引号
        return new HttpMessageConverters(stringHttpMessageConverter, fastJsonConverter);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // 所有的当前站点的请求地址，都支持跨域访问。
                registry.addMapping("/**")
                        // 所有的外部域都可跨域访问。 如果是localhost则很难配置，因为在跨域请求的时候，外部域的解析可能是localhost、127.0.0.1、主机名
                        .allowedOriginPatterns("*")
                        //允许任何请求头
                        .allowedHeaders("*")
                        // 是否支持跨域用户凭证
                        .allowCredentials(true)
                        // 当前站点支持的跨域请求类型是什么
                        .allowedMethods(ORIGINS)
                        // 超时时长设置为1小时。 时间单位是秒。
                        .maxAge(3600);
            }
        };
    }
}
