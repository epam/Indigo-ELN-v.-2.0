import { Component, computed, inject, input, OnInit } from '@angular/core';
import { Reaction, ReactionOutput } from '@core/types/entities/experiments/experiment.i';
import {
  ColumnConfig,
  ColumnInputType,
  ColumnOption,
} from '@pages/experiment/stoichiometry/shared/editable-table.types';
import { EditableDataTableComponent } from '@pages/experiment/stoichiometry/editable-data-table/editable-data-table.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { EnteredValue } from '@core/types/entities/values.i';
import { determineCellClasses } from '@core/utils/experiment-model.util';
import { CompoundType } from '@core/types/entities/compound.i';
import { BuiltInDictionary, DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { BuiltInDictionaryService } from '@core/services/health-hazards/built-in-dictionary.service';
import { MolUnit, WeightUnit } from '@core/types/entities/experiments/experiment-shared.i';

@Component({
  selector: 'eln-reaction-products-table',
  templateUrl: './reaction-products-table.component.html',
  imports: [EditableDataTableComponent],
})
export class ReactionProductsTableComponent implements OnInit {
  private experimentDetailService = inject(ExperimentDetailService);
  private builtInDictionaryService = inject(BuiltInDictionaryService);

  reaction = input<Reaction | null>(null);
  dataSource = computed(() => this.reaction()?.outputs);

  saltCodes = computed(() => this.builtInDictionaryService.getSaltCodes());

  ngOnInit() {
    this.builtInDictionaryService.load([BuiltInDictionary.HEALTH_HAZARD]);
    this.builtInDictionaryService.loadSaltCodes();
  }

  columns: ColumnConfig<ReactionOutput>[] = [
    {
      id: 'outputName',
      header: 'Output Name',
      type: ColumnInputType.TEXT,
      field: (row) => row.outputName,
      onSave: (row, value: string | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetOutputRowName',
            anchor: row.anchor,
            name: value,
          })
          .subscribe({});
      },
    },
    {
      id: 'molFormula',
      header: 'Mol Formula',
      type: ColumnInputType.HTML,
      editable: () => false,
      field: (row) => row.compound.formula,
    },
    {
      id: 'molWeight',
      header: 'Mol Weight',
      type: ColumnInputType.NUMBER,
      editable: () => false,
      field: (row) => row.compound.molWeight?.value?.toString(),
    },
    {
      id: 'exactMass',
      header: 'Exact Mass',
      type: ColumnInputType.NUMBER,
      field: (row) => row.compound.exactMass?.toString(),
      editable: () => false,
    },
    {
      id: 'theoWeight',
      header: 'Theo Weight',
      type: ColumnInputType.UNIT_INPUT,
      field: (row) => row.theoWeight,
      editable: () => false,
      options: Object.values(WeightUnit).map((unit) => ({
        id: unit,
        name: unit,
      })) as ColumnOption[],
    },
    {
      id: 'theoMoles',
      header: 'Theo Moles',
      type: ColumnInputType.UNIT_INPUT,
      field: (row) => row.theoMol,
      editable: () => false,
      options: Object.values(MolUnit).map((unit) => ({
        id: unit,
        name: unit,
      })) as ColumnOption[],
    },
    {
      id: 'saltCode',
      header: 'Salt Code',
      type: ColumnInputType.SELECT,
      field: (row) => row.compound.saltCode?.name ?? null,
      editable: (row) => row.compound.type === CompoundType.VIRTUAL,
      onSave: (row, selectedSaltCode: DictionaryItemRef | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetOutputSaltCode',
            anchor: row.anchor,
            saltCode: selectedSaltCode,
          })
          .subscribe({});
      },
      options: this.saltCodes(),
    },
    {
      id: 'saltEQ',
      header: 'Salt EQ',
      type: ColumnInputType.NUMBER,
      field: (row) => row.compound.saltEQ?.toString(),
      onSave: (row, value: string | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetOutputSaltEQ',
            anchor: row.anchor,
            saltEQ: parseInt(value),
          })
          .subscribe({});
      },
    },
    {
      id: 'eq',
      header: 'EQ',
      type: ColumnInputType.NUMBER,
      field: (row) => row.eq?.value?.toString(),
      classes: (row) => this.determineClasses(row.eq),
      onSave: (row, value: string | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetOutputRowEQ',
            anchor: row.anchor,
            eq: value,
          })
          .subscribe({});
      },
    },
    {
      id: 'addBatch',
      header: '',
      type: ColumnInputType.ICON,
      field: () => null,
      iconClasses: () => ['indicon-plus', 'text-[20px]', 'text-red-200'],
      tooltip: () => 'Add Batch',
      onSave: (row: ReactionOutput) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'AddProductSample',
            anchor: row.anchor,
          })
          .subscribe({});
      },
    },
  ];

  displayedColumns = this.columns.map((col) => col.id);

  private determineClasses(value?: EnteredValue<unknown>): string[] {
    return determineCellClasses(value, this.experimentDetailService.updatedNodes());
  }
}
