import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { ItemTemplate, RootTemplate } from '@/core/types/entities/template.i';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { NotificationService } from '@core/services/notification/notification.service';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';
import { NotificationType } from '@core/types/notification.i';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { finalize, map, tap } from 'rxjs/operators';

interface ExperimentForm {
  templateId: string;
}

@Component({
  standalone: true,
  selector: 'app-experiment-add',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, MatInputModule, FormDialogComponent],
  templateUrl: './experiment-add.component.html',
})
export class ExperimentAddComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private dialogRef = inject(MatDialogRef<ExperimentAddComponent>);
  private notification = inject(NotificationService);

  fields: FormlyFieldConfig[] = [];
  contentLoading = true;
  contentError: string | null = null;
  notebookId: string;

  ngOnInit(): void {
    this.initForm();
    this.loadTemplates();
  }

  createExperimentFn = (formData: ExperimentForm) =>
    this.api
      .request<ExperimentDetail>('post', `/notebooks/${this.notebookId}/experiments`, {
        templateID: formData.templateId,
      })
      .pipe(
        tap((newExperiment: ExperimentDetail) => {
          this.showNotification('Experiment created', NotificationType.Success);
          this.dialogRef.close('refresh');
          this.router.navigate(['/experiments', newExperiment.id]);
        }),
      );

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
    this.api
      .request<RootTemplate>('get', 'templates')
      .pipe(
        map((res) =>
          res.items.map((item: ItemTemplate) => ({
            value: item.id,
            label: item.name,
          })),
        ),
        finalize(() => (this.contentLoading = false)),
      )
      .subscribe({
        next: (options) => {
          this.fields[0].props!.options = options;
        },
        error: () => {
          this.contentError = 'Failed to load templates';
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
