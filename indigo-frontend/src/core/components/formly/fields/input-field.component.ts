import { twsx } from '@/core/utils/twsx';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { FieldType, FieldTypeConfig, FormlyModule } from '@ngx-formly/core';

@Component({
  selector: 'eln-formly-input',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormlyModule],
  template: `
    <div class="relative flex items-center">
      <input
        [type]="type"
        [formControl]="formControl"
        [class]="getInputClasses()"
        [formlyAttributes]="field"
      />

      <em
        *ngIf="formControl.value && !formControl.disabled"
        class="chip-icon indicon-close text-lg cursor-pointer absolute z-50 right-2"
        (click)="clearValue()"
      ></em>
    </div>
  `,
})
export class InputFieldComponent extends FieldType<FieldTypeConfig> {
  get type() {
    return this.props.type || 'text';
  }

  getInputClasses(): string {
    return twsx(
      'relative inline-flex w-full items-center gap-2 rounded-sm border px-3 py-2 transition-colors duration-200 border-neutral-300 outline-none placeholder:text-neutral-600 text-sm',
      !this.formControl.disabled && 'focus:border-primary-400 focus:text-black',
      this.formControl.disabled &&
        'cursor-not-allowed bg-neutral-100 border-neutral-300 text-neutral-600',
      !this.formControl.disabled &&
        !this.showError &&
        'hover:border-primary-400 active:border-primary-400',
      this.showError && 'border-red-200 text-red-200',
      this.props['suffixStyle'] && 'border-none rounded-l-none',
    );
  }

  clearValue() {
    this.formControl.setValue('');
  }
}
