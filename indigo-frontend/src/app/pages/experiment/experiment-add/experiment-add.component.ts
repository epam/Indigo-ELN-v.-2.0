import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { NotebookService } from '@/core/services/notebook/notebook.service';
import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { finalize, map } from 'rxjs/operators';
import { RootTemplate, ItemTemplate } from '@/core/types/entities/template.i';

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

  fields: FormlyFieldConfig[] = [];
  templatesLoading = false; // true while fetching template options
  submitting = false; // true while submitting create request
  ready = false;
  notebookId: string;

  ngOnInit(): void {
    this.initForm();
    this.loadTemplates();
  }

  createExperiment(formData: ExperimentForm): void {
    this.submitting = true;

    this.api
      .request<void>('post', `/notebooks/${this.notebookId}/experiments`, {
        templateID: formData.templateId,
      })
      .pipe(
        finalize(() => {
          this.submitting = false;
        }),
      )
      .subscribe({
        next: () => {
          this.showNotification('Experiment created', NotificationType.Success);
          this.dialogRef.close('refresh');
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
      .request<RootTemplate>('get', 'templates')
      .pipe(
        map((res) =>
          res.items.map((item: ItemTemplate) => ({
            value: item.id,
            label: item.name,
          })),
        ),
        finalize(() => {
          this.templatesLoading = false;
          this.ready = true;
        }),
      )
      .subscribe({
        next: (options) => {
          this.fields[0].props!.options = options;
        },
      });
  }

  private showNotification(message: string, type: NotificationType): void {
    this.notification.notify({
      message,
      type,
      isInline: false,
    });
  }
}
