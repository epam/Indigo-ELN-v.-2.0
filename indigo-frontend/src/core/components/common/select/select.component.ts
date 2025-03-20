import { dropdownAnimation } from '@/core/animations/control-animations';
import { DropdownBaseComponent } from '@/core/components/common/dropdown/dropdown-base.component';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  EventEmitter,
  forwardRef,
  Input,
  Output,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ChipComponent } from '../chip/chip.component';
import { DropdownMenuItem } from '../dropdown-menu/dropdown-menu.i';

type SelectValue = string | string[] | null;

@Component({
  selector: 'app-select',
  standalone: true,
  imports: [CommonModule, ClassPickerPipe, ChipComponent],
  templateUrl: './select.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SelectComponent),
      multi: true,
    },
  ],
  animations: [dropdownAnimation],
})
export class SelectComponent
  extends DropdownBaseComponent
  implements ControlValueAccessor, AfterViewInit
{
  @Input() items: DropdownMenuItem[] = [];
  @Input() placeholder = 'Select an option';
  @Input() label?: string;
  @Input() required = false;
  @Input() disabled = false;
  @Input() hasError = false;
  @Input() multiple = false;
  @Input() renderChips = false;
  @Input() suffixStyle = false;
  @Output() selectionChange = new EventEmitter<SelectValue>();

  value: SelectValue = null;
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  onChange: (value: SelectValue) => void = () => {};
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  onTouched: () => void = () => {};

  readonly containerSelector = '.select-container';

  get selectedLabel(): string | undefined {
    if (!this.value) return undefined;

    if (!this.multiple) {
      return this.getItemLabel(this.value as string);
    } else if (Array.isArray(this.value)) {
      return this.getMultiSelectLabel(this.value);
    }

    return undefined;
  }

  get selectedValues(): string[] {
    return this.getSelectedValues();
  }

  ngAfterViewInit(): void {
    this.checkDropdownPosition();
  }

  override toggleDropdown(event: MouseEvent): void {
    if (this.disabled) return;

    super.toggleDropdown(event);

    // Mark as touched when closed
    if (!this.isOpen) {
      this.onTouched();
    }
  }

  selectItem(item: DropdownMenuItem, event: MouseEvent): void {
    if (this.disabled || item.disabled || !item.value) return;

    event.stopPropagation();

    if (this.multiple) {
      this.handleMultiSelect(item);
    } else {
      this.handleSingleSelect(item);
    }

    this.onTouched();
  }

  isSelected(item: DropdownMenuItem): boolean {
    if (!this.value || !item.value) return false;

    if (this.multiple && Array.isArray(this.value)) {
      return this.value.includes(item.value);
    }

    return this.value === item.value;
  }

  removeItem(value: string, event: MouseEvent | null): void {
    event?.stopPropagation();

    if (!this.multiple || !Array.isArray(this.value)) return;

    const newValues = this.value.filter((v) => v !== value);
    this.value = newValues.length > 0 ? newValues : [];
    this.onChange(this.value);
    this.selectionChange.emit(this.value);
  }

  writeValue(value: SelectValue): void {
    if (this.multiple) {
      this.value = this.normalizeMultiValue(value);
    } else {
      this.value = value;
    }
  }

  registerOnChange(fn: (value: SelectValue) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  // Utility functions
  public getItemLabel(value: string): string | undefined {
    const selectedItem = this.items.find((item) => item.value === value);
    return selectedItem?.label;
  }

  private getMultiSelectLabel(values: string[]): string | undefined {
    if (values.length === 0) return undefined;

    if (values.length === 1) {
      return this.getItemLabel(values[0]);
    }

    return `${values.length} items selected`;
  }

  private getSelectedValues(): string[] {
    if (!this.value) return [];
    return Array.isArray(this.value) ? this.value : [this.value as string];
  }

  private handleSingleSelect(item: DropdownMenuItem): void {
    if (this.value !== item.value) {
      this.value = item.value as string;
      this.onChange(this.value);
      this.selectionChange.emit(this.value);
    }

    // Close dropdown for single select
    this.isOpen = false;
  }

  private handleMultiSelect(item: DropdownMenuItem): void {
    if (!item.value) {
      return;
    }
    const currentValues = Array.isArray(this.value) ? [...this.value] : [];

    if (currentValues.includes(item.value)) {
      // Remove if already selected
      const newValues = currentValues.filter((v) => v !== item.value);
      this.value = newValues.length > 0 ? newValues : [];
    } else {
      // Add if not selected
      this.value = [...currentValues, item.value];
    }

    this.onChange(this.value);
    this.selectionChange.emit(this.value);

    // Don't close dropdown for multiselect
  }

  private normalizeMultiValue(value: SelectValue): string[] {
    if (value === null || value === undefined) {
      return [];
    } else if (!Array.isArray(value)) {
      return [value];
    } else {
      return value;
    }
  }
}
