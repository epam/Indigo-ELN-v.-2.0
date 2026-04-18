import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { catchError, EMPTY, Observable } from 'rxjs';
import {
  ReactionInput,
  ReactionInputSample,
} from '@core/types/entities/experiments/experiment.i';
import {
  DensityUnit,
  MolarityUnit,
  MolUnit,
  ReactionRole,
  VolumeUnit,
  WeightUnit,
} from '@core/types/entities/experiments/experiment-shared.i';
import {
  BuiltInDictionary,
  DictionaryItemRef,
} from '@core/types/entities/dictionary.i';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { BuiltInDictionaryService } from '@core/services/health-hazards/built-in-dictionary.service';
import { CompoundType } from '@/core/types/entities/compound.i';
import { EditableDataTableComponent } from '../editable-data-table/editable-data-table.component';
import {
  ColumnConfig,
  ColumnInputType,
  ColumnOption,
} from '../shared/editable-table.types';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { EnteredValue } from '@core/types/entities/values.i';
import { determineCellClasses } from '@core/utils/experiment-model.util';
import { MutationResponse } from '@core/types/entities/experiments/mutation.i';
import { SelectComponent } from "@/core/components/common/select/select.component";
import { ButtonComponent } from "@/core/components/common/button/button.component";
import { MatIcon } from "@angular/material/icon";
import { FormsModule } from '@angular/forms';
import { SIGNIFICANT_FIGURES } from '../significant-figures.constants';
import { DropdownMenuItem } from '@/core/components/common/dropdown-menu/dropdown-menu.i';

interface InputSampleRow {
  input: ReactionInput;
  sample: ReactionInputSample;
}

@Component({
  selector: 'eln-reaction-inputs-table',
  templateUrl: './reaction-inputs-table.component.html',
  imports: [MatSnackBarModule, EditableDataTableComponent, SelectComponent, ButtonComponent, MatIcon,FormsModule],
})
export class ReactionInputsTableComponent implements OnInit {
  private experimentDetailService = inject(ExperimentDetailService);
  private builtInDictionaryService = inject(BuiltInDictionaryService);
  private snackBar = inject(MatSnackBar);
  readonly experimentModel = this.experimentDetailService.experimentModel;
  readonly items = signal<DropdownMenuItem[]>([...SIGNIFICANT_FIGURES]);

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
  saltCodes = computed(() => this.builtInDictionaryService.getSaltCodes());

  ngOnInit() {
    this.builtInDictionaryService.load([BuiltInDictionary.HEALTH_HAZARD]);
    this.builtInDictionaryService.loadSaltCodes();
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
      onSave: (row: InputSampleRow, value: string | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowChemicalName',
            anchor: row.input.anchor,
            chemicalName: value,
          })
          .subscribe({});
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
      onSave: (row: InputSampleRow, value: string | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputCompoundMolWeight',
            anchor: row.input.anchor,
            molWeight: value,
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
      onSave: (row: InputSampleRow, value: EnteredValue<WeightUnit> | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputWeight',
            anchor: row.sample.anchor,
            weight: value?.value,
            unit: value?.unit,
          })
          .subscribe({});
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
      onSave: (row: InputSampleRow, value: EnteredValue<VolumeUnit> | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputVolume',
            anchor: row.sample.anchor,
            volume: value?.value,
            unit: value?.unit,
          })
          .subscribe({});
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
      onSave: (row: InputSampleRow, value: EnteredValue<MolUnit> | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputMol',
            anchor: row.sample.anchor,
            mol: value?.value,
            unit: value?.unit,
          })
          .subscribe({});
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
      onSave: (row: InputSampleRow, value: string | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowEQ',
            anchor: row.input.anchor,
            eq: value,
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
      options: [
        ReactionRole.REACTANT,
        ReactionRole.CATALYST,
        ReactionRole.SOLVENT,
      ].map((role) => ({
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
      onSave: (
        row: InputSampleRow,
        value: EnteredValue<DensityUnit> | null,
      ) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputDensity',
            anchor: row.sample.anchor,
            density: value?.value,
            unit: value?.unit,
          })
          .subscribe({});
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
      onSave: (
        row: InputSampleRow,
        value: EnteredValue<MolarityUnit> | null,
      ) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputMolarity',
            anchor: row.sample.anchor,
            molarity: value?.value,
            unit: value?.unit,
          })
          .subscribe({});
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
      onSave: (row: InputSampleRow, value: string | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputPurity',
            anchor: row.sample.anchor,
            purity: value,
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
      onSave: (
        row: InputSampleRow,
        selectedSaltCode: DictionaryItemRef | null,
      ) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowSaltCode',
            anchor: row.input.anchor,
            saltCode: selectedSaltCode,
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
      onSave: (row: InputSampleRow, value: string | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowSaltEQ',
            anchor: row.input.anchor,
            saltEQ: value,
          })
          .subscribe({});
      },
    },
    {
      id: 'hazardComments',
      header: 'Hazard Comments',
      type: ColumnInputType.MULTI_SELECT,
      field: (row: InputSampleRow) => row.sample.healthHazards ?? [],
      onSave: (row: InputSampleRow, selectedHazards: DictionaryItemRef[]) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputHealthHazards',
            anchor: row.sample.anchor,
            healthHazards: selectedHazards,
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
      onSave: (row: InputSampleRow, value: string | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputComment',
            anchor: row.sample.anchor,
            comment: value,
          })
          .subscribe({});
      },
    },
    {
      id: 'delete',
      header: '',
      type: ColumnInputType.ICON,
      field: () => null,
      iconClasses: () => ['indicon-delete', 'text-[20px]', 'text-red-200'],
      tooltip: () => 'Delete',
      onSave: (row: InputSampleRow) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'RemoveInput',
            anchor: row.sample.anchor,
          })
          .subscribe({});
      },
    },
  ]);

  displayedColumns = computed(() => this.columns().map((col) => col.id));

  private determineClasses(value?: EnteredValue<unknown>): string[] {
    return determineCellClasses(
      value,
      this.experimentDetailService.updatedNodes(),
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

      private parseSignificantFigure(
    value: string | string[] | null,
  ): number | null {
    if (!value || Array.isArray(value)) return null;

    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? null : parsed;
  }
}
