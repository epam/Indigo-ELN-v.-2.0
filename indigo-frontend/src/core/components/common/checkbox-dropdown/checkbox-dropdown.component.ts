import { CommonModule } from '@angular/common';
import {
  Component,
  Input,
  AfterViewInit,
  forwardRef,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';
import { DropdownBaseComponent } from '../dropdown/dropdown-base.component';
import { dropdownAnimation } from '@/core/animations/control-animations';
import { CheckboxDropdownItem } from './checkbox-dropdown.i';

@Component({
  selector: 'eln-checkbox-dropdown',
  imports: [CommonModule],
  templateUrl: './checkbox-dropdown.component.html',
  animations: [dropdownAnimation],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CheckboxDropdownComponent),
      multi: true,
    },
  ],
})
export class CheckboxDropdownComponent
  extends DropdownBaseComponent
  implements AfterViewInit, OnChanges, ControlValueAccessor
{
  @Input() items: CheckboxDropdownItem[] = [];
  @Input() disabled = false;
  @Input() placeholder = '';

  @Output() dropdownToggled = new EventEmitter<boolean>();
  @Output() selectionChanged = new EventEmitter<CheckboxDropdownItem[]>();

  readonly containerSelector = '.dd-container';

  private _value: string[] = [];
  private onChange: (value: string[]) => void = () => {
    /* noop */
  };
  private onTouched: () => void = () => {
    /* noop */
  };

  ngAfterViewInit() {
    this.checkDropdownPosition();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['items']) {
      this._value = this.items.filter((item) => item.checked).map((item) => item.value);
    }
  }

  writeValue(value: string[] | null): void {
    this._value = value ?? [];
    this.syncItemsFromValue();
  }

  registerOnChange(fn: (value: string[]) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  override toggleDropdown(event: MouseEvent): void {
    if (this.disabled) return;

    super.toggleDropdown(event);
    this.dropdownToggled.emit(this.isOpen);

    if (this.isOpen) {
      this.onTouched();
    }
  }

  toggleItemSelection(item: CheckboxDropdownItem): void {
    const nextValue = this._value.includes(item.value)
      ? this._value.filter((value) => value !== item.value)
      : [...this._value, item.value];

    this._value = nextValue;
    this.syncItemsFromValue();
    this.onChange(nextValue);
    this.selectionChanged.emit(this.items.map((option) => ({ ...option })));
  }

  private syncItemsFromValue(): void {
    this.items = this.items.map((item) => ({ ...item, checked: this._value.includes(item.value) }));
  }

  get selectedLabel(): string {
    const selectedItems = this._value
      .map((value) => this.items.find((item) => item.value === value))
      .filter((item): item is CheckboxDropdownItem => item !== undefined);

    if (selectedItems.length === 0) return this.placeholder;

    const selectedLabel = `${this.placeholder}: ${selectedItems[0].label}`;
    const additionalCount = selectedItems.length - 1;

    return additionalCount > 0 ? `${selectedLabel} (+${additionalCount})` : selectedLabel;
  }
}
