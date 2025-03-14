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
  placeholder = input<[string, string]>();
  required = input<boolean>(false);
  hasError = input<boolean>(false);
  items = input<DropdownMenuItem[]>([]);
  valueChange = output<[string, string]>();
  value = signal<[string, string]>(['', '']);
  disabled = signal<boolean>(false);

  formGroup!: FormGroup;

  ngOnInit() {
    this.formGroup = new FormGroup({
      suffix: new FormControl({
        value: this.value()[0],
        disabled: this.disabled(),
      }),
      input: new FormControl({
        value: this.value()[1],
        disabled: this.disabled(),
      }),
    });
  }

  onChange = (value: [string, string]) => {
    this.value.set(value);
  };

  onTouched = () => {};

  writeValue(value: [string, string]): void {
    this.value.set(value);
    this.formGroup.patchValue({ suffix: value[0], input: value[1] });
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
    const placeholderTuple = this.placeholder();
    if (placeholderTuple?.length) {
      item === 'suffix' ? placeholderTuple[0] : placeholderTuple[1];
    }
    return item === 'suffix' ? 'Select an option' : this.label();
  }
}
