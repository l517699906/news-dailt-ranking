package com.llf.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkylineWebcamsDTO {

    private String placeName;

    private String playUrl;
}
