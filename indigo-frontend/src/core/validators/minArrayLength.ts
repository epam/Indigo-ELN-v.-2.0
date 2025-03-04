import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function minArrayLength(minLength: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (Array.isArray(control.value) && control.value.length < minLength) {
      return {
        minArrayLength: {
          requiredLength: minLength,
          actualLength: control.value.length,
        },
      };
    }
    return null;
  };
}
