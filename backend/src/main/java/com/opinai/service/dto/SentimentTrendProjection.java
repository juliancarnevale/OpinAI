package com.opinai.service.dto;

import java.time.LocalDate;

public interface SentimentTrendProjection {
    LocalDate getTrendDate();
    long getPositive();
    long getNegative();
    long getNeutral();
    long getAnalysisCount();
}
