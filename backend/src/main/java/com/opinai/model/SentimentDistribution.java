package com.opinai.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SentimentDistribution {
    private int positive;
    private int negative;
    private int neutral;
}
