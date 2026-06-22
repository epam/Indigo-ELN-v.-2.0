import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';
import {
  ReportErrorContext,
  ReportErrorFormValue,
} from '@core/components/common/report-error-dialog/report-error-dialog.component';
import { ApiService } from '@core/services/api.service';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { Observable } from 'rxjs';

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
    return this.api.request<void>('post', 'incidents', this.buildPayload(formValue, context));
  }

  getTechnicalDetails(context?: ReportErrorContext | null): ReportErrorTechnicalDetails | null {
    const experiment = this.experimentDetailService.experimentDetail();

    return buildTechnicalDetails(this.getCurrentUrl(), experiment, context);
  }

  private buildPayload(formValue: ReportErrorFormValue, context?: ReportErrorContext | null): FormData {
    const experiment = this.experimentDetailService.experimentDetail();
    const payload = new FormData();
    const technicalDetails = buildTechnicalDetails(this.getCurrentUrl(), experiment, context);
    const message = buildIncidentMessage(formValue, technicalDetails);

    payload.append('message', message);

    if (experiment?.id) {
      payload.append('experimentId', experiment.id);
    }

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

function buildIncidentMessage(
  formValue: ReportErrorFormValue,
  technicalDetails: ReportErrorTechnicalDetails | null,
): string {
  if (!technicalDetails) {
    return buildUserMessage(formValue);
  }

  const technicalDetailsText = safePrettyStringify(technicalDetails);

  return [buildUserMessage(formValue), 'Technical details:', technicalDetailsText].filter(Boolean).join('\n\n');
}

function safePrettyStringify(value: unknown): string {
  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return 'Unable to serialize value';
  }
}
