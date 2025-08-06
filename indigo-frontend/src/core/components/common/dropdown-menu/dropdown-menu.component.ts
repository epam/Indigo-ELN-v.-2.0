import { dropdownAnimation } from '@/core/animations/control-animations';
import { DropdownBaseComponent } from '@/core/components/common/dropdown/dropdown-base.component';
import { CommonModule } from '@angular/common';
import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  Output,
  forwardRef,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { DropdownMenuItem } from './dropdown-menu.i';

@Component({
  selector: 'eln-dropdown-menu',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dropdown-menu.component.html',
  animations: [dropdownAnimation],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DropdownMenuComponent),
      multi: true,
    },
  ],
})
export class DropdownMenuComponent
  extends DropdownBaseComponent
  implements AfterViewInit, ControlValueAccessor
{
  @Input() items: DropdownMenuItem[] = [];
  @Input() selected?: string;
  @Input() placeholder = 'Select Item';
  @Input() disabled = false;
  @Output() itemSelected = new EventEmitter<string>();
  @Output() dropdownToggled = new EventEmitter<boolean>();

  @Input() controlled = false;
  @Input() hasError = false;
  @Input() hasSuccess = false;

  readonly containerSelector = '.dd-container';

  get selectedItem(): DropdownMenuItem | undefined {
    if (!this.selected) return undefined;
    return this.items.find((item) => item.label === this.selected);
  }

  private _value: string | null = null;
  private onChange: (value: string | null) => void = () => {
    /* noop */
  };
  private onTouched: () => void = () => {
    /* noop */
  };

  ngAfterViewInit() {
    // Initial check
    this.checkDropdownPosition();
  }

  writeValue(value: string | null): void {
    this._value = value;
    this.updateSelectedFromValue(value);
  }

  registerOnChange(fn: (value: string | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  private updateSelectedFromValue(value: string | null): void {
    if (value) {
      const item = this.items.find(
        (item) => item.value === value || item.label === value,
      );
      this.selected = item?.label || value;
    } else {
      this.selected = undefined;
    }
  }

  override toggleDropdown(event: MouseEvent): void {
    if (this.disabled) return;

    super.toggleDropdown(event);
    this.dropdownToggled.emit(this.isOpen);

    // Mark as touched when dropdown is opened
    if (this.isOpen) {
      this.onTouched();
    }
  }

  selectItem(item: DropdownMenuItem, event: MouseEvent): void {
    if (this.disabled || item.disabled) return;

    event.stopPropagation();

    const newValue = item.value || item.label;

    if (this._value !== newValue) {
      this._value = newValue;

      if (!this.controlled) {
        this.selected = item.label;
      }

      this.onChange(newValue);

      this.itemSelected.emit(newValue);
    }

    this.isOpen = false;
    this.onTouched();
  }
}
