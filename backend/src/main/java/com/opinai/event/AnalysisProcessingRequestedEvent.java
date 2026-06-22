package com.opinai.event;

import lombok.Getter;
import java.util.UUID;

@Getter
public class AnalysisProcessingRequestedEvent {
    private final UUID analysisId;

    public AnalysisProcessingRequestedEvent(UUID analysisId) {
        this.analysisId = analysisId;
    }
}
