export interface Report {
  id: string;
  projectId: string;
  name: string;
  format: 'PDF' | 'CSV';
  fileSize: number;
  status: 'GENERATING' | 'READY' | 'FAILED';
  createdAt: string;
}
