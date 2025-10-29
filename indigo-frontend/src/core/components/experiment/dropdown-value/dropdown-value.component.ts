import { Component, forwardRef, Input } from '@angular/core';
import {
  MatOption,
  MatSelect,
  MatSelectChange,
} from '@angular/material/select';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'eln-dropdown-value',
  templateUrl: './dropdown-value.component.html',
  imports: [MatSelect, MatOption],
  styles: `
    :host {
      display: contents;
    }
    .select {
      cursor: pointer;
      color: cornflowerblue;
    }
  `,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DropdownValueComponent),
      multi: true,
    },
  ],
})
export class DropdownValueComponent implements ControlValueAccessor {
  @Input() value: string | null;
  @Input() options: { id: string; name: string }[];
  @Input() onChange: ((newValue: string | null) => void) | null = null;
  @Input() allowNull = true;
  @Input() readOnly = false;
  onChangeForm: (newValue: string | null) => void = () => {};
  onTouchForm: () => void = () => {};

  getDisplayName(): string | null {
    return (
      this.options.find((option) => option.id === this.value)?.name ?? null
    );
  }

  changeValue(event: MatSelectChange) {
    const newValue = event.value;
    if (this.value != newValue) {
      this.onChange(newValue);
    }
  }

  writeValue(obj: any): void {
    this.value = obj?.value;
  }

  registerOnChange(fn: any): void {
    this.onChangeForm = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouchForm = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.readOnly = isDisabled;
  }
}
