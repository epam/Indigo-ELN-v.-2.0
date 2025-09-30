import { Component, Input } from '@angular/core';
import {
  MatOption,
  MatSelect,
  MatSelectChange,
} from '@angular/material/select';

@Component({
  selector: 'eln-dropdown-value',
  templateUrl: './dropdown-value.component.html',
  imports: [MatSelect, MatOption],
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
    return (
      this.options.find((option) => option.id === this.value)?.name ?? null
    );
  }

  changeValue(event: MatSelectChange) {
    const newValue = event.value;
    if (this.value != newValue) {
      this.onChange(newValue);
    }
  }
}
