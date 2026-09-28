import { Component, computed, inject, input, signal, TemplateRef, ViewChild } from '@angular/core';
import { ReactionInput, ReactionInputSample } from '@core/types/entities/experiments/experiment.i';
import {
  DensityUnit,
  MolarityUnit,
  MolUnit,
  ReactionRole,
  ReactionRoleNames,
  UNIT_DISPLAY_NAMES,
  UUID,
  VolumeUnit,
  WeightUnit,
} from '@core/types/entities/experiments/experiment-shared.i';
import { BuiltInDictionary, DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BuiltInDictionaryService } from '@core/services/health-hazards/built-in-dictionary.service';
import { CompoundType } from '@/core/types/entities/compound.i';
import { EditableDataTableComponent } from '../editable-data-table/editable-data-table.component';
import { ColumnConfig, ColumnInputType, ColumnOption } from '../shared/editable-table.types';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { EnteredValue } from '@core/types/entities/values.i';
import { SelectComponent } from '@/core/components/common/select/select.component';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { FormsModule } from '@angular/forms';
import { SIGNIFICANT_FIGURES } from '../significant-figures.constants';
import { DropdownMenuItem } from '@/core/components/common/dropdown-menu/dropdown-menu.i';
import { ReactionAnchor } from '@core/types/entities/experiments/mutation.i';
import { MatTooltip } from '@angular/material/tooltip';
import { SampleSearchComponent } from '@pages/experiment/sample-search/sample-search.component';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { SlideInPanelService } from '@core/components/common/slide-in-panel/slide-in-panel.service';

interface InputSampleRow {
  input: ReactionInput;
  sample: ReactionInputSample;
}

@Component({
  selector: 'eln-reaction-inputs-table',
  templateUrl: './reaction-inputs-table.component.html',
  imports: [MatSnackBarModule, EditableDataTableComponent, SelectComponent, ButtonComponent, FormsModule, MatTooltip],
})
export class ReactionInputsTableComponent {
  private experimentDetailService = inject(ExperimentDetailService);
  private builtInDictionaryService = inject(BuiltInDictionaryService);
  private slideInPanel = inject(SlideInPanelService);
  private notificationService = inject(NotificationService);

  @ViewChild('sampleHeader') sampleHeader: TemplateRef<any>;

  experimentId = input.required<UUID>();
  reactionAnchor = input.required<ReactionAnchor>();

  model = computed(() => this.experimentDetailService.experimentModel());
  significantFiguresOptions = signal<DropdownMenuItem[]>([...SIGNIFICANT_FIGURES]);

  reaction = computed(() => this.experimentDetailService.getReaction(this.reactionAnchor()));
  dataSource = computed(() => {
    return this.reaction().inputs.flatMap((input) => input.samples.map((sample) => ({ input, sample })));
  });
  healthHazards = this.builtInDictionaryService.getDictionaryItems(BuiltInDictionary.HEALTH_HAZARD);
  saltCodes = this.builtInDictionaryService.getDictionaryItems(BuiltInDictionary.SALT_CODE);

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
      field: (row: InputSampleRow) => row.input.compound.molWeight?.value?.toString(),
      classes: (row) => this.determineClasses(row.input.compound.molWeight),
      editable: (row: InputSampleRow) => row.input.compound.type === CompoundType.UNKNOWN,
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
      field: (row: InputSampleRow) => row.sample.weight,
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
        name: UNIT_DISPLAY_NAMES[unit],
      })) as ColumnOption[],
    },
    {
      id: 'volume',
      header: 'Volume',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) => row.sample.volume,
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
        name: UNIT_DISPLAY_NAMES[unit],
      })) as ColumnOption[],
    },
    {
      id: 'mol',
      header: 'Mol',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) => row.sample.mol,
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
        name: UNIT_DISPLAY_NAMES[unit],
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
      type: ColumnInputType.RADIO,
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
      field: (row: InputSampleRow) => ({ id: row.input.role, name: ReactionRoleNames[row.input.role] }) as ColumnOption,
      required: () => true,
      onSave: (row: InputSampleRow, value: ColumnOption) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowRole',
            anchor: row.input.anchor,
            role: value.id as ReactionRole,
          })
          .subscribe({});
      },
      options: [ReactionRole.REACTANT, ReactionRole.REAGENT, ReactionRole.CATALYST, ReactionRole.SOLVENT].map(
        (role) => ({
          id: role,
          name: ReactionRoleNames[role],
        }),
      ) as ColumnOption[],
    },
    {
      id: 'density',
      header: 'Density',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) => row.sample.density,
      classes: (row) => this.determineClasses(row.sample.density),
      onSave: (row: InputSampleRow, value: EnteredValue<DensityUnit> | null) => {
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
        name: UNIT_DISPLAY_NAMES[unit],
      })) as ColumnOption[],
    },
    {
      id: 'molarity',
      header: 'Molarity',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: InputSampleRow) => row.sample.molarity,
      classes: (row) => this.determineClasses(row.sample.molarity),
      onSave: (row: InputSampleRow, value: EnteredValue<MolarityUnit> | null) => {
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
        name: UNIT_DISPLAY_NAMES[unit],
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
      type: ColumnInputType.HTML,
      field: (row: InputSampleRow) => row.input.compound.formula,
      editable: () => false,
    },
    {
      id: 'saltCode',
      header: 'Salt Code',
      type: ColumnInputType.SELECT,
      field: (row: InputSampleRow) => row.input.compound.saltCode ?? null,
      editable: (row: InputSampleRow) => row.input.compound.type === CompoundType.VIRTUAL,
      onSave: (row: InputSampleRow, selectedSaltCode: DictionaryItemRef | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetInputRowSaltCode',
            anchor: row.input.anchor,
            saltCode: selectedSaltCode,
          })
          .subscribe({});
      },
      options: this.saltCodes,
    },
    {
      id: 'saltEQ',
      header: 'Salt EQ',
      type: ColumnInputType.TEXT,
      field: (row: InputSampleRow) => row.input.compound.saltEQ?.toString(),
      editable: (row: InputSampleRow) => row.input.compound.saltCode != null,
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
      options: this.healthHazards,
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
          .subscribe(() =>
            this.notificationService.notify({
              type: NotificationType.Info,
              isInline: true,
              message: 'Removed, press Ctrl-Z/Cmd-Z to undo',
            }),
          );
      },
    },
  ]);

  displayedColumns = computed(() => this.columns().map((col) => col.id));

  private determineClasses(value?: EnteredValue<unknown>): string[] {
    return this.experimentDetailService.determineCellClasses(value);
  }

  addMaterial() {
    const ref = this.slideInPanel.open(SampleSearchComponent, {
      header: this.sampleHeader,
      inputs: { reactionAnchor: this.reactionAnchor() },
    });
    ref.instance.closed.subscribe(() => ref.close());
  }

  addNewRow() {
    this.experimentDetailService
      .updateDataModel({
        type: 'AddEmptyInput',
        anchor: this.reaction().anchor,
      })
      .subscribe();
  }

  onSignificantFiguresChange(value: string | string[] | null): void {
    const parsedValue = this.parseSignificantFigure(value);
    if (parsedValue === null) return;

    this.experimentDetailService
      .updateDataModel({
        type: 'SetExperimentSignificantFigures',
        significantFigures: parsedValue,
      })
      .subscribe();
  }

  private parseSignificantFigure(value: string | string[] | null): number | null {
    if (!value || Array.isArray(value)) return null;

    const parsed = Number.parseInt(value, 10);
    return Number.isNaN(parsed) ? null : parsed;
  }
}
