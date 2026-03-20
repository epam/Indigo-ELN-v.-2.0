import { Component, computed, inject, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { ReactionInput, ReactionInputSample, } from '@core/types/entities/experiments/experiment.i';
import {
  DensityUnit,
  MolarityUnit,
  MolUnit,
  ReactionRole,
  VolumeUnit,
  WeightUnit,
} from '@core/types/entities/experiments/experiment-shared.i';
import { BuiltInDictionary, DictionaryItemRef, } from '@core/types/entities/dictionary.i';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BuiltInDictionaryService } from '@core/services/health-hazards/built-in-dictionary.service';
import { CompoundType } from '@/core/types/entities/compound.i';
import { EditableDataTableComponent } from '../editable-data-table/editable-data-table.component';
import { ColumnConfig, ColumnInputType, ColumnOption, UnitInputChange, } from '../shared/editable-table.types';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';
import { EnteredValue } from '@core/types/entities/values.i';
import { determineCellClasses } from '@core/utils/experiment-model.util';

interface InputSampleRow {
  input: ReactionInput;
  sample: ReactionInputSample;
}

@Component({
  selector: 'eln-reaction-inputs-table',
  templateUrl: './reaction-inputs-table.component.html',
  imports: [MatSnackBarModule, EditableDataTableComponent],
})
export class ReactionInputsTableComponent implements OnInit {
  private experimentDetailService = inject(ExperimentDetailService);
  private builtInDictionaryService = inject(BuiltInDictionaryService);
  private snackBar = inject(MatSnackBar);

  reaction = computed(
    () => this.experimentDetailService.experimentDetail()?.model.reactions[0],
  );
  dataSource = computed(() => {
    const inputs = this.reaction()?.inputs;
    return inputs?.flatMap((input) =>
      input.samples.map((sample) => ({ input, sample })),
    );
  });
  healthHazards = computed(() =>
    this.builtInDictionaryService.getDictionaryItem(
      BuiltInDictionary.HEALTH_HAZARD,
    ),
  );
  saltCodes = computed(() => {
    // TODO: Load from API when available
    return [] as DictionaryItemRef[];
  });

  ngOnInit() {
    this.builtInDictionaryService.load([BuiltInDictionary.HEALTH_HAZARD]);
  }

  readonly columns = computed<ColumnConfig<InputSampleRow>[]>(() => [
    {
      id: 'compoundId',
      header: 'Compound ID',
      type: ColumnInputType.TEXT,
      field: (row: InputSampleRow) => row.input.compound.compoundKey,
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
      field: (row: InputSampleRow) => row.input.chemicalName,
      editable: () => false,
      onSave: () => {
        /* TODO add mutation for chemical name */
      },
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
      field: (row: InputSampleRow) =>
        row.input.compound.molWeight?.value?.toString(),
      classes: (row) => this.determineClasses(row.input.compound.molWeight),
      editable: (row: InputSampleRow) =>
        row.input.compound.type === CompoundType.UNKNOWN,
      onSave: (row: InputSampleRow, event: Event) => {
        const value = (event.target as HTMLInputElement).value;
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputCompoundMolWeight',
            anchor: row.input.anchor,
            molWeight: value || null,
          })
          .subscribe({});
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
      classes: (row) => this.determineClasses(row.sample.weight),
      onSave: (row: InputSampleRow, payload?: unknown) => {
        this.applyUnitInputChange(payload as UnitInputChange, (value, unit) => {
          return this.experimentDetailService.updateDataModel({
            type: 'SetInputWeight',
            anchor: row.sample.anchor,
            weight: value,
            unit: unit as WeightUnit,
          });
        });
      },
      options: Object.values(WeightUnit).map((unit) => ({
        id: unit,
        name: unit,
      })) as ColumnOption[],
    },
    {
      id: 'volume',
      header: 'Volume',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) =>
        row.sample.volume?.value
          ? { value: row.sample.volume.value, unit: row.sample.volume.unit }
          : null,
      classes: (row) => this.determineClasses(row.sample.volume),
      onSave: (row: InputSampleRow, payload?: unknown) => {
        this.applyUnitInputChange(payload as UnitInputChange, (value, unit) => {
          return this.experimentDetailService.updateDataModel({
            type: 'SetInputVolume',
            anchor: row.sample.anchor,
            volume: value,
            unit: unit as VolumeUnit,
          });
        });
      },
      options: Object.values(VolumeUnit).map((unit) => ({
        id: unit,
        name: unit,
      })) as ColumnOption[],
    },
    {
      id: 'mol',
      header: 'Mol',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) =>
        row.sample.mol?.value
          ? { value: row.sample.mol.value, unit: row.sample.mol.unit }
          : null,
      classes: (row) => this.determineClasses(row.sample.mol),
      onSave: (row: InputSampleRow, payload?: unknown) => {
        this.applyUnitInputChange(payload as UnitInputChange, (value, unit) => {
          return this.experimentDetailService.updateDataModel({
            type: 'SetInputMol',
            anchor: row.sample.anchor,
            mol: value,
            unit: unit as MolUnit,
          });
        });
      },
      options: Object.values(MolUnit).map((unit) => ({
        id: unit,
        name: unit,
      })) as ColumnOption[],
    },
    {
      id: 'eq',
      header: 'EQ',
      type: ColumnInputType.NUMBER,
      field: (row: InputSampleRow) => row.input.eq?.value?.toString(),
      classes: (row) => this.determineClasses(row.input.eq),
      onSave: (row: InputSampleRow, event: Event) => {
        const value = (event.target as HTMLInputElement).value;

        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowEQ',
            anchor: row.input.anchor,
            eq: value || null,
          })
          .subscribe({});
      },
    },
    {
      id: 'limiting',
      header: 'Limiting',
      type: ColumnInputType.CHECKBOX,
      field: (row: InputSampleRow) => row.input.limiting,
      onSave: (row: InputSampleRow) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowLimiting',
            anchor: row.input.anchor,
          })
          .subscribe({});
      },
    },
    {
      id: 'rxnRole',
      header: 'Rxn Role',
      type: ColumnInputType.SELECT,
      field: (row: InputSampleRow) => row.input.role,
      onSave: (row: InputSampleRow, value: ReactionRole) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowRole',
            anchor: row.input.anchor,
            role: value,
          })
          .subscribe({});
      },
      options: Object.values(ReactionRole).map((role) => ({
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
      classes: (row) => this.determineClasses(row.sample.density),
      onSave: (row: InputSampleRow, payload?: unknown) => {
        this.applyUnitInputChange(payload as UnitInputChange, (value, unit) => {
          return this.experimentDetailService.updateDataModel({
            type: 'SetInputDensity',
            anchor: row.sample.anchor,
            density: value,
            unit: unit as DensityUnit,
          });
        });
      },
      options: Object.values(DensityUnit).map((unit) => ({
        id: unit,
        name: unit,
      })) as ColumnOption[],
    },
    {
      id: 'molarity',
      header: 'Molarity',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) =>
        row.sample.molarity?.value
          ? { value: row.sample.molarity.value, unit: row.sample.molarity.unit }
          : null,
      classes: (row) => this.determineClasses(row.sample.molarity),
      onSave: (row: InputSampleRow, payload?: unknown) => {
        this.applyUnitInputChange(payload as UnitInputChange, (value, unit) => {
          return this.experimentDetailService.updateDataModel({
            type: 'SetInputMolarity',
            anchor: row.sample.anchor,
            molarity: value,
            unit: unit as MolarityUnit,
          });
        });
      },
      options: Object.values(MolarityUnit).map((unit) => ({
        id: unit,
        name: unit,
      })) as ColumnOption[],
    },
    {
      id: 'purity',
      header: 'Purity',
      type: ColumnInputType.NUMBER,
      field: (row: InputSampleRow) => row.sample.purity?.value?.toString(),
      classes: (row) => this.determineClasses(row.sample.purity),
      onSave: (row: InputSampleRow, event: Event) => {
        const value = (event.target as HTMLInputElement).value;
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputPurity',
            anchor: row.sample.anchor,
            purity: value || null,
          })
          .subscribe({});
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
      editable: (row: InputSampleRow) =>
        row.input.compound.type === CompoundType.VIRTUAL,
      onSave: (row: InputSampleRow, selectedSaltCode: unknown) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowSaltCode',
            anchor: row.input.anchor,
            saltCode: selectedSaltCode as DictionaryItemRef | null,
          })
          .subscribe({});
      },
      options: this.saltCodes(),
    },
    {
      id: 'saltEQ',
      header: 'Salt EQ',
      type: ColumnInputType.TEXT,
      field: (row: InputSampleRow) => row.input.compound.saltEQ?.toString(),
      onSave: (row: InputSampleRow, event: Event) => {
        const value = (event.target as HTMLInputElement).value;
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowSaltEQ',
            anchor: row.input.anchor,
            saltEQ: value || null,
          })
          .subscribe({});
      },
    },
    {
      id: 'hazardComments',
      header: 'Hazard Comments',
      type: ColumnInputType.MULTI_SELECT,
      field: (row: InputSampleRow) => row.sample.healthHazards ?? [],
      onSave: (row: InputSampleRow, selectedHazards: unknown[]) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputHealthHazards',
            anchor: row.sample.anchor,
            healthHazards: selectedHazards as DictionaryItemRef[],
          })
          .subscribe({});
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
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputComment',
            anchor: row.sample.anchor,
            comment: value || null,
          })
          .subscribe({});
      },
    },
  ]);

  displayedColumns = computed(() => this.columns().map((col) => col.id));

  compareDictionaryItems = (
    a?: DictionaryItemRef | null,
    b?: DictionaryItemRef | null,
  ) => (!!a && !!b ? a.id === b.id : a === b);

  private applyUnitInputChange(
    change: UnitInputChange,
    mutator: (
      value: string | undefined,
      unit: string | undefined,
    ) => Observable<ExperimentDetail>,
  ) {
    // Only proceed if both value and unit are present
    if (
      change.value == null ||
      change.value === '' ||
      change.unit == null ||
      change.unit === ''
    ) {
      if (change.previous?.value != null) {
        mutator(undefined, undefined).subscribe({});
        return;
      }
      return;
    }

    mutator(change.value, change.unit).subscribe({});
  }

  private determineClasses(value?: EnteredValue<unknown>): string[] {
    return determineCellClasses(
      value,
      this.experimentDetailService.experimentDetail(),
    );
  }

  addNewRow() {
    if (!this.reaction()) {
      this.snackBar.open('No reaction available', 'Close', { duration: 3000 });
      return;
    }

    this.experimentDetailService
      .updateDataModel({
        type: 'AddEmptyInput',
        anchor: this.reaction()!.anchor,
      })
      .subscribe(() =>
        this.snackBar.open('Material added', 'Close', { duration: 2000 }),
      );
  }
}
