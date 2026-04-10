import {
  Component,
  inject,
  input,
  output,
  signal,
  ViewChild,
} from '@angular/core';
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
import {
  MatOption,
  MatSelect,
  MatSelectTrigger,
} from '@angular/material/select';
import { MatInput } from '@angular/material/input';
import { MatDivider } from '@angular/material/divider';
import { FormsModule } from '@angular/forms';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import {
  ColumnConfig,
  ColumnInputType,
  ExpandableConfig,
  FieldValue,
  UnitFieldValue,
} from '../shared/editable-table.types';
import { DropdownMenuItem } from '@/core/components/common/dropdown-menu/dropdown-menu.i';
import { SelectComponent } from '@/core/components/common/select/select.component';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { SIGNIFICANT_FIGURES } from '../significant-figures.constants';
import { catchError } from 'rxjs/internal/operators/catchError';
import { EMPTY } from 'rxjs/internal/observable/empty';

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
    ButtonComponent,
    SelectComponent,
  ],
})
export class EditableDataTableComponent<TRow = unknown> {
  experimentDetailService = inject(ExperimentDetailService);
  readonly experimentModel = this.experimentDetailService.experimentModel;

  readonly ColumnInputType = ColumnInputType;
  @ViewChild(MatTable) table?: MatTable<TRow>;

  title = input.required<string>();
  dataSource = input.required<TRow[] | null>();
  columns = input.required<ColumnConfig<TRow>[]>();
  displayedColumns = input.required<string[]>();
  emptyMessage = input<string>('No data available');
  loadingMessage = input<string>('Loading...');
  showAddButton = input<boolean>(true);
  showSignificantFigures = input<boolean>(false);
  expandableConfig = input<ExpandableConfig<TRow> | null>(null);
  readonly items = signal<DropdownMenuItem[]>([...SIGNIFICANT_FIGURES]);

  addRow = output<void>();

  expandedRows = signal<Set<TRow>>(new Set());

  compareDictionaryItems = (
    a?: DictionaryItemRef | null,
    b?: DictionaryItemRef | null,
  ) => (!!a && !!b ? a.id === b.id : a === b);

  getInputType(columnId: string): ColumnInputType {
    const column = this.columns().find((c) => c.id === columnId);
    return column?.type ?? ColumnInputType.TEXT;
  }

  toUnitField(fieldValue: FieldValue): UnitFieldValue | null {
    return fieldValue &&
      typeof fieldValue !== 'string' &&
      typeof fieldValue !== 'boolean' &&
      !Array.isArray(fieldValue)
      ? fieldValue
      : null;
  }

  onAddRow() {
    this.addRow.emit();
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

  onSignificantFiguresChange(value: string | string[] | null): void {
    const parsedValue = this.parseSignificantFigure(value);
    if (parsedValue === null) return;

    this.experimentDetailService
      .updateDataModel({
        type: 'SetExperimentSignificantFigures',
        significantFigures: parsedValue,
      })
      .pipe(catchError(() => EMPTY))
      .subscribe();
  }

  detailRow = (_index: number, row: TRow) => this.isRowExpanded(row);

  private parseSignificantFigure(
    value: string | string[] | null,
  ): number | null {
    if (!value || Array.isArray(value)) return null;

    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? null : parsed;
  }
}
