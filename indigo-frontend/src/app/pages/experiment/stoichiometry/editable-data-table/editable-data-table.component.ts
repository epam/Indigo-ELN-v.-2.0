import { Component, inject, input, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderCellDef,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable,
} from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatOption, MatSelect, MatSelectTrigger } from '@angular/material/select';
import { MatInput } from '@angular/material/input';
import { MatDivider } from '@angular/material/divider';
import { FormsModule } from '@angular/forms';
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import {
  ColumnConfig,
  ColumnInputType,
  ExpandableConfig,
  FieldValue,
  UnitFieldValue,
} from '../shared/editable-table.types';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { MatIconButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { EnteredValue } from '@core/types/entities/values.i';

@Component({
  selector: 'eln-editable-data-table',
  templateUrl: './editable-data-table.component.html',
  styleUrl: './editable-data-table.component.scss',
  imports: [
    MatTable,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderCellDef,
    MatCell,
    MatCellDef,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatIconModule,
    MatSelect,
    MatSelectTrigger,
    MatOption,
    MatInput,
    MatDivider,
    FormsModule,
    CommonModule,
    MatIconButton,
    MatTooltip,
  ],
})
export class EditableDataTableComponent<TRow = unknown> {
  experimentDetailService = inject(ExperimentDetailService);
  readonly experimentModel = this.experimentDetailService.experimentModel;

  readonly ColumnInputType = ColumnInputType;
  @ViewChild(MatTable) table?: MatTable<TRow>;

  dataSource = input.required<TRow[] | null>();
  columns = input.required<ColumnConfig<TRow, FieldValue>[]>();
  displayedColumns = input.required<string[]>();
  emptyMessage = input<string>('No data available');
  loadingMessage = input<string>('Loading...');
  expandableConfig = input<ExpandableConfig<TRow> | null>(null);

  expandedRows = signal<Set<TRow>>(new Set());

  compareDictionaryItems = (a?: DictionaryItemRef | null, b?: DictionaryItemRef | null) =>
    !!a && !!b ? a.id === b.id : a === b;

  toUnitField(fieldValue: FieldValue): UnitFieldValue | null {
    return fieldValue as UnitFieldValue;
  }

  toggleRow(row: TRow) {
    const expanded = this.expandedRows();
    if (expanded.has(row)) {
      expanded.delete(row);
    } else {
      expanded.add(row);
    }
    this.expandedRows.set(new Set(expanded));
    this.table?.renderRows();
  }

  isRowExpanded(row: TRow): boolean {
    return this.expandedRows().has(row);
  }

  getDisplayedColumnsWithExpand(): string[] {
    const config = this.expandableConfig();
    if (config?.enabled) {
      return ['expand', ...this.displayedColumns()];
    }
    return this.displayedColumns();
  }
  detailRow = (_index: number, row: TRow) => this.isRowExpanded(row);

  callSave(column: ColumnConfig<TRow, FieldValue>, row: TRow, newValue: FieldValue): void {
    const oldValue = column.field(row);
    if (oldValue !== newValue) {
      column?.onSave(row, newValue || null);
    }
  }

  callSaveEV(
    column: ColumnConfig<TRow, unknown>,
    row: TRow,
    selectedValue: string,
    selectedUnit: unknown,
    linkedInput: HTMLInputElement | null,
    linkedUnitSelect: MatSelect | null,
  ): void {
    const columnEV = column as ColumnConfig<TRow, EnteredValue<unknown>>;
    const oldValue = columnEV.field(row);
    const newValue = {
      value: selectedValue,
      unit: selectedUnit,
    } as EnteredValue<unknown>;
    const oldSet = this.isFullySet(oldValue),
      newSet = this.isFullySet(newValue);
    if (newSet && oldSet) {
      // update existing value
      if (newValue.value !== oldValue?.value || newValue.unit !== oldValue.unit) {
        columnEV?.onSave(row, newValue);
      }
    } else if (newSet) {
      // set new value
      columnEV?.onSave(row, newValue);
    } else if (oldSet) {
      // remove old value
      columnEV?.onSave(row, null);
    } else if (linkedUnitSelect != null && newValue.value != null && newValue.unit == null) {
      // user entered number only; expand units combobox automatically
      linkedUnitSelect.open();
      linkedUnitSelect.focus();
    } else if (linkedInput != null && newValue.unit == null) {
      // user didn't select unit; reset numeric input
      linkedInput.value = '';
    }
  }

  private isFullySet(value: EnteredValue<unknown> | null): boolean {
    return value != null && value.value != null && value.value !== '' && value.unit != null && value.unit !== '';
  }
}
