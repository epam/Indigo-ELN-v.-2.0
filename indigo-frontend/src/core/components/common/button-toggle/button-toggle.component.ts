import { CommonModule } from '@angular/common';
import { Component, forwardRef, Input } from '@angular/core';
import {
  ControlValueAccessor,
  FormsModule,
  NG_VALUE_ACCESSOR,
} from '@angular/forms';
import { ButtonToggleVariantPipe } from './button-toggle-variant.pipe';

export interface ToggleOption {
  value: string;
  icon?: string;
  disabled?: boolean;
}

@Component({
  selector: 'app-button-toggle',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonToggleVariantPipe],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ButtonToggleComponent),
      multi: true,
    },
  ],
  templateUrl: './button-toggle.component.html',
})
export class ButtonToggleComponent implements ControlValueAccessor {
  @Input() options: ToggleOption[] = [];
  @Input() variant: 'default' | 'alpha' = 'default';
  @Input() classList = '';

  selectedValue: string | null = null;
  disabled = false;

  onChange = (value: string) => {};

  onTouch = () => {};

  writeValue(value: string): void {
    this.selectedValue = value;
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouch = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onButtonClick(option: ToggleOption): void {
    if (this.disabled || option.disabled) return;

    this.selectedValue = option.value;
    this.onChange(this.selectedValue);
    this.onTouch();
  }

  isSelected(value: string): boolean {
    return this.selectedValue === value;
  }
}
