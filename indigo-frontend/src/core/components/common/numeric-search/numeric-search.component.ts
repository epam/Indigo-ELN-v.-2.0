import { Component, forwardRef, OnInit } from '@angular/core';
import {
  NumericSearch,
  NumericSearchTypeNames,
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
import { KeyValuePipe } from '@angular/common';
import { DelegatingControlBase } from '@core/components/common/delegating-control/delegating-control-base.component';

@Component({
  selector: 'eln-numeric-search',
  imports: [
    MatSelect,
    MatOption,
    MatInput,
    FormsModule,
    KeyValuePipe,
    ReactiveFormsModule,
  ],
  templateUrl: './numeric-search.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => NumericSearchComponent),
      multi: true,
    },
  ],
})
export class NumericSearchComponent
  extends DelegatingControlBase<NumericSearch>
  implements OnInit
{
  form = new FormGroup({
    type: new FormControl<keyof typeof NumericSearchTypeNames>('eq'),
    value: new FormControl<number | null>(null),
  });

  ngOnInit() {
    this.form.valueChanges.subscribe((formValue) => {
      let result: NumericSearch | null = null;
      if (formValue.value != null) {
        result = { type: formValue.type, value: formValue.value };
      }
      this.triggerChange(result);
    });
  }

  setValue(obj: NumericSearch | null): void {
    this.form.get('type').setValue(obj?.type || 'eq');
    this.form.get('value').setValue(obj?.value);
  }

  protected getControlsToDisable(): AbstractControl[] {
    return Object.values(this.form.controls);
  }

  protected readonly NumericSearchTypeNames = NumericSearchTypeNames;
}
