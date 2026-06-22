package com.opinai.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KeyIssueDto {
    private String issue;
    private long count;
    private double percentage;
}
