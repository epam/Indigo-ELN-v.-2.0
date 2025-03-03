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
import { DropdownMenuItem } from '../dropdown-menu/dropdown-menu.i';

@Component({
  selector: 'app-select',
  standalone: true,
  imports: [CommonModule, ClassPickerPipe],
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
  @Output() selectionChange = new EventEmitter<string>();

  value: string | null = null;
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  onChange: (value: string | null) => void = () => {};
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  onTouched: () => void = () => {};

  readonly containerSelector = '.select-container';

  get selectedLabel(): string | undefined {
    const selectedItem = this.items.find((item) => item.value === this.value);
    return selectedItem?.label;
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
    if (this.disabled || item.disabled) return;

    event.stopPropagation();

    if (this.value !== item.value) {
      this.value = item.value as string;
      this.onChange(this.value);
      this.selectionChange.emit(this.value);
    }

    this.isOpen = false;
  }

  writeValue(value: string | null): void {
    this.value = value;
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
}
