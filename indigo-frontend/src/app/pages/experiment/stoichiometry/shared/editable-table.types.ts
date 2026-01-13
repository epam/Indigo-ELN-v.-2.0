import { DictionaryItemRef } from '@core/types/entities/dictionary.i';

export enum ColumnInputType {
  TEXT = 'text',
  NUMBER = 'number',
  SELECT = 'select',
  CHECKBOX = 'checkbox',
  UNIT_INPUT = 'unit-input',
  MULTI_SELECT = 'multi-select',
  BUTTON = 'button',
}

export interface UnitFieldValue {
  value: number | string;
  unit: string;
}

export type FieldValue = string | null | boolean | UnitFieldValue | DictionaryItemRef[];

export interface UnitInputChange {
  value?: number | null;
  unit?: string | null;
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
  editable?: (row: TRow) => boolean;
  onSave?: (row: TRow, payload?: unknown) => void;
  options?: ColumnOption[] | DictionaryItemRef[];
}
