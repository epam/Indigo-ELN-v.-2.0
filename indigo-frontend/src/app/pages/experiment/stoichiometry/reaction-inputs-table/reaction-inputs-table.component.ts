import { Component, computed, inject, input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Reaction,
  ReactionInput,
  ReactionInputSample,
  ExperimentModel,
} from '@core/types/entities/experiments/experiment.i';
import {
  DensityUnit,
  MolarityUnit,
  MolUnit,
  ReactionRole,
  VolumeUnit,
  WeightUnit,
} from '@core/types/entities/experiments/experiment-shared.i';
import { DictionaryItemRef, BuiltInDictionary } from '@core/types/entities/dictionary.i';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ExperimentModelService } from '@core/services/experiment/experiment-model.service';
import { BuiltInDictionaryService } from '@core/services/health-hazards/built-in-dictionary.service';
import { CompoundType } from '@/core/types/entities/compound.i';
import { EditableDataTableComponent } from '../editable-data-table/editable-data-table.component';
import {
  ColumnInputType,
  ColumnConfig,
  UnitInputChange,
  ColumnOption,
} from '../shared/editable-table.types';

interface InputSampleRow {
  input: ReactionInput;
  sample: ReactionInputSample;
}

@Component({
  selector: 'eln-reaction-inputs-table',
  templateUrl: './reaction-inputs-table.component.html',
  imports: [
    MatSnackBarModule,
    EditableDataTableComponent,
  ],
})
export class ReactionInputsTableComponent implements OnInit {
  private experimentModelService = inject(ExperimentModelService);
  private builtInDictionaryService = inject(BuiltInDictionaryService);
  private snackBar = inject(MatSnackBar);

  reaction = input<Reaction | null>(null);
  experimentId = input<string | null>(null);
  dataSource = computed(() => {
    const inputs = this.reaction()?.inputs ?? [];
    return inputs.flatMap(input =>
      input.samples.map(sample => ({ input, sample }))
    );
  });
  healthHazards = computed(() =>
    this.builtInDictionaryService.getDictionaryItems(BuiltInDictionary.HEALTH_HAZARD),
  );
  saltCodes = computed(() => {
    // TODO: Load from API when available
    return [] as DictionaryItemRef[];
  });

  ngOnInit() {
    this.builtInDictionaryService.load(BuiltInDictionary.HEALTH_HAZARD);
  }

  readonly columns = computed<ColumnConfig<InputSampleRow>[]>(() => [
    {
      id: 'compoundId',
      header: 'Compound ID',
      type: ColumnInputType.TEXT,
      field: (row: InputSampleRow) => row.input.compound.strCode,
      editable: () => false,
    },
    {
      id: 'casNumber',
      header: 'CAS Number',
      type: ColumnInputType.TEXT,
      field: (row: InputSampleRow) => row.input.compound.casNumber,
      editable: () => false,
    },
    {
      id: 'chemicalName',
      header: 'Chemical Name',
      type: ColumnInputType.TEXT,
      field: (row: InputSampleRow) => row.sample.chemicalName,
      editable: () => false,
      onSave: () => { /* TODO add mutation for chemical name */ },
    },
    {
      id: 'nbkBatch',
      header: 'NBK Batch #',
      type: ColumnInputType.TEXT,
      field: (row: InputSampleRow) => row.sample.nbkBatchNumber,
      editable: () => false,
    },
    {
      id: 'molWeight',
      header: 'Mol. Weight',
      type: ColumnInputType.NUMBER,
      field: (row: InputSampleRow) => row.input.compound.molWeight?.value?.toString(),
      editable: (row: InputSampleRow) => row.input.compound.type === CompoundType.UNKNOWN,
      onSave: (row: InputSampleRow, event: Event) => {
        const value = +(event.target as HTMLInputElement).value;
        const previousState = structuredClone(this.experimentModelService.experimentModel());

        this.experimentModelService.updateDataModel({
          type: 'SetInputCompoundMolWeight',
          anchor: row.input.anchor,
          molWeight: value || null,
        }).subscribe({
          error: (error) => this.handleUpdateError(error, previousState),
        });
      },
    },
    {
      id: 'weight',
      header: 'Weight',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) =>
        row.sample.weight?.value
          ? { value: row.sample.weight.value, unit: row.sample.weight.unit }
          : null,
      onSave: (row: InputSampleRow, payload?: unknown) => {
        this.applyUnitInputChange(payload as UnitInputChange, (value, unit) => {
          return this.experimentModelService.updateDataModel({
            type: 'SetInputWeight',
            anchor: row.sample.anchor,
            weight: value,
            unit: unit as WeightUnit,
          });
        });
      },
      options: Object.values(WeightUnit).map(unit => ({ id: unit, name: unit })) as ColumnOption[],
    },
    {
      id: 'volume',
      header: 'Volume',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) =>
        row.sample.volume?.value
          ? { value: row.sample.volume.value, unit: row.sample.volume.unit }
          : null,
      onSave: (row: InputSampleRow, payload?: unknown) => {
        this.applyUnitInputChange(payload as UnitInputChange, (value, unit) => {
          return this.experimentModelService.updateDataModel({
            type: 'SetInputVolume',
            anchor: row.sample.anchor,
            volume: value,
            unit: unit as VolumeUnit,
          });
        });
      },
      options: Object.values(VolumeUnit).map(unit => ({ id: unit, name: unit })) as ColumnOption[],
    },
    {
      id: 'mol',
      header: 'Mol',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) =>
        row.sample.mol?.value ? { value: row.sample.mol.value, unit: row.sample.mol.unit } : null,
      onSave: (row: InputSampleRow, payload?: unknown) => {
        this.applyUnitInputChange(payload as UnitInputChange, (value, unit) => {
          return this.experimentModelService.updateDataModel({
            type: 'SetInputMol',
            anchor: row.sample.anchor,
            mol: value,
            unit: unit as MolUnit,
          });
        });
      },
      options: Object.values(MolUnit).map(unit => ({ id: unit, name: unit })) as ColumnOption[],
    },
    {
      id: 'eq',
      header: 'EQ',
      type: ColumnInputType.NUMBER,
      field: (row: InputSampleRow) => row.input.eq?.value?.toString(),
      onSave: (row: InputSampleRow, event: Event) => {
        const value = +(event.target as HTMLInputElement).value;
        const previousState = structuredClone(this.experimentModelService.experimentModel());

        this.experimentModelService.updateDataModel({
          type: 'SetInputRowEQ',
          anchor: row.input.anchor,
          eq: value,
        }).subscribe({
          error: (error) => this.handleUpdateError(error, previousState),
        });
      },
    },
    {
      id: 'limiting',
      header: 'Limiting',
      type: ColumnInputType.CHECKBOX,
      field: (row: InputSampleRow) => row.input.limiting,
      onSave: (row: InputSampleRow) => {
        const previousState = structuredClone(this.experimentModelService.experimentModel());

        this.experimentModelService.updateDataModel({
          type: 'SetInputRowLimiting',
          anchor: row.input.anchor,
        }).subscribe({
          error: (error) => this.handleUpdateError(error, previousState),
        });
      },
    },
    {
      id: 'rxnRole',
      header: 'Rxn Role',
      type: ColumnInputType.SELECT,
      field: (row: InputSampleRow) => row.input.role,
      onSave: (row: InputSampleRow, value: ReactionRole) => {
        const previousState = structuredClone(this.experimentModelService.experimentModel());

        this.experimentModelService.updateDataModel({
          type: 'SetInputRowRole',
          anchor: row.input.anchor,
          role: value,
        }).subscribe({
          error: (error) => this.handleUpdateError(error, previousState),
        });
      },
      options: Object.values(ReactionRole).map(role => ({
        id: role,
        name: role.toLocaleLowerCase(),
      })) as ColumnOption[],
    },
    {
      id: 'density',
      header: 'Density',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) =>
        row.sample.density?.value
          ? { value: row.sample.density.value, unit: row.sample.density.unit }
          : null,
      onSave: (row: InputSampleRow, payload?: unknown) => {
        this.applyUnitInputChange(payload as UnitInputChange, (value, unit) => {
          return this.experimentModelService.updateDataModel({
            type: 'SetInputDensity',
            anchor: row.sample.anchor,
            density: value,
            unit: unit as DensityUnit,
          });
        });
      },
      options: Object.values(DensityUnit).map(unit => ({ id: unit, name: unit })) as ColumnOption[],
    },
    {
      id: 'molarity',
      header: 'Molarity',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) =>
        row.sample.molarity?.value
          ? { value: row.sample.molarity.value, unit: row.sample.molarity.unit }
          : null,
      onSave: (row: InputSampleRow, payload?: unknown) => {
        this.applyUnitInputChange(payload as UnitInputChange, (value, unit) => {
          return this.experimentModelService.updateDataModel({
            type: 'SetInputMolarity',
            anchor: row.sample.anchor,
            molarity: value,
            unit: unit as MolarityUnit,
          });
        });
      },
      options: Object.values(MolarityUnit).map(unit => ({ id: unit, name: unit })) as ColumnOption[],
    },
    {
      id: 'purity',
      header: 'Purity',
      type: ColumnInputType.NUMBER,
      field: (row: InputSampleRow) => row.sample.purity?.value?.toString(),
      onSave: (row: InputSampleRow, event: Event) => {
        const value = +(event.target as HTMLInputElement).value;
        const previousState = structuredClone(this.experimentModelService.experimentModel());

        this.experimentModelService.updateDataModel({
          type: 'SetInputPurity',
          anchor: row.sample.anchor,
          purity: value,
        }).subscribe({
          error: (error) => this.handleUpdateError(error, previousState),
        });
      },
    },
    {
      id: 'molFormula',
      header: 'Mol Formula',
      type: ColumnInputType.TEXT,
      field: (row: InputSampleRow) => row.input.compound.formula,
      editable: () => false, // TODO define if editable or not
    },
    {
      id: 'saltCode',
      header: 'Salt Code',
      type: ColumnInputType.SELECT,
      field: (row: InputSampleRow) => row.input.compound.saltCode?.name ?? null,
      editable: (row: InputSampleRow) => row.input.compound.type === CompoundType.VIRTUAL,
      onSave: (row: InputSampleRow, selectedSaltCode: unknown) => {
        const previousState = structuredClone(this.experimentModelService.experimentModel());

        this.experimentModelService.updateDataModel({
          type: 'SetInputRowSaltCode',
          anchor: row.input.anchor,
          saltCode: selectedSaltCode as DictionaryItemRef | null,
        }).subscribe({
          error: (error) => this.handleUpdateError(error, previousState),
        });
      },
      options: this.saltCodes(),
    },
    {
      id: 'saltEQ',
      header: 'Salt EQ',
      type: ColumnInputType.TEXT,
      field: (row: InputSampleRow) => row.input.compound.saltEQ?.toString(),
      onSave: (row: InputSampleRow, event: Event) => {
        const value = +(event.target as HTMLInputElement).value;
        const previousState = structuredClone(this.experimentModelService.experimentModel());

        this.experimentModelService.updateDataModel({
          type: 'SetInputRowSaltEQ',
          anchor: row.input.anchor,
          saltEQ: value || null,
        }).subscribe({
          error: (error) => this.handleUpdateError(error, previousState),
        });
      },
    },
    {
      id: 'hazardComments',
      header: 'Hazard Comments',
      type: ColumnInputType.MULTI_SELECT,
      field: (row: InputSampleRow) => row.sample.healthHazards ?? [],
      onSave: (row: InputSampleRow, selectedHazards: unknown[]) => {
        const previousState = structuredClone(this.experimentModelService.experimentModel());

        this.experimentModelService.updateDataModel({
          type: 'SetInputHealthHazards',
          anchor: row.sample.anchor,
          healthHazards: selectedHazards as DictionaryItemRef[],
        }).subscribe({
          error: (error) => this.handleUpdateError(error, previousState),
        });
      },
      options: this.healthHazards(),
    },
    {
      id: 'comments',
      header: 'Comments',
      type: ColumnInputType.TEXT,
      field: (row: InputSampleRow) => row.sample.comment ?? null,
      onSave: (row: InputSampleRow, event: Event) => {
        const value = (event.target as HTMLInputElement).value;
        const previousState = structuredClone(this.experimentModelService.experimentModel());

        this.experimentModelService.updateDataModel({
          type: 'SetInputComment',
          anchor: row.sample.anchor,
          comment: value || null,
        }).subscribe({
          error: (error) => this.handleUpdateError(error, previousState),
        });
      },
    },
  ]);

  displayedColumns = computed(() => this.columns().map((col) => col.id));

  compareDictionaryItems = (a?: DictionaryItemRef | null, b?: DictionaryItemRef | null) =>
    !!a && !!b ? a.id === b.id : a === b;

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
    if (!this.reaction()) {
      this.snackBar.open('No reaction available', 'Close', { duration: 3000 });
      return;
    }

    this.experimentModelService.updateDataModel({
      type: 'AddEmptyInput',
      anchor: this.reaction()!.anchor,
    }).subscribe({
      next: () => this.snackBar.open('Material added', 'Close', { duration: 2000 }),
      error: (error) => {
        const previousState = structuredClone(this.experimentModelService.experimentModel());
        this.handleUpdateError(error, previousState);
      },
    });
  }

}
