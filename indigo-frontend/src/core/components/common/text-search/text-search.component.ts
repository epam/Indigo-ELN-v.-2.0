import { Component, forwardRef, Input } from '@angular/core';
import {
  TextSearch,
  TextSearchTypeNames,
} from '@core/types/entities/experiments/search.i';
import { MatOption, MatSelect } from '@angular/material/select';
import { MatInput } from '@angular/material/input';
import {
  ControlValueAccessor,
  FormsModule,
  NG_VALUE_ACCESSOR,
} from '@angular/forms';
import { KeyValuePipe } from '@angular/common';

@Component({
  selector: 'eln-text-search',
  imports: [MatSelect, MatOption, MatInput, FormsModule, KeyValuePipe],
  templateUrl: './text-search.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TextSearchComponent),
      multi: true,
    },
  ],
})
export class TextSearchComponent implements ControlValueAccessor {
  @Input() value: TextSearch;
  disabled = false;
  defaultValue = { type: 'exact', value: '', from: '', to: '' } as TextSearch;
  onChange: ((arg0: TextSearch) => void) | null = null;
  onTouched: (() => void) | null = null;

  writeValue(obj: TextSearch | null): void {
    this.value = obj || this.defaultValue;
  }

  registerOnChange(fn: (arg0: TextSearch | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  setType(type: typeof this.value.type) {
    this.value = { ...this.value, type: type } as TextSearch;
    this.fireOnChange();
  }

  setInputValue(event: Event) {
    this.value = {
      ...this.value,
      value: (event.target as HTMLInputElement).value,
    } as TextSearch;
    this.fireOnChange();
  }

  setInputFrom(event: Event) {
    this.value = {
      ...this.value,
      from: (event.target as HTMLInputElement).value,
    } as TextSearch;
    this.fireOnChange();
  }

  setInputTo(event: Event) {
    this.value = {
      ...this.value,
      to: (event.target as HTMLInputElement).value,
    } as TextSearch;
    this.fireOnChange();
  }

  fireOnChange() {
    let value: TextSearch | null;
    if (this.value.type !== 'between') {
      value = this.value.value
        ? { type: this.value.type, value: this.value.value }
        : null;
    } else {
      value =
        this.value.from && this.value.to
          ? { type: this.value.type, from: this.value.from, to: this.value.to }
          : null;
    }
    this.onChange?.(value);
  }

  protected readonly TextSearchTypeNames = TextSearchTypeNames;
}
