import { getRandomStr } from '@/core/utils/string.util';
import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-toggle',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ToggleComponent),
      multi: true,
    },
  ],
  template: ` <label [for]="id" class="flex items-center cursor-pointer">
    <div class="relative">
      <input
        [id]="id"
        type="checkbox"
        class="sr-only peer"
        [checked]="value"
        (change)="onInputChange($event)"
        [disabled]="disabled"
      />
      <div class="w-10 h-4 bg-primary-100 rounded-full shadow-inner"></div>
      <div
        class="absolute w-6 h-6 bg-white rounded-full shadow -left-1 -top-1 transition peer-checked:translate-x-full peer-checked:bg-primary-400"
      ></div>
    </div>
    @if (label.length > 0) {
      <div class="ml-3 text-neutral-1000 font-medium select-none">
        {{ label }}
      </div>
    }
  </label>`,
})
export class ToggleComponent implements ControlValueAccessor {
  @Input() id = getRandomStr();
  @Input() label: string = '';

  value = false;
  disabled = false;

  onChange = (value: boolean) => {};
  onTouch = () => {};

  writeValue(value: boolean): void {
    this.value = value;
  }

  registerOnChange(fn: (value: boolean) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouch = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onInputChange(event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    this.value = checkbox.checked;
    this.onChange(this.value);
    this.onTouch();
  }
}
