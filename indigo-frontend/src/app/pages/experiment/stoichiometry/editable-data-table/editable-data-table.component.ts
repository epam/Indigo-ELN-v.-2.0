import { Component, input, output } from '@angular/core';
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
import { MatSelect, MatOption, MatSelectTrigger } from '@angular/material/select';
import { MatInput } from '@angular/material/input';
import { MatDivider } from '@angular/material/divider';
import { FormsModule } from '@angular/forms';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import {
  ColumnInputType,
  ColumnConfig,
  FieldValue,
  UnitFieldValue,
} from '../shared/editable-table.types';

@Component({
  selector: 'eln-editable-data-table',
  templateUrl: './editable-data-table.component.html',
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
    ButtonComponent,
  ],
})
export class EditableDataTableComponent<TRow = unknown> {
  readonly ColumnInputType = ColumnInputType;

  title = input.required<string>();
  dataSource = input.required<TRow[]>();
  columns = input.required<ColumnConfig<TRow>[]>();
  displayedColumns = input.required<string[]>();
  emptyMessage = input<string>('No data available');
  showAddButton = input<boolean>(true);

  addRow = output<void>();

  compareDictionaryItems = (a?: DictionaryItemRef | null, b?: DictionaryItemRef | null) =>
    !!a && !!b ? a.id === b.id : a === b;

  getInputType(columnId: string): ColumnInputType {
    const column = this.columns().find(c => c.id === columnId);
    return column?.type ?? ColumnInputType.TEXT;
  }

  toUnitField(fieldValue: FieldValue): UnitFieldValue | null {
    return fieldValue && typeof fieldValue !== 'string' && typeof fieldValue !== 'boolean' && !Array.isArray(fieldValue)
      ? fieldValue
      : null;
  }

  onAddRow() {
    this.addRow.emit();
  }
}
