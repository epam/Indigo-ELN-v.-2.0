import { Component, Input } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import {
  MatOption,
  MatSelect,
  MatSelectChange,
} from '@angular/material/select';

@Component({
  selector: 'eln-dropdown-value',
  templateUrl: './dropdown-value.component.html',
  imports: [DecimalPipe, MatSelect, MatOption],
  styles: `
    :host {
      display: contents;
    }
    .select {
      cursor: pointer;
      color: cornflowerblue;
    }
  `,
})
export class DropdownValueComponent {
  @Input() value: string | null;
  @Input() options: { id: string; name: string }[];
  @Input() onChange: ((newValue: string | null) => void) | null = null;
  @Input() allowNull = true;
  @Input() readOnly = false;

  getDisplayName(): string | null {
    if (this.value != null) {
      for (const option of this.options) {
        if (this.value === option.id) {
          return option.name;
        }
      }
    }
    return null;
  }

  changeValue(event: MatSelectChange) {
    const newValue = event.value;
    if (this.value != newValue) {
      this.onChange(newValue);
    }
  }
}
