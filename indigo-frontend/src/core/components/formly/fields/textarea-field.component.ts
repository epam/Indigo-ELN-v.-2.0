import { twsx } from '@/core/utils/twsx';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { FieldType, FieldTypeConfig, FormlyModule } from '@ngx-formly/core';

@Component({
  selector: 'eln-formly-textarea',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormlyModule],
  template: `
    <textarea
      [formControl]="formControl"
      [class]="getTextareaClasses()"
      [rows]="rows"
      [formlyAttributes]="field"
    ></textarea>
  `,
})
export class TextareaFieldComponent extends FieldType<FieldTypeConfig> {
  get rows(): number {
    return this.props['rows'] || 6;
  }

  getTextareaClasses(): string {
    return twsx(
      'relative inline-flex w-full rounded-sm border px-3 py-2 transition-colors duration-200 border-neutral-300 outline-none placeholder:text-neutral-600 text-sm min-h-32 resize-y',
      !this.formControl.disabled && 'focus:border-primary-400 focus:text-black',
      this.formControl.disabled && 'cursor-not-allowed bg-neutral-100 border-neutral-300 text-neutral-600',
      !this.formControl.disabled && !this.showError && 'hover:border-primary-400 active:border-primary-400',
      this.showError && 'border-red-200 text-red-200',
    );
  }
}
