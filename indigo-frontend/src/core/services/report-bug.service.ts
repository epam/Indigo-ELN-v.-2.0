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
    if (!context) {
      return null;
    }

    const experiment = this.experimentDetailService.experimentDetail();

    return {
      currentUrl: this.getCurrentUrl(),
      experimentJson: experiment ? safePrettyStringify(experiment) : undefined,
      requestURL: context.requestURL,
      requestMethod: context.requestMethod,
      requestBody: context.requestBody,
      responseBody: context.responseBody,
    };
  }

  private buildPayload(formValue: ReportErrorFormValue, context?: ReportErrorContext | null): FormData {
    const experiment = this.experimentDetailService.experimentDetail();
    const payload = new FormData();
    const message = buildIncidentMessage(formValue, this.getCurrentUrl(), context);

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

function buildIncidentMessage(
  formValue: ReportErrorFormValue,
  currentUrl: string,
  context?: ReportErrorContext | null,
): string {
  const details = {
    currentUrl,
    ...(context?.requestURL ? { requestURL: context.requestURL } : {}),
    ...(context?.requestMethod ? { requestMethod: context.requestMethod } : {}),
    ...(context?.requestBody ? { requestBody: context.requestBody } : {}),
    ...(context?.responseBody ? { responseBody: context.responseBody } : {}),
  };

  const technicalDetails = safePrettyStringify(details);

  return [buildUserMessage(formValue), 'Technical details:', technicalDetails].filter(Boolean).join('\n\n');
}

function safePrettyStringify(value: unknown): string {
  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return 'Unable to serialize value';
  }
}
