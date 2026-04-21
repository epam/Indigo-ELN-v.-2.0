import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { FieldType, FieldTypeConfig, FormlyModule } from '@ngx-formly/core';
import { DictionarySelectComponent } from '../../common/dictionary-select/dictionary-select.component';

@Component({
  selector: 'eln-formly-select',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormlyModule, DictionarySelectComponent],
  template: `
    <eln-dictionary-select
      [dictionaryId]="props['dictionaryId']"
      [formControl]="formControl"
      [formlyAttributes]="field"
      [multiple]="props['multiple']"
      [required]="props['required']"
    >
    </eln-dictionary-select>
  `,
})
export class SelectFieldComponent extends FieldType<FieldTypeConfig> {}
