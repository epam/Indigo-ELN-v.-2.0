import { Component, forwardRef, input, output, signal } from '@angular/core';
import {
  ControlValueAccessor,
  FormControl,
  FormGroup,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
} from '@angular/forms';
import { SelectComponent } from '../select/select.component';
import { InputComponent } from '../input/input.component';
import { DropdownMenuItem } from '../dropdown-menu/dropdown-menu.i';
import { ClassPickerPipe } from '../../../pipes/classPicker.pipe';
import { InputSuffixValue } from '../../../types/input-suffix.i';

@Component({
  selector: 'app-input-suffix',
  templateUrl: './input-suffix.component.html',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => InputSuffixComponent),
      multi: true,
    },
  ],
  imports: [
    SelectComponent,
    InputComponent,
    ClassPickerPipe,
    ReactiveFormsModule,
  ],
})
export class InputSuffixComponent implements ControlValueAccessor {
  label = input.required<string>();
  placeholder = input<InputSuffixValue>();
  required = input<boolean>(false);
  hasError = input<boolean>(false);
  items = input<DropdownMenuItem[]>([]);
  valueChange = output<InputSuffixValue>();
  value = signal<InputSuffixValue>({
    suffix: '',
    input: '',
  });
  disabled = signal<boolean>(false);

  formGroup!: FormGroup;

  ngOnInit() {
    this.formGroup = new FormGroup({
      suffix: new FormControl({
        value: this.value().suffix,
        disabled: this.disabled(),
      }),
      input: new FormControl({
        value: this.value().input,
        disabled: this.disabled(),
      }),
    });
  }

  onChange = (value: InputSuffixValue) => {
    this.value.set(value);
  };

  onTouched = () => {};

  writeValue(value: InputSuffixValue): void {
    this.value.set(value);
    this.formGroup.patchValue({ suffix: value.suffix, input: value.input });
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled.set(isDisabled);
    if (isDisabled) {
      this.formGroup.get('suffix')?.disable();
      this.formGroup.get('input')?.disable();
    }
  }

  getPlaceholder(item: 'suffix' | 'input') {
    const placeholder = this.placeholder();
    if (item === 'suffix' && placeholder?.suffix) {
      return placeholder.suffix;
    } else if (item === 'input' && placeholder?.input) {
      return placeholder.input;
    }
    return item === 'suffix' ? 'Select an option' : this.label() || 'Enter text';
  }
}
