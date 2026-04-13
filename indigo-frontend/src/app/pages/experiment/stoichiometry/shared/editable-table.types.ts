import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { Type } from '@angular/core';

export enum ColumnInputType {
  TEXT = 'text',
  NUMBER = 'number',
  SELECT = 'select',
  CHECKBOX = 'checkbox',
  UNIT_INPUT = 'unit-input',
  MULTI_SELECT = 'multi-select',
  BUTTON = 'button',
  ICON = 'icon',
}

export interface UnitFieldValue {
  value: number | string;
  unit: string;
}

export type FieldValue =
  | string
  | null
  | boolean
  | UnitFieldValue
  | DictionaryItemRef[];

export interface UnitInputChange {
  value?: string | null;
  unit?: string | null;
  previous?: { value?: string | null; unit?: string | null };
}

export interface ColumnOption {
  id: string;
  name: string;
}

export interface ColumnConfig<TRow = unknown> {
  id: string;
  header: string;
  type: ColumnInputType;
  field: (row: TRow) => FieldValue;
  classes?: (row: TRow) => string[];
  editable?: (row: TRow) => boolean;
  onSave?: (row: TRow, payload?: unknown) => void;
  options?: ColumnOption[] | DictionaryItemRef[];
  tooltip?: (row: TRow) => string;
  iconClasses?: (row: TRow) => string[];
}

export interface ExpandableConfig<TRow = unknown> {
  enabled: boolean;
  component: Type<any>;
  getRowData: (row: TRow) => any;
}
