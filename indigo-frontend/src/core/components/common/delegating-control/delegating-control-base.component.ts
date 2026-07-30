import { AbstractControl, ControlValueAccessor } from '@angular/forms';
import { AfterViewInit, Directive } from '@angular/core';
import { setEnabled } from '@core/utils/search.util';

// Abstract base for form controls; uses @Directive() (not @Injectable()) so Angular
// invokes the ngAfterViewInit lifecycle hook on the concrete component subclasses.
@Directive()
export abstract class DelegatingControlBase<T> implements ControlValueAccessor, AfterViewInit {
  private change: ((arg0: T) => void) | null;
  private touched: (() => void) | null;

  private viewInitialized = false;
  delayedInitializations: (() => void)[] = [];

  ngAfterViewInit(): void {
    this.viewInitialized = true;
    this.delayedInitializations.forEach((operation) => operation());
  }

  abstract setValue(obj: T | null): void;

  writeValue(obj: T | null): void {
    this.doAfterInitialization(() => this.setValue(obj));
  }

  registerOnChange(fn: (arg0: T) => void): void {
    this.change = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.touched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.doAfterInitialization(() => {
      this.getControlsToDisable().forEach((c) => {
        setEnabled(c, !isDisabled, false);
      });
    });
  }

  protected abstract getControlsToDisable(): AbstractControl[];

  protected triggerChange(value: T | null) {
    this.change?.(value);
  }

  protected triggerTouched() {
    this.touched?.();
  }

  private doAfterInitialization(fn: () => void) {
    if (this.viewInitialized) {
      fn();
    } else {
      this.delayedInitializations.push(fn);
    }
  }
}
