import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { CommonModule } from '@angular/common';
import {
  Component,
  ContentChild,
  EventEmitter,
  inject,
  Input,
  Output,
  TemplateRef,
} from '@angular/core';
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
  form = new FormGroup({});
  model: any = {};

  @Input() title = '';
  @Input() fields: FormlyFieldConfig[] = [];

  @Input() size: 'sm' | 'md' | 'lg' | 'xl' | 'xxl' | 'auto' = 'auto';
  @Input() hideSubmitButton = false;
  @Input() hideCancelButton = false;
  @Input() submitButtonText = 'Submit';
  @Input() cancelButtonText = 'Cancel';
  @Input() containerClass = '';
  @Output() formSubmit = new EventEmitter<any>();
  @ContentChild('modalHeader') modalHeader: TemplateRef<unknown> | null = null;
  @ContentChild('modalContent') modalContent: TemplateRef<unknown> | null =
    null;
  @ContentChild('modalFooter') modalFooter: TemplateRef<unknown> | null = null;

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.formSubmit.emit(this.form.value);
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-backdrop')) {
      this.dialogRef.close('backdrop');
    }
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.dialogRef.close('escape');
    }
  }
}
