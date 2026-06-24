export interface ReportErrorFormValue {
  title: string;
  problemDescription: string;
}

export interface ReportErrorContext {
  requestURL?: string;
  requestMethod?: string;
  requestBody?: string;
  responseBody?: string;
}

export interface ReportErrorDialogData {
  initialValue?: Partial<ReportErrorFormValue>;
  context?: ReportErrorContext | null;
}
