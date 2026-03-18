import { Component, ElementRef, inject, Input, ViewChild } from '@angular/core';
import {
  EnteredValue,
  EnteredValueSource,
  MeasurementUnit,
} from '@core/types/entities/values.i';
import { DecimalPipe, NgClass } from '@angular/common';
import { MatOption, MatSelect } from '@angular/material/select';

@Component({
  selector: 'eln-entered-value',
  templateUrl: './entered-value.component.html',
  styleUrl: './entered-value.component.scss',
  providers: [DecimalPipe],
  imports: [DecimalPipe, MatSelect, MatOption, NgClass],
})
export class EnteredValueComponent<U> {
  private _value: EnteredValue<U> | null;
  private _units: MeasurementUnit[] | null;

  @Input() showUnits = true;

  @Input() onChange: ((newValue: EnteredValue<U>) => void) | null = null;

  @Input() readOnly = false;

  decimalPipe = inject(DecimalPipe);

  @ViewChild('editNumber') editNumberRef!: ElementRef<HTMLInputElement>;

  @ViewChild('editUnits') editUnitsRef!: MatSelect;

  @ViewChild('parent') parentRef!: ElementRef<HTMLElement>;

  unitDisplayName: string | null = null;

  editing = false;

  recalculated = false;

  @Input()
  set value(newValue: EnteredValue<U> | null) {
    const oldValue = this._value;
    this._value = newValue;
    this.recalculated = false;
    this.updateUnitDisplayName();
    if (
      oldValue !== undefined &&
      newValue?.source === EnteredValueSource.CALCULATED_FROM_LAST_ENTERED
    ) {
      requestAnimationFrame(() => {
        this.recalculated = true;
      });
    }
  }

  get value(): EnteredValue<U> | null {
    return this._value;
  }

  @Input()
  set units(units: MeasurementUnit[]) {
    this._units = units;
    this.updateUnitDisplayName();
  }

  get units(): MeasurementUnit[] | null {
    return this._units;
  }

  startEditing() {
    if (!this.readOnly) {
      this.editing = true;
      requestAnimationFrame(() => {
        this.editNumberRef.nativeElement.focus();
        this.editNumberRef.nativeElement.select();
      });
    }
  }

  inputBlur(event: Event) {
    if (event instanceof FocusEvent && event.relatedTarget != null) {
      if (
        (event.relatedTarget as HTMLElement).closest('.x-parent') ==
        this.parentRef.nativeElement
      ) {
        return; // focus is still within our component, continue editing
      }
    }
    this.stopEditing('_notmodified');
  }

  stopEditing(selectedUnits: U | null | '_notmodified') {
    if (!this.editing) {
      return;
    }
    this.editing = false;
    const oldValueStr = this._value?.value;
    const oldValue = oldValueStr != null ? parseFloat(oldValueStr) : null;
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
    if (valueChanged || unitsChanged) {
      this.onChange(
        newValue != null
          ? { ...this._value, value: newValue.toString(), unit: newUnits }
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

  private updateUnitDisplayName(): void {
    this.unitDisplayName = null;
    if (this._value != null && this.units != null) {
      const foundUnit = this.units.find((x) => this._value.unit === x.value);
      this.unitDisplayName = foundUnit?.displayName;
    }
  }
}
