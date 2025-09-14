import { Component, ElementRef, inject, Input, ViewChild } from '@angular/core';
import {
  EnteredValue,
  EnteredValueSource,
  MeasurementUnit,
} from '@core/types/entities/values.i';
import { DecimalPipe } from '@angular/common';
import { MatOption, MatSelect } from '@angular/material/select';

@Component({
  selector: 'eln-entered-value',
  templateUrl: './entered-value.component.html',
  providers: [DecimalPipe],
  imports: [DecimalPipe, MatSelect, MatOption],
  styles: `
    :host {
      display: contents;
      text-align: right;
    }
    .viewing.readWrite {
      cursor: pointer;
      color: cornflowerblue;
    }
    .edit-number-narrow {
      width: 70%;
    }
    .edit-units {
      width: 30%;
    }
    .edit-number-wide {
      width: 100%;
    }
    .conflict {
      background-color: #f44336; /* Red background */
      transition: background-color 1s ease-out;
      animation: highlight-error 1s ease-out;
    }
    @keyframes highlight-error {
      0% {
        background-color: #f44336;
      }
      100% {
        background-color: lightpink;
      }
    }
    .recalculated {
      //background-color: #4caf50; /* Green background */
      transition: background-color 1s ease-out;
      animation: highlight-recalculated 1s ease-out;
    }
    @keyframes highlight-recalculated {
      0% {
        background-color: #4caf50; /* Start green */
      }
      100% {
        background-color: transparent; /* Fade to normal */
      }
    }
  `,
})
export class EnteredValueComponent {
  private _value: EnteredValue | null;
  @Input() showUnits = true;
  @Input() units: MeasurementUnit[];
  @Input() onChange: ((newValue: EnteredValue) => void) | null = null;
  @Input() readOnly = false;
  @ViewChild('editNumber') editNumberRef!: ElementRef<HTMLInputElement>;
  @ViewChild('editUnits') editUnitsRef!: MatSelect;
  @ViewChild('parent') parentRef!: ElementRef<HTMLElement>;
  decimalPipe = inject(DecimalPipe);
  editing = false;
  recalculated = false;

  @Input()
  set value(newValue: EnteredValue | null) {
    this._value = newValue;
    this.recalculated = false;
    if (
      this._value !== undefined &&
      newValue?.source === EnteredValueSource.CALCULATED_FROM_LAST_ENTERED
    ) {
      requestAnimationFrame(() => {
        this.recalculated = true;
      });
    }
  }

  get value(): EnteredValue | null {
    return this._value;
  }

  getUnitDisplayName(): string | null {
    if (this._value != null) {
      for (const unit of this.units) {
        if (this._value.unit === unit.value) {
          return unit.displayName;
        }
      }
    }
    return null;
  }

  startEditing() {
    if (!this.readOnly) {
      this.editing = true;
      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          this.editNumberRef.nativeElement.focus();
          this.editNumberRef.nativeElement.select();
        });
      });
    }
  }

  inputBlur(event: Event) {
    if (event instanceof FocusEvent && event.relatedTarget != null) {
      if (
        (event.relatedTarget as HTMLElement).closest('.parent') ==
        this.parentRef.nativeElement
      ) {
        return; // focus is still within our component, continue editing
      }
    }
    this.stopEditing('_notmodified');
  }

  stopEditing(selectedUnits: string | null | '_notmodified') {
    if (!this.editing) {
      return;
    }
    this.editing = false;
    const oldValue = this._value?.value;
    const oldUnits = this._value?.unit;
    let newValue = this.editNumberRef.nativeElement.valueAsNumber;
    newValue = isNaN(newValue) ? null : newValue;
    if (
      newValue != null &&
      oldValue != null &&
      Math.abs(newValue - oldValue) <= 0.0005
    ) {
      newValue = oldValue; // avoid minor changes due to rounding
    }
    const newUnits = selectedUnits == '_notmodified' ? oldUnits : selectedUnits;
    const valueChanged = newValue != oldValue;
    const unitsChanged = newValue != null && newUnits != oldUnits;
    console.log(
      `oldValue=${oldValue}, newValue=${newValue}, oldUnits=${oldUnits}, newUnits=${newUnits}, valueChanged=${valueChanged}, unitsChanged=${unitsChanged}, go=${valueChanged || unitsChanged}`,
    );
    if (valueChanged || unitsChanged) {
      this.onChange(
        newValue != null
          ? { ...this._value, value: newValue, unit: newUnits }
          : null,
      );
    }
  }

  cancelEditing() {
    this.editing = false;
    // reset value in the input
    this.editNumberRef.nativeElement.value = this.decimalPipe.transform(
      this._value?.value,
      '1.0-3',
    );
  }
}
