export interface FeedbackItem {
  id: string;
  content: string;
  sourceType: 'MANUAL' | 'CSV';
  createdAt: string;
}
