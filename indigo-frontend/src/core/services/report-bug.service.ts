import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';
import { ApiService } from '@core/services/api.service';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { ReportErrorContext, ReportErrorFormValue } from '@core/types/report-error.i';
import { Observable } from 'rxjs';

export const INCIDENTS_ENDPOINT = 'incidents';

export interface ReportErrorTechnicalDetails {
  currentUrl: string;
  experimentJson?: string;
  requestURL?: string;
  requestMethod?: string;
  requestBody?: string;
  responseBody?: string;
}

@Injectable({
  providedIn: 'root',
})
export class ReportBugService {
  private api = inject(ApiService);
  private document = inject(DOCUMENT);
  private experimentDetailService = inject(ExperimentDetailService);

  submitReport(formValue: ReportErrorFormValue, context?: ReportErrorContext | null): Observable<void> {
    return this.api.request<void>('post', INCIDENTS_ENDPOINT, this.buildPayload(formValue, context));
  }

  getTechnicalDetails(context?: ReportErrorContext | null): ReportErrorTechnicalDetails | null {
    const experiment = this.experimentDetailService.experimentDetail();

    return buildTechnicalDetails(this.getCurrentUrl(), experiment, context);
  }

  private buildPayload(formValue: ReportErrorFormValue, context?: ReportErrorContext | null): FormData {
    const experiment = this.experimentDetailService.experimentDetail();
    const payload = new FormData();
    const currentUrl = this.getCurrentUrl();

    payload.append('url', currentUrl);
    payload.append('message', buildUserMessage(formValue));

    if (experiment != null) {
      payload.append('experiment', safePrettyStringify(experiment));
    }

    appendContextField(payload, 'requestURL', context?.requestURL);
    appendContextField(payload, 'requestMethod', context?.requestMethod);
    appendContextField(payload, 'requestBody', context?.requestBody);
    appendContextField(payload, 'responseBody', context?.responseBody);

    return payload;
  }

  private getCurrentUrl(): string {
    return this.document.location?.href ?? '';
  }
}

function buildUserMessage(formValue: ReportErrorFormValue): string {
  return [formValue.title.trim(), formValue.problemDescription.trim()].filter(Boolean).join('\n\n');
}

function buildTechnicalDetails(
  currentUrl: string,
  experiment: unknown | null,
  context?: ReportErrorContext | null,
): ReportErrorTechnicalDetails | null {
  if (!context) {
    return null;
  }

  return {
    currentUrl,
    experimentJson: experiment ? safePrettyStringify(experiment) : undefined,
    ...(context?.requestURL ? { requestURL: context.requestURL } : {}),
    ...(context?.requestMethod ? { requestMethod: context.requestMethod } : {}),
    ...(context?.requestBody ? { requestBody: context.requestBody } : {}),
    ...(context?.responseBody ? { responseBody: context.responseBody } : {}),
  };
}

function safePrettyStringify(value: unknown): string {
  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return 'Unable to serialize value';
  }
}

function appendContextField(payload: FormData, key: string, value: string | undefined): void {
  if (value) {
    payload.append(key, value);
  }
}
