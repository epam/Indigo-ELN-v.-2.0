import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { FieldTypeConfig, FormlyModule, FieldType } from '@ngx-formly/core';
import { SelectComponent } from '@core/components/common/select/select.component';
import { Observable, of, isObservable } from 'rxjs';
import { DropdownMenuItem } from '@core/components/common/dropdown-menu/dropdown-menu.i';

@Component({
  selector: 'eln-formly-dropdown',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormlyModule, SelectComponent],
  template: `
    <eln-select
      [formControl]="formControl"
      [formlyAttributes]="field"
      [items]="(options$ | async) || []"
      [placeholder]="props['placeholder'] || ''"
      [label]="props['label']"
      [required]="props['required'] || false"
      [multiple]="props['multiple'] || false"
      [disabled]="props['disabled'] || false"
    ></eln-select>
  `,
})
export class DropdownFieldComponent extends FieldType<FieldTypeConfig> {
  get options$(): Observable<DropdownMenuItem[]> {
    const options = this.props['options'];
    return isObservable(options) ? options : of(options || []);
  }
}

