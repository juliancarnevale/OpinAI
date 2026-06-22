package com.opinai.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImprovementOpportunityDto {
    private String opportunity;
    private long count;
}
