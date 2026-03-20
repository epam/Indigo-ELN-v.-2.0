import { Component, computed, input } from '@angular/core';
import {
  Reaction,
  ReactionOutput,
} from '@core/types/entities/experiments/experiment.i';
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

@Component({
  selector: 'eln-reaction-products-table',
  templateUrl: './reaction-products-table.component.html',
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
  ],
})
export class ReactionProductsTableComponent {
  reaction = input<Reaction | null>(null);
  dataSource = computed(() => this.reaction()?.outputs);

  columns = [
    {
      id: 'chemicalName',
      header: 'Chemical Name',
      type: 'text',
      field: (_row: ReactionOutput, index: number) => `P${index}`,
    },
    {
      id: 'molFormula',
      header: 'Mol Formula',
      type: 'text',
      field: (row: ReactionOutput) => row.compound?.formula,
    },
    {
      id: 'molWeight',
      header: 'Mol Weight',
      type: 'text',
      field: (row: ReactionOutput) =>
        row.compound?.molWeight?.value
          ? `${row.compound.molWeight.value} ${row.compound.molWeight.unit}`
          : null,
    },
    {
      id: 'exactMass',
      header: 'Exact Mass',
      type: 'text',
      field: () => null, // Not available in current model
    },
    {
      id: 'theoWeight',
      header: 'Theo Weight',
      type: 'text',
      field: (row: ReactionOutput) =>
        row.theoWeight?.value
          ? `${row.theoWeight.value} ${row.theoWeight.unit}`
          : null,
    },
    {
      id: 'theoMoles',
      header: 'Theo Moles',
      type: 'text',
      field: (row: ReactionOutput) =>
        row.theoMol?.value ? `${row.theoMol.value} ${row.theoMol.unit}` : null,
    },
    {
      id: 'saltCode',
      header: 'Salt Code',
      type: 'text',
      field: (row: ReactionOutput) => row.compound?.saltCode?.code || '00', // Default value (00 - Parent structure)
    },
    {
      id: 'saltEQ',
      header: 'Salt EQ',
      type: 'text',
      field: (row: ReactionOutput) => row.compound?.saltEQ?.toString(),
    },
    {
      id: 'hazardComments',
      header: 'Hazard Comments',
      type: 'text',
      field: (row: ReactionOutput) =>
        row.samples[0]?.healthHazards?.map((h) => h.name).join(', '), // Data retrieved from External database
    },
    {
      id: 'eq',
      header: 'EQ',
      type: 'text',
      field: (row: ReactionOutput) => row.eq?.value?.toString() || '1', // "1" by default
    },
  ];

  displayedColumns = this.columns.map((col) => col.id);
}
