package com.opinai.mapper;

import com.opinai.controller.dto.AnalysisDetailResponse;
import com.opinai.controller.dto.AnalysisResponse;
import com.opinai.controller.dto.FeedbackItemResponse;
import com.opinai.model.Analysis;
import com.opinai.model.FeedbackItem;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AnalysisMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "feedbackItemsCount", expression = "java(analysis.getFeedbackItems() != null ? analysis.getFeedbackItems().size() : 0)")
    AnalysisResponse toResponse(Analysis analysis);

    List<AnalysisResponse> toResponseList(List<Analysis> analyses);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    AnalysisDetailResponse toDetailResponse(Analysis analysis);

    FeedbackItemResponse toFeedbackItemResponse(FeedbackItem feedbackItem);

    List<FeedbackItemResponse> toFeedbackItemResponseList(List<FeedbackItem> feedbackItems);
}
