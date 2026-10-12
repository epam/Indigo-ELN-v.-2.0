import { useSyncExternalStore } from 'react';

export const INCIDENTS_PATH = '/api/eln/incidents';

/** The failed request a report was raised from. Absent when the user opened the dialog themselves. */
export interface ReportErrorContext {
  requestURL: string;
  requestMethod: string;
  requestBody?: string;
  responseBody?: string;
}

export interface ReportErrorDraft {
  title?: string;
  problemDescription?: string;
  context?: ReportErrorContext;
}

export interface ReportErrorFormValues {
  title: string;
  problemDescription: string;
}

function normalizeBody(body: unknown): string | undefined {
  if (body == null) return undefined;
  return typeof body === 'string' ? body : JSON.stringify(body);
}

export function buildReportErrorContext(
  requestURL: string,
  requestMethod: string,
  requestJson: unknown,
  responseBody: unknown,
): ReportErrorContext {
  return {
    requestURL,
    requestMethod,
    requestBody: normalizeBody(requestJson),
    responseBody: normalizeBody(responseBody),
  };
}

/**
 * The multipart body `IncidentAPI` takes: optional parts are left out rather than sent empty.
 */
export function buildIncidentForm(
  values: ReportErrorFormValues,
  url: string,
  experimentJson?: string,
  context?: ReportErrorContext,
): FormData {
  const form = new FormData();
  form.append('url', url);
  form.append('message', [values.title.trim(), values.problemDescription.trim()].filter(Boolean).join('\n\n'));

  const optional = { experiment: experimentJson, ...context };
  for (const [key, value] of Object.entries(optional)) {
    if (value) form.append(key, value);
  }
  return form;
}

/**
 * Held outside React so `notifyError` can open the dialog from a toast action without a hook,
 * the same way `toastManager` lets it raise the toast. `seq` changes on every open, keying the
 * form so each report starts from its own draft.
 */
interface ReportErrorState {
  open: boolean;
  draft: ReportErrorDraft;
  seq: number;
}

let state: ReportErrorState = { open: false, draft: {}, seq: 0 };
const listeners = new Set<() => void>();

function setState(next: ReportErrorState) {
  state = next;
  listeners.forEach((listener) => listener());
}

export function openReportError(draft: ReportErrorDraft = {}): void {
  setState({ open: true, draft, seq: state.seq + 1 });
}

export function closeReportError(): void {
  setState({ ...state, open: false });
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function useReportErrorState(): ReportErrorState {
  return useSyncExternalStore(subscribe, () => state);
}
