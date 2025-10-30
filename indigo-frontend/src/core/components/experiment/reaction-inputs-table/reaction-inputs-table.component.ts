import { Component, computed, inject, input } from '@angular/core';
import {
  Reaction,
  ReactionInput,
} from '@core/types/entities/experiments/experiment.i';
import {
  DensityUnit,
  MolarityUnit,
  MolUnit,
  ReactionRole,
  VolumeUnit,
  WeightUnit,
} from '@core/types/entities/experiments/experiment-shared.i';
import { EnteredValue } from '@core/types/entities/values.i';
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
import { ExperimentService } from '@core/services/experiment/experiment.service';

@Component({
  selector: 'eln-reaction-inputs-table',
  templateUrl: './reaction-inputs-table.component.html',
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
export class ReactionInputsTableComponent {
  private experimentService = inject(ExperimentService);

  reaction = input<Reaction | null>(null);
  dataSource = computed(() => this.reaction()?.inputs ?? []);

  columns = [
    {
      id: 'compoundId',
      header: 'Compound ID',
      type: 'text',
      field: (row: ReactionInput) => row.compound.strCode || row.anchor,
    },
    {
      id: 'casNumber',
      header: 'CAS Number',
      type: 'text',
      field: () => null,
    },
    {
      id: 'chemicalName',
      header: 'Chemical Name',
      type: 'text',
      field: () => null,
    },
    {
      id: 'nbkBatch',
      header: 'NBK Batch #',
      type: 'text',
      field: () => null,
    },
    {
      id: 'molWeight',
      header: 'Mol. Weight',
      type: 'text',
      field: (row: ReactionInput) =>
        row.compound.molWeight?.value
          ? `${row.compound.molWeight.value} ${row.compound.molWeight?.unit}`
          : null,
    },
    {
      id: 'weight',
      header: 'Weight',
      type: 'text',
      field: (row: ReactionInput) =>
        row.samples[0]?.weight?.value
          ? `${row.samples[0].weight.value} ${row.samples[0].weight.unit}`
          : null,
    },
    {
      id: 'volume',
      header: 'Volume',
      type: 'text',
      field: (row: ReactionInput) =>
        row.samples[0]?.volume?.value
          ? `${row.samples[0].volume.value} ${row.samples[0].volume.unit}`
          : null,
    },
    {
      id: 'mol',
      header: 'Mol',
      type: 'text',
      field: (row: ReactionInput) =>
        row.mol?.value ? `${row.mol.value} ${row.mol.unit}` : null,
    },
    {
      id: 'eq',
      header: 'EQ',
      type: 'text',
      field: (row: ReactionInput) => row.eq?.value?.toString(),
    },
    {
      id: 'limiting',
      header: 'Limiting',
      type: 'text',
      field: (row: ReactionInput) => (row.limiting ? '✓' : null), //
    },
    {
      id: 'rxnRole',
      header: 'Rxn Role',
      type: 'text',
      field: (row: ReactionInput) => row.role || 'REACTANT',
    },
    {
      id: 'density',
      header: 'Density',
      type: 'text',
      field: (row: ReactionInput) =>
        row.samples[0]?.density?.value
          ? `${row.samples[0].density.value} ${row.samples[0].density.unit}`
          : null,
    },
    {
      id: 'molarity',
      header: 'Molarity',
      type: 'text',
      field: (row: ReactionInput) =>
        row.samples[0]?.molarity?.value
          ? `${row.samples[0].molarity.value} ${row.samples[0].molarity.unit}`
          : null,
    },
    {
      id: 'purity',
      header: 'Purity',
      type: 'text',
      field: (row: ReactionInput) => row.samples[0]?.purity?.value?.toString(),
    },
    {
      id: 'molFormula',
      header: 'Mol Formula',
      type: 'text',
      field: (row: ReactionInput) => row.compound.formula,
    },
    {
      id: 'saltCode',
      header: 'Salt Code',
      type: 'text',
      field: (row: ReactionInput) => row.compound.saltCode?.code,
    },
    {
      id: 'saltEQ',
      header: 'Salt EQ',
      type: 'text',
      field: (row: ReactionInput) => row.compound.saltEQ?.toString(),
    },
    {
      id: 'hazardComments',
      header: 'Hazard Comments',
      type: 'text',
      field: (row: ReactionInput) =>
        row.samples[0]?.healthHazard?.map((h) => h.name).join(', '),
    },
    {
      id: 'comments',
      header: 'Comments',
      type: 'text',
      field: () => null,
    },
  ];

  displayedColumns = this.columns.map((col) => col.id);

  setRole(row: ReactionInput, value: ReactionRole) {
    this.experimentService.mutateModel({
      type: 'SetInputRowRole',
      anchor: row.anchor,
      role: value,
    });
  }

  setWeight(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputWeight',
      anchor: row.samples[0].anchor,
      weight: value?.value,
      unit: value?.unit as WeightUnit,
    });
  }

  setVolume(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputVolume',
      anchor: row.samples[0].anchor,
      volume: value?.value,
      unit: value?.unit as VolumeUnit,
    });
  }

  setMol(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputMol',
      anchor: row.anchor,
      mol: value?.value,
      unit: value?.unit as MolUnit,
    });
  }

  setEQ(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputRowEQ',
      anchor: row.anchor,
      eq: value?.value,
    });
  }

  setDensity(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputDensity',
      anchor: row.samples[0].anchor,
      density: value?.value,
      unit: value?.unit as DensityUnit,
    });
  }

  setMolarity(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputMolarity',
      anchor: row.samples[0].anchor,
      molarity: value?.value,
      unit: value?.unit as MolarityUnit,
    });
  }

  setPurity(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputPurity',
      anchor: row.samples[0].anchor,
      purity: value?.value,
    });
  }
}
