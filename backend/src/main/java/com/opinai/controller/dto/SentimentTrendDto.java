package com.opinai.controller.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class SentimentTrendDto {
    private LocalDate date;
    private long positive;
    private long negative;
    private long neutral;
    private long analysisCount;
}
