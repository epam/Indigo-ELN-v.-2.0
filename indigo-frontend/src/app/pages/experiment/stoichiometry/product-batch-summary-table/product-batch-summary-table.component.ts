import { Component, computed, inject, input } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Reaction,
  ReactionOutput,
  ReactionOutputSample,
  ExperimentModel,
} from '@core/types/entities/experiments/experiment.i';
import {
  VolumeUnit,
  WeightUnit,
  MolWeightUnit,
  MolUnit,
  ReactionOutputType,
  SampleRegistrationStatus,
} from '@core/types/entities/experiments/experiment-shared.i';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ExperimentModelService } from '@core/services/experiment/experiment-model.service';
import { EditableDataTableComponent } from '../editable-data-table/editable-data-table.component';
import {
  ColumnInputType,
  ColumnConfig,
  UnitInputChange,
  ColumnOption,
} from '../shared/editable-table.types';

interface OutputSampleRow {
  output: ReactionOutput;
  sample: ReactionOutputSample;
}

@Component({
  selector: 'eln-product-batch-summary-table',
  templateUrl: './product-batch-summary-table.component.html',
  imports: [
    MatSnackBarModule,
    EditableDataTableComponent,
  ],
})
export class ProductBatchSummaryTableComponent {
  private experimentModelService = inject(ExperimentModelService);
  private snackBar = inject(MatSnackBar);

  reaction = input<Reaction | null>(null);
  experimentId = input<string | null>(null);

  // TODO: Remove mock data once backend provides real output samples
  private mockOutputSamples = computed<OutputSampleRow[]>(() => {
    // Create mock samples for demonstration (independent of real data)
    const mockOutput1 = {
      anchor: 'mock-output-1',
      chemicalName: 'P1',
      compound: {
        strCode: 'P1',
        formula: 'C10H12O2',
        molWeight: { value: 164.2, unit: MolWeightUnit.G_PER_MOL },
      },
      type: ReactionOutputType.INTERMEDIATE,
      eq: { value: 1 },
      samples: [],
    } as ReactionOutput;

    const mockOutput2 = {
      anchor: 'mock-output-2',
      chemicalName: 'P2',
      compound: {
        strCode: 'P2',
        formula: 'C8H10O',
        molWeight: { value: 122.16, unit: MolWeightUnit.G_PER_MOL },
      },
      type: ReactionOutputType.BY_PRODUCT,
      eq: { value: 1 },
      samples: [],
    } as ReactionOutput;

    return [
      {
        output: mockOutput1,
        sample: {
          anchor: 'mock-sample-1',
          nbkBatchNumber: '001',
          actualWeight: { value: 2.46, unit: WeightUnit.MG },
          volume: undefined,
          actualMol: undefined,
          yield: { value: 0.01 },
          purity: { value: 0.04 },
          registrationStatus: undefined,
          healthHazards: [],
          handlingPrecautions: [],
          storageInstructions: [],
          compoundProtection: [],
          solubilityInSolvents: [],
          residualSolvents: [],
          purityCalculations: [],
          precursorReactantIds: [],
        } as ReactionOutputSample,
      },
      {
        output: mockOutput2,
        sample: {
          anchor: 'mock-sample-2',
          nbkBatchNumber: '002',
          actualWeight: { value: 2.46, unit: WeightUnit.MG },
          volume: undefined,
          actualMol: undefined,
          yield: { value: 0.01 },
          purity: { value: 0.04 },
          registrationStatus: undefined,
          healthHazards: [],
          handlingPrecautions: [],
          storageInstructions: [],
          compoundProtection: [],
          solubilityInSolvents: [],
          residualSolvents: [],
          purityCalculations: [],
          precursorReactantIds: [],
        } as ReactionOutputSample,
      },
    ];
  });

  dataSource = computed(() => {
    // TODO: Replace mock data with real data from reaction outputs
    const mockData = this.mockOutputSamples();
    if (mockData.length > 0) {
      return mockData;
    }

    // Fallback to real data when available
    const outputs = this.reaction()?.outputs ?? [];
    return outputs.flatMap(output =>
      output.samples.map(sample => ({ output, sample }))
    );
  });

  readonly columns = computed<ColumnConfig<OutputSampleRow>[]>(() => [
    {
      id: 'batchId',
      header: 'Batch ID',
      type: ColumnInputType.TEXT,
      field: (row: OutputSampleRow) => row.sample.nbkBatchNumber ?? null,
      editable: () => false,
    },
    {
      id: 'chemicalName',
      header: 'Chemical Name',
      type: ColumnInputType.TEXT,
      field: (row: OutputSampleRow) => row.output.chemicalName ?? null,
      editable: () => false,
    },
    {
      id: 'reactionRole',
      header: 'Reaction Role',
      type: ColumnInputType.TEXT,
      field: (row: OutputSampleRow) => this.formatReactionOutputType(row.output.type),
      editable: () => false,
    },
    {
      id: 'regStatus',
      header: 'Reg Status',
      type: ColumnInputType.TEXT,
      field: (row: OutputSampleRow) => this.formatRegistrationStatus(row.sample.registrationStatus),
      editable: () => false,
    },
    {
      id: 'totalWeight',
      header: 'Total Weight',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: OutputSampleRow) =>
        row.sample.actualWeight?.value
          ? { value: row.sample.actualWeight.value, unit: row.sample.actualWeight.unit }
          : null,
      onSave: (row: OutputSampleRow) => {
        // TODO: Implement mutation for setting output sample actual weight
        console.log('TODO: Set output actual weight', row.sample.anchor);
        this.snackBar.open('Weight update not yet implemented', 'Close', { duration: 3000 });
      },
      options: Object.values(WeightUnit).map(unit => ({ id: unit, name: unit })) as ColumnOption[],
    },
    {
      id: 'totalVolume',
      header: 'Total Volume',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: OutputSampleRow) =>
        row.sample.volume?.value
          ? { value: row.sample.volume.value, unit: row.sample.volume.unit }
          : null,
      onSave: (row: OutputSampleRow) => {
        // TODO: Implement mutation for setting output sample volume
        console.log('TODO: Set output volume', row.sample.anchor);
        this.snackBar.open('Volume update not yet implemented', 'Close', { duration: 3000 });
      },
      options: Object.values(VolumeUnit).map(unit => ({ id: unit, name: unit })) as ColumnOption[],
    },
    {
      id: 'totalMoles',
      header: 'Total Moles',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: OutputSampleRow) =>
        row.sample.actualMol?.value
          ? { value: row.sample.actualMol.value, unit: row.sample.actualMol.unit }
          : null,
      onSave: (row: OutputSampleRow) => {
        // TODO: Implement mutation for setting output sample moles
        console.log('TODO: Set output moles', row.sample.anchor);
        this.snackBar.open('Moles update not yet implemented', 'Close', { duration: 3000 });
      },
      options: Object.values(MolUnit).map(unit => ({ id: unit, name: unit })) as ColumnOption[],
    },
    {
      id: 'yield',
      header: 'Yield',
      type: ColumnInputType.NUMBER,
      field: (row: OutputSampleRow) => row.sample.yield?.value?.toString() ?? null,
      editable: () => false,
    },
    {
      id: 'purity',
      header: 'Purity',
      type: ColumnInputType.NUMBER,
      field: (row: OutputSampleRow) => row.sample.purity?.value?.toString() ?? null,
      onSave: (row: OutputSampleRow, event: Event) => {
        // TODO: Implement mutation for setting output sample purity
        const value = +(event.target as HTMLInputElement).value;
        console.log('TODO: Set output purity', row.sample.anchor, value);
        
        // Placeholder for now
        this.snackBar.open('Purity update not yet implemented', 'Close', { duration: 3000 });
      },
    },
    {
      id: 'syncWithProducts',
      header: 'Sync with Products',
      type: ColumnInputType.BUTTON,
      field: () => '🔄',
      onSave: (row: OutputSampleRow) => {
        // TODO: Implement sync with products functionality
        console.log('TODO: Sync with products', row.sample.anchor);
        this.snackBar.open('Sync functionality not yet implemented', 'Close', { duration: 3000 });
      },
    },
  ]);

  displayedColumns = computed(() => this.columns().map((col) => col.id));

  private formatReactionOutputType(type: ReactionOutputType): string {
    const typeMap: Record<ReactionOutputType, string> = {
      [ReactionOutputType.FINAL]: 'Final',
      [ReactionOutputType.BY_PRODUCT]: 'By Product',
      [ReactionOutputType.INTERMEDIATE]: 'Intermediate',
    };
    return typeMap[type] ?? type;
  }

  private formatRegistrationStatus(status?: SampleRegistrationStatus): string {
    if (!status) return 'None';
    
    const statusMap: Record<SampleRegistrationStatus, string> = {
      [SampleRegistrationStatus.IN_PROGRESS]: 'In Progress',
      [SampleRegistrationStatus.FAILED]: 'Failed',
      [SampleRegistrationStatus.REGISTERED]: 'Registered',
    };
    return statusMap[status] ?? status;
  }

  private applyUnitInputChange(
    change: UnitInputChange,
    mutator: (value: number | undefined, unit: string | undefined) => Observable<ExperimentModel>,
  ) {
    // Only proceed if both value and unit are present
    if (change.value === null || change.value === undefined ||
      change.unit === null || change.unit === undefined) {
      return;
    }

    const previousState = structuredClone(this.experimentModelService.experimentModel());
    mutator(change.value, change.unit).subscribe({
      error: (error) => this.handleUpdateError(error, previousState),
    });
  }

  private handleUpdateError(error: Error, previousState: ExperimentModel | null) {
    console.error('Error updating data:', error);
    if (previousState) {
      console.log('Reverting to previous state due to error');
      this.experimentModelService.setExperimentModel(previousState);
    }
    this.snackBar.open('There was an error', 'Close', { duration: 4000 });
  }

  addNewRow() {
    // TODO: Implement mutation for adding new output sample row
    this.snackBar.open('Add row functionality not yet implemented', 'Close', { duration: 3000 });
  }
}
