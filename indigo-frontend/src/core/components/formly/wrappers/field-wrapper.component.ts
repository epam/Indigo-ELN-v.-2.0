import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import {
  FormlyFieldProps as CoreFormlyFieldProps,
  FieldWrapper,
  FormlyFieldConfig,
  FormlyModule,
} from '@ngx-formly/core';

export interface FormlyFieldProps extends CoreFormlyFieldProps {
  hideLabel?: boolean;
  hideRequiredMarker?: boolean;
  labelPosition?: 'floating';
}

@Component({
  selector: 'eln-wrapper-form-field',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormlyModule],
  template: `
    <ng-template #labelTemplate>
      <label
        *ngIf="props.label && props.hideLabel !== true"
        [attr.for]="id"
        class="form-label block mb-2 text-xs"
      >
        {{ props.label }}
        <span
          *ngIf="props.required && props.hideRequiredMarker !== true"
          class="text-red-500"
          aria-hidden="true"
          >*</span
        >
      </label>
    </ng-template>

    <div [class.has-error]="showError">
      <ng-container *ngIf="props.labelPosition !== 'floating'">
        <ng-container [ngTemplateOutlet]="labelTemplate"></ng-container>
      </ng-container>

      <ng-template #fieldComponent></ng-template>

      <ng-container *ngIf="props.labelPosition === 'floating'">
        <ng-container [ngTemplateOutlet]="labelTemplate"></ng-container>
      </ng-container>

      <div *ngIf="showError" class="invalid-feedback" [style.display]="'block'">
        <formly-validation-message
          id="{{ id }}-formly-validation-error"
          class="text-red-500 text-xs ml-1"
          [field]="field"
          role="alert"
        ></formly-validation-message>
      </div>

      <small *ngIf="props.description" class="form-text text-muted">{{
        props.description
      }}</small>
    </div>
  `,
})
export class ElnWrapperFormField extends FieldWrapper<
  FormlyFieldConfig<FormlyFieldProps>
> {}
