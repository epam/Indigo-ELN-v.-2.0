import { Component, forwardRef, OnInit } from '@angular/core';
import {
  TextSearch,
  TextSearchTypeNames,
} from '@core/types/entities/experiments/search.i';
import { MatOption, MatSelect } from '@angular/material/select';
import { MatInput } from '@angular/material/input';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormsModule,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { AsyncPipe, KeyValuePipe } from '@angular/common';
import { DelegatingControlBase } from '@core/components/common/delegating-control/delegating-control-base.component';

@Component({
  selector: 'eln-text-search',
  imports: [
    MatSelect,
    MatOption,
    MatInput,
    FormsModule,
    KeyValuePipe,
    ReactiveFormsModule,
    AsyncPipe,
  ],
  templateUrl: './text-search.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TextSearchComponent),
      multi: true,
    },
  ],
})
export class TextSearchComponent
  extends DelegatingControlBase<TextSearch>
  implements OnInit
{
  form = new FormGroup({
    type: new FormControl<keyof typeof TextSearchTypeNames>('exact'),
    value: new FormControl<string>(''),
    from: new FormControl<string>(''),
    to: new FormControl<string>(''),
  });

  ngOnInit() {
    this.form.valueChanges.subscribe((formValue) => {
      let result: TextSearch | null = null;
      switch (formValue.type) {
        case 'exact':
          if (formValue.value?.trim()) {
            result = { type: 'exact', value: formValue.value };
          }
          break;
        case 'startsWith':
          if (formValue.value?.trim()) {
            result = { type: 'startsWith', value: formValue.value };
          }
          break;
        case 'endsWith':
          if (formValue.value?.trim()) {
            result = { type: 'endsWith', value: formValue.value };
          }
          break;
        case 'contains':
          if (formValue.value?.trim()) {
            result = { type: 'contains', value: formValue.value };
          }
          break;
        case 'between':
          if (formValue.from?.trim() || formValue.to?.trim()) {
            result = {
              type: 'between',
              from: formValue.from,
              to: formValue.to,
            };
          }
          break;
      }
      this.triggerChange(result);
    });
  }

  setValue(obj: TextSearch | null): void {
    this.form.get('type').setValue(obj?.type || 'exact');
    this.form.get('value').setValue(obj?.type != 'between' ? obj?.value : '');
    this.form.get('from').setValue(obj?.type == 'between' ? obj?.from : '');
    this.form.get('to').setValue(obj?.type == 'between' ? obj?.to : '');
  }

  protected getControlsToDisable(): AbstractControl[] {
    return Object.values(this.form.controls);
  }

  protected readonly TextSearchTypeNames = TextSearchTypeNames;
}
