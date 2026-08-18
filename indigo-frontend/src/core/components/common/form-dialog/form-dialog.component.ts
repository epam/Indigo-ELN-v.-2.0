import { Component, ContentChild, EventEmitter, inject, Input, Output, TemplateRef } from '@angular/core';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TwsxPipe } from '@/core/pipes/twsx.pipe';
import { FormlyFieldConfig, FormlyModule } from '@ngx-formly/core';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'eln-form-dialog',
  templateUrl: 'form-dialog.component.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormlyModule,
    CommonModule,
    MatButtonModule,
    MatDialogModule,
    ClassPickerPipe,
    FormlyModule,
    ButtonComponent,
    TwsxPipe,
    MatProgressSpinner,
  ],
})
export class FormDialogComponent {
  dialogRef: MatDialogRef<FormDialogComponent> = inject(MatDialogRef);
  private notificationService = inject(NotificationService);
  form = new FormGroup({});
  submitting = false;

  @Input() model: any = {};
  @Input() title = '';
  @Input() fields: FormlyFieldConfig[] = [];
  @Input() size: 'sm' | 'md' | 'lg' | 'xl' | 'xxl' | 'auto' = 'auto';
  @Input() hideSubmitButton = false;
  @Input() hideCancelButton = false;
  @Input() submitButtonText = 'Submit';
  @Input() cancelButtonText = 'Cancel';
  @Input() submitEnabled = true;
  @Input() submitLoading = false;
  @Input() closeOnBackdropClick = false;
  @Input() containerClass = '';
  @Input() toastMessages: Record<string, string> = {};
  @Input() contentLoading = false;
  @Input() contentError: string | null = null;
  @Input() submitFn?: (data: any) => Observable<any>;
  @Output() formSubmit = new EventEmitter<any>();
  @ContentChild('modalHeader') modalHeader: TemplateRef<unknown> | null = null;
  @ContentChild('modalContent') modalContent: TemplateRef<unknown> | null = null;
  @ContentChild('modalFooter') modalFooter: TemplateRef<unknown> | null = null;

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.showValidationErrors();
      return;
    }
    if (this.submitFn) {
      this.submitting = true;
      this.submitFn(this.form.value)
        .pipe(finalize(() => (this.submitting = false)))
        .subscribe({ error: () => {} });
    } else {
      this.formSubmit.emit(this.form.value);
    }
  }

  private showValidationErrors(): void {
    const messages: string[] = [];

    Object.keys(this.form.controls).forEach((key) => {
      const control = this.form.get(key);
      if (control?.errors) {
        const fieldConfig = this.fields.find((f) => f.key === key);
        const validationMessages = fieldConfig?.validation?.messages || {};

        Object.keys(control.errors).forEach((errorKey) => {
          const msg = this.toastMessages[errorKey] ?? (validationMessages[errorKey] as string);
          if (msg && !messages.includes(msg)) {
            messages.push(msg);
          }
        });
      }
    });

    messages.forEach((message) => {
      this.notificationService.notify({
        message,
        type: NotificationType.Error,
        isInline: false,
      });
    });
  }

  onBackdropClick(event: MouseEvent): void {
    if (this.closeOnBackdropClick && (event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.dialogRef.close('backdrop');
    }
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.dialogRef.close('escape');
    }
  }
}
