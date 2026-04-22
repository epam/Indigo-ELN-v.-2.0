import { TwsxPipe } from '@/core/pipes/twsx.pipe';
import { CommonModule } from '@angular/common';
import { Component, forwardRef, Input, input, output, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ClassPickerPipe } from '../../../pipes/classPicker.pipe';

@Component({
  selector: 'eln-input',
  templateUrl: './input.component.html',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputComponent),
      multi: true,
    },
  ],
  imports: [ClassPickerPipe, CommonModule, TwsxPipe],
})
export class InputComponent implements ControlValueAccessor {
  @Input() icon;
  @Input() wrapperClassname: string;
  @Input() labelClassname: string;
  @Input() inputWrapperClassname: string;
  @Input() inputClassname: string;
  @Input() iconClassname: string;
  @Input() showClearButton = true;
  label = input<string>();
  placeholder = input<string>();
  required = input<boolean>(false);
  hasError = input<boolean>(false);
  suffixStyle = input<boolean>(false);
  valueChange = output<string>();
  value = signal<string>('');
  disabled = signal<boolean>(false);

  onChange = (value: string) => {
    this.value.set(value);
  };

  onTouched = () => {
    /* noop */
  };

  writeValue(value: string): void {
    this.value.set(value);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled.set(isDisabled);
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
