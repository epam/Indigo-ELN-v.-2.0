import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormDialogComponent } from '@core/components/common/form-dialog/form-dialog.component';
import { NotificationService } from '@core/services/notification/notification.service';
import { ReportBugService, ReportErrorTechnicalDetails } from '@core/services/report-bug.service';
import { NotificationType } from '@core/types/notification.i';
import { ReportErrorDialogData, ReportErrorFormValue } from '@core/types/report-error.i';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { finalize } from 'rxjs';

export type { ReportErrorContext, ReportErrorDialogData, ReportErrorFormValue } from '@core/types/report-error.i';

@Component({
  selector: 'eln-report-error-dialog',
  standalone: true,
  imports: [CommonModule, FormDialogComponent],
  templateUrl: './report-error-dialog.component.html',
})
export class ReportErrorDialogComponent {
  private dialogRef = inject(MatDialogRef<ReportErrorDialogComponent, ReportErrorFormValue>);
  private data = inject<ReportErrorDialogData | null>(MAT_DIALOG_DATA, { optional: true });
  private reportBugService = inject(ReportBugService);
  private notificationService = inject(NotificationService);

  protected readonly submitting = signal(false);
  protected readonly technicalDetails: ReportErrorTechnicalDetails | null = this.reportBugService.getTechnicalDetails(
    this.data?.context,
  );

  protected readonly model: ReportErrorFormValue = {
    title: this.data?.initialValue?.title ?? '',
    problemDescription: this.data?.initialValue?.problemDescription ?? '',
  };

  protected readonly fields: FormlyFieldConfig[] = [
    {
      type: 'input',
      key: 'title',
      props: {
        label: 'Title',
        placeholder: 'Brief summary of the issue',
        required: true,
      },
      validation: {
        messages: {
          required: 'Title is required.',
        },
      },
    },
    {
      type: 'textarea',
      key: 'problemDescription',
      props: {
        label: 'Problem Description',
        placeholder: 'Describe what happened and what you expected instead',
        required: true,
        rows: 6,
      },
      validation: {
        messages: {
          required: 'Problem Description is required.',
        },
      },
    },
    ...buildTechnicalFields(this.technicalDetails),
  ];

  onSubmit(value: ReportErrorFormValue): void {
    this.submitting.set(true);
    this.reportBugService
      .submitReport(value, this.data?.context)
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe(() => {
        this.notificationService.notify({
          message: 'Error report submitted successfully.',
          type: NotificationType.Success,
          isInline: false,
        });
        this.dialogRef.close(value);
      });
  }
}

function buildTechnicalFields(context: ReportErrorTechnicalDetails | null): FormlyFieldConfig[] {
  if (!context) {
    return [];
  }

  const fields: FormlyFieldConfig[] = [createReadonlyTextareaField('currentUrl', 'Current URL', context.currentUrl, 2)];

  if (context.requestURL) {
    fields.push(createReadonlyTextareaField('requestURL', 'Request URL', context.requestURL, 2));
  }

  if (context.requestMethod) {
    fields.push(createReadonlyTextareaField('requestMethod', 'Request Method', context.requestMethod, 1));
  }

  if (context.requestBody) {
    fields.push(createReadonlyTextareaField('requestBody', 'Request Body', context.requestBody, 4));
  }

  if (context.responseBody) {
    fields.push(createReadonlyTextareaField('responseBody', 'Response Body', context.responseBody, 6));
  }

  if (context.experimentJson) {
    fields.push(createReadonlyTextareaField('experimentJson', 'Experiment JSON', context.experimentJson, 8));
  }

  return fields;
}

function createReadonlyTextareaField(key: string, label: string, value: string, rows: number): FormlyFieldConfig {
  return {
    type: 'textarea',
    key,
    defaultValue: value,
    props: {
      label,
      rows,
      disabled: true,
      readonly: true,
    },
  };
}
