import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { NotebookService } from '@/core/services/notebook/notebook.service';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { finalize, map } from 'rxjs/operators';

interface ExperimentForm {
  templateId: string;
}

@Component({
  standalone: true,
  selector: 'app-experiment-add',
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatInputModule,
    FormDialogComponent,
    MatProgressSpinnerModule,
  ],
  templateUrl: './experiment-add.component.html',
  providers: [NotebookService],
})
export class ExperimentAddComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private dialogRef = inject(MatDialogRef<ExperimentAddComponent>);
  private dialogData = inject(MAT_DIALOG_DATA) as any;
  private notification = inject(NotificationService);
  private cdr = inject(ChangeDetectorRef);

  fields: FormlyFieldConfig[] = [];
  templatesLoading = false; // true while fetching template options
  submitting = false; // true while submitting create request
  ready = false;

  ngOnInit(): void {
    this.initForm();
    this.loadTemplates();
  }

  createExperiment(formData: ExperimentForm): void {
    const notebookId = this.getNotebookId();
    if (!notebookId) {
      this.notification.notify({
        message: 'Notebook ID not found',
        type: NotificationType.Error,
        isInline: false,
      });
      return;
    }

    this.submitting = true;
    this.invokeDialogCallback('onSubmitting', true);

    this.api
      .request<void>('post', `/notebooks/${notebookId}/experiments`, {
        templateID: formData.templateId,
      })
      .pipe(
        finalize(() => {
          this.submitting = false;
          this.invokeDialogCallback('onSubmitting', false);
        }),
      )
      .subscribe({
        next: () => {
          this.showNotification('Experiment created', NotificationType.Success);
          this.dialogRef.close('refresh');
        },
        error: () => {
          this.showNotification(
            'Failed to create experiment',
            NotificationType.Error,
          );
        },
      });
  }

  private initForm(): void {
    this.fields = [
      {
        type: 'dropdown',
        key: 'templateId',
        className: 'flex-1',
        props: {
          label: 'Select Template',
          placeholder: 'Select Template',
          options: [],
          required: true,
        },
      },
    ];
  }

  private loadTemplates(): void {
    this.templatesLoading = true;

    this.api
      .request<any>('get', 'templates')
      .pipe(
        map((res) =>
          res.items.map((item: any) => ({
            value: item.id,
            label: item.name,
          })),
        ),
        finalize(() => {
          this.templatesLoading = false;
          this.ready = true;
          this.cdr.markForCheck();
        }),
      )
      .subscribe({
        next: (options) => {
          this.fields[0].props!.options = options;
        },
        error: () => {
          this.templatesLoading = false;
          this.notification.notify({
            message: 'Failed to load templates',
            type: NotificationType.Error,
            isInline: false,
          });
        },
      });
  }

  private getNotebookId(): string | null {
    const segments = this.router.url.split('?')[0].split('/');
    const index = segments.indexOf('notebooks');
    return index > -1 ? segments[index + 1] : null;
  }

  private invokeDialogCallback(callbackName: string, value: boolean): void {
    if (this.dialogData?.[callbackName]) {
      try {
        this.dialogData[callbackName](value);
      } catch {}
    }
  }

  private showNotification(message: string, type: NotificationType): void {
    this.notification.notify({
      message,
      type,
      isInline: false,
    });
  }
}
