import { Component, ContentChild, EventEmitter, inject, Input, Output, TemplateRef } from '@angular/core';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TwsxPipe } from '@/core/pipes/twsx.pipe';
import { FormlyFieldConfig, FormlyModule } from '@ngx-formly/core';
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
  ],
})
export class FormDialogComponent {
  dialogRef: MatDialogRef<FormDialogComponent> = inject(MatDialogRef);
  private notificationService = inject(NotificationService);
  form = new FormGroup({});

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
    this.formSubmit.emit(this.form.value);
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
