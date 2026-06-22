package com.opinai.service.dto;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class ProjectReportPayload {
    String projectName;
    String projectDescription;
    List<AnalysisReportDTO> analyses;
    List<FeedbackReportDTO> feedbackItems;
}
