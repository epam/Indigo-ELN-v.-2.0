import { Component, forwardRef, input, output, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ClassPickerPipe } from '../../../pipes/classPicker.pipe';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-input',
  templateUrl: './input.component.html',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true,
    },
  ],
  imports: [ClassPickerPipe, CommonModule],
})
export class InputComponent implements ControlValueAccessor {
  label = input.required<string>();
  placeholder = input<string>();
  required = input<boolean>(false);
  disabled = input<boolean>(false);
  hasError = input<boolean>(false);
  valueChange = output<string>();

  value = signal<string>('');

  onChange = (value: string) => {
    this.value.set(value);
  };

  onTouched = () => {};

  writeValue(value: string): void {
    this.value.set(value);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  onInput(event: any) {
    this.onChange(event.target.value);
    this.valueChange.emit(event.target.value);
  }

  getPlaceholder() {
    return this.placeholder() || this.label();
  }

  clearValue() {
    this.value.set('');
    this.onInput({ target: { value: '' } });
  }
}
