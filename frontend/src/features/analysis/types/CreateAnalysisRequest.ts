export interface FeedbackItemCreateRequest {
  content: string;
}

export interface CreateAnalysisRequest {
  title: string;
  feedbackItems: FeedbackItemCreateRequest[];
}
