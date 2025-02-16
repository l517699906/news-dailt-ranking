package com.llf.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VisitorCountDTO {

    private Integer todayPv;

    private Integer todayUv;

    private Integer allPv;

    private Integer allUv;
}
