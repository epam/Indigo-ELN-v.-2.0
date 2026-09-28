import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { Type } from '@angular/core';
import { EnteredValue } from '@core/types/entities/values.i';
import { Observable } from 'rxjs';

export enum ColumnInputType {
  TEXT = 'text',
  HTML = 'html',
  NUMBER = 'number',
  SELECT = 'select',
  RADIO = 'radio',
  UNIT_INPUT = 'unit-input',
  MULTI_SELECT = 'multi-select',
  BUTTON = 'button',
  ICON = 'icon',
}

export type FieldValue = string | null | boolean | EnteredValue<unknown> | DictionaryItemRef | DictionaryItemRef[];

export interface UnitInputChange {
  value?: string | null;
  unit?: string | null;
  previous?: { value?: string | null; unit?: string | null };
}

export interface ColumnOption {
  id: string;
  name: string;
}

export interface ColumnConfig<TRow = unknown, TValue = FieldValue> {
  id: string;
  header: string;
  type: ColumnInputType;
  field: (row: TRow) => TValue;
  classes?: (row: TRow) => string[];
  editable?: (row: TRow) => boolean;
  required?: (row: TRow) => boolean;
  onSave?: (row: TRow, payload?: TValue | null) => void;
  options?: ColumnOption[] | DictionaryItemRef[] | Observable<ColumnOption[]> | Observable<DictionaryItemRef[]>;
  tooltip?: (row: TRow) => string;
  iconClasses?: (row: TRow) => string[];
}

export interface ExpandableConfig<TRow = unknown> {
  enabled: boolean;
  component: Type<any>;
  getRowData: (row: TRow) => any;
}
