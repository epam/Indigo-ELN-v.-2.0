import { Component, forwardRef, Input } from '@angular/core';
import {
  NumericSearch,
  NumericSearchTypeNames,
} from '@core/types/entities/experiments/search.i';
import { MatOption, MatSelect } from '@angular/material/select';
import { MatInput } from '@angular/material/input';
import { FormsModule, NG_VALUE_ACCESSOR } from '@angular/forms';
import { KeyValuePipe } from '@angular/common';

@Component({
  selector: 'eln-numeric-search',
  imports: [MatSelect, MatOption, MatInput, FormsModule, KeyValuePipe],
  templateUrl: './numeric-search.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => NumericSearchComponent),
      multi: true,
    },
  ],
})
export class NumericSearchComponent {
  @Input() value: NumericSearch;
  disabled = false;
  defaultValue = { type: 'eq', value: NaN } as NumericSearch;
  onChange: (arg0: NumericSearch | null) => void = () => {};
  onTouched: () => void = () => {};

  writeValue(obj: NumericSearch | null): void {
    this.value = obj || this.defaultValue;
  }

  registerOnChange(fn: (arg0: NumericSearch | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  setType(type: typeof this.value.type) {
    this.value = { ...this.value, type: type } as NumericSearch;
    this.fireOnChange();
  }

  setInputValue(event: Event) {
    this.value = {
      ...this.value,
      value: (event.target as HTMLInputElement).valueAsNumber,
    } as NumericSearch;
    this.fireOnChange();
  }

  fireOnChange() {
    const value = !isNaN(this.value.value)
      ? { type: this.value.type, value: this.value.value }
      : null;
    this.onChange(value);
  }

  protected readonly NumericSearchTypeNames = NumericSearchTypeNames;
}
