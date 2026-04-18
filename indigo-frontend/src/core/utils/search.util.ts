import {
  NumericSearch,
  NumericSearchTypeNames,
  TextSearch,
  TextSearchTypeNames,
} from '@core/types/entities/experiments/search.i';
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { UserMetadata } from '@core/types/entities/user.i';
import { AbstractControl } from '@angular/forms';

export function textSearchSummary(name: string, search: TextSearch | null): string | null {
  if (search?.type === 'between') {
    return `<b>${name}</b>&ensp;${TextSearchTypeNames[search.type]}&ensp;${search.from}&nbsp;and&nbsp;${search.to}`;
  } else if (search != null) {
    return `<b>${name}</b>&ensp;${TextSearchTypeNames[search.type]}&ensp;${search.value}`;
  }
  return null;
}

export function numericSearchSummary(name: string, search: NumericSearch | null): string | null {
  if (search != null) {
    return `<b>${name}</b>&ensp;${NumericSearchTypeNames[search.type]}&ensp;${search.value}`;
  }
  return null;
}

export function dictionarySearchSummary(
  name: string,
  value: DictionaryItemRef | DictionaryItemRef[] | UserMetadata | UserMetadata[] | null,
): string | null {
  if (value != null) {
    const array = Array.isArray(value) ? value : [value];
    if (array.length) {
      return `<b>${name}</b>&ensp;is&ensp;${array.map(referenceDisplayName).join('&ensp;or&ensp;')}`;
    }
  }
  return null;
}

export function enumSearchSummary<T extends string>(
  name: string,
  value: T | T[] | null,
  enumNames: Record<T, string>,
): string | null {
  if (value != null) {
    const array = Array.isArray(value) ? value : [value];
    if (array.length) {
      return `<b>${name}</b>&ensp;is&ensp;${array.map((v) => enumNames[v]).join('&ensp;or&ensp;')}`;
    }
  }
  return null;
}

export function setEnabled(control: AbstractControl, isEnabled: boolean, emitEvent: boolean): void {
  if (isEnabled) {
    control.enable({ emitEvent });
  } else {
    control.disable({ emitEvent });
  }
}

export function isFormValueNotEmpty(value: any): boolean {
  if (value === null || value === undefined) {
    return false;
  }
  if (typeof value === 'string' || value instanceof String) {
    return value.trim() !== '';
  }
  if (Array.isArray(value)) {
    return value.length > 0;
  }
  return true;
}

function referenceDisplayName(ref: any): string {
  return ref.username || ref.name;
}
