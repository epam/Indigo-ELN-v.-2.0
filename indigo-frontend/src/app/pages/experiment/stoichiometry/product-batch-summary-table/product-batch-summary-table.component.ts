import { Component, computed, inject, input } from '@angular/core';
import { switchMap } from 'rxjs';
import { ReactionOutput, ReactionOutputSample } from '@core/types/entities/experiments/experiment.i';
import {
  MolUnit,
  ReactionOutputType,
  SampleRegistrationStatus,
  UNIT_DISPLAY_NAMES,
  UUID,
  VolumeUnit,
  WeightUnit,
} from '@core/types/entities/experiments/experiment-shared.i';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { EditableDataTableComponent } from '../editable-data-table/editable-data-table.component';
import { BatchDetailData, BatchDetailPanelComponent } from '../batch-detail-panel/batch-detail-panel.component';
import { ColumnConfig, ColumnInputType, ColumnOption, ExpandableConfig } from '../shared/editable-table.types';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { EnteredValue } from '@core/types/entities/values.i';
import { determineCellClasses } from '@core/utils/experiment-model.util';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { MatIcon } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { MatTooltip } from '@angular/material/tooltip';
import { ApiService } from '@core/services/api.service';
import { openFileDialog } from '@core/utils/file.util';
import { MutationResponse, ReactionAnchor } from '@core/types/entities/experiments/mutation.i';

interface OutputSampleRow {
  output: ReactionOutput;
  sample: ReactionOutputSample;
}

@Component({
  selector: 'eln-product-batch-summary-table',
  templateUrl: './product-batch-summary-table.component.html',
  imports: [MatSnackBarModule, EditableDataTableComponent, ButtonComponent, MatIcon, MatMenuModule, MatTooltip],
})
export class ProductBatchSummaryTableComponent {
  private experimentDetailService = inject(ExperimentDetailService);
  private notificationService = inject(NotificationService);
  private apiService = inject(ApiService);

  experimentId = input.required<UUID>();
  reactionAnchor = input.required<ReactionAnchor>();

  reaction = computed(() => this.experimentDetailService.getReaction(this.reactionAnchor()));

  dataSource = computed(() => {
    return this.reaction().outputs.flatMap((output) => output.samples.map((sample) => ({ output, sample })));
  });

  linkableProducts = computed(() => {
    return this.reaction().outputs.filter((p) => p.intended);
  });

  readonly columns = computed<ColumnConfig<OutputSampleRow>[]>(() => [
    {
      id: 'batchId',
      header: 'Batch No',
      type: ColumnInputType.TEXT,
      field: (row: OutputSampleRow) => {
        const nbk = row.sample.nbkBatchNumber ?? '';
        // Extract last segment after last hyphen (e.g., "aaaaaaaa-bbbb-ccc" -> "ccc")
        const parts = nbk.split('-');
        return parts.length > 0 ? parts[parts.length - 1] : nbk;
      },
      editable: () => false,
    },
    {
      id: 'chemicalName',
      header: 'Product Name',
      type: ColumnInputType.TEXT,
      field: (row: OutputSampleRow) => row.output.outputName ?? null,
      editable: () => false,
    },
    {
      id: 'reactionStep',
      header: 'Reaction Step',
      type: ColumnInputType.TEXT,
      field: () => '1', // TODO: Use reaction index when multiple reactions supported
      editable: () => false,
    },
    {
      id: 'productType',
      header: 'Product Type',
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
      header: 'Actual Weight',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: OutputSampleRow) =>
        row.sample.actualWeight?.value
          ? {
              value: row.sample.actualWeight.value,
              unit: row.sample.actualWeight.unit,
            }
          : null,
      classes: (row) => this.determineClasses(row.sample.actualWeight),
      onSave: (row: OutputSampleRow, value: EnteredValue<WeightUnit> | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetOutputActualWeight',
            anchor: row.sample.anchor,
            actualWeight: value?.value,
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
      id: 'totalVolume',
      header: 'Volume',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: OutputSampleRow) =>
        row.sample.volume?.value ? { value: row.sample.volume.value, unit: row.sample.volume.unit } : null,
      classes: (row) => this.determineClasses(row.sample.volume),
      onSave: (row: OutputSampleRow, value: EnteredValue<VolumeUnit> | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetOutputVolume',
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
      id: 'totalMoles',
      header: 'Actual Moles',
      type: ColumnInputType.UNIT_INPUT,
      field: (row: OutputSampleRow) =>
        row.sample.actualMol?.value
          ? {
              value: row.sample.actualMol.value,
              unit: row.sample.actualMol.unit,
            }
          : null,
      classes: (row) => this.determineClasses(row.sample.actualMol),
      onSave: (row: OutputSampleRow, value: EnteredValue<MolUnit> | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetOutputActualMol',
            anchor: row.sample.anchor,
            actualMol: value?.value,
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
      id: 'molarity',
      header: 'Molarity',
      type: ColumnInputType.TEXT,
      field: (row: OutputSampleRow) => row.sample.molarity?.value?.toString() ?? null,
      classes: (row) => this.determineClasses(row.sample.molarity),
      editable: () => false,
    },
    {
      id: 'yield',
      header: 'Yield',
      type: ColumnInputType.NUMBER,
      field: (row: OutputSampleRow) => row.sample.yield?.value?.toString() ?? null,
      classes: (row) => this.determineClasses(row.sample.yield),
      editable: () => false,
    },
    {
      id: 'purity',
      header: 'Purity',
      type: ColumnInputType.NUMBER,
      field: (row: OutputSampleRow) => row.sample.purity?.value?.toString() ?? null,
      classes: (row) => this.determineClasses(row.sample.purity),
      onSave: (row: OutputSampleRow, value: string | null) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetOutputPurity',
            anchor: row.sample.anchor,
            purity: value,
          })
          .subscribe({});
      },
    },
    {
      id: 'syncWithProducts',
      header: '',
      type: ColumnInputType.ICON,
      field: () => null,
      iconClasses: () => ['indicon-link', 'text-[20px]', 'text-blue-600'],
      tooltip: () => 'Sync with Products',
      editable: (row: OutputSampleRow) => !row.output.intended,
      onSave: (row: OutputSampleRow) => {
        this.experimentDetailService
          .updateDataModel({
            type: 'SetOutputRowIntended',
            anchor: row.output.anchor,
            intended: true,
          })
          .subscribe({});
      },
    },
    {
      id: 'register',
      header: '',
      type: ColumnInputType.ICON,
      field: () => null,
      iconClasses: (row: OutputSampleRow) => [
        'indicon-add',
        'text-[20px]',
        row.sample.registrationStatus in [null, SampleRegistrationStatus.FAILED] ? 'text-green-600' : 'text-green-100',
      ],
      tooltip: () => 'Register Sample',
      editable: (row: OutputSampleRow) => row.sample.registrationStatus == null,
      onSave: (row: OutputSampleRow) => {
        const error = (() => {
          switch (row.sample.registrationStatus) {
            case SampleRegistrationStatus.REGISTERED:
              return 'Sample is already registered';
            case SampleRegistrationStatus.IN_PROGRESS:
              return 'Sample is already sent for registration';
            default:
              return null;
          }
        })();
        if (error != null) {
          this.notificationService.notify({
            type: NotificationType.Error,
            message: error,
            isInline: false,
          });
          return;
        }
        this.experimentDetailService
          .updateDataModel({
            type: 'RegisterSample',
            anchor: row.sample.anchor,
          })
          .subscribe({});
      },
    },
  ]);

  displayedColumns = computed(() => this.columns().map((col) => col.id));

  expandableConfig = computed<ExpandableConfig<OutputSampleRow>>(() => ({
    enabled: true,
    component: BatchDetailPanelComponent,
    getRowData: (row) =>
      ({
        sample: row.sample,
        output: row.output,
        reaction: this.reaction()!,
        experimentId: this.experimentId() || '',
      }) as BatchDetailData,
  }));

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

  private determineClasses(value?: EnteredValue<unknown>): string[] {
    return determineCellClasses(value, this.experimentDetailService.updatedNodes());
  }

  getProductMenuLabel(output: ReactionOutput, index: number): string {
    return output.outputName?.trim() || output.chemicalName?.trim() || `P${index + 1}`;
  }

  addBatchForOutput(output: ReactionOutput) {
    this.experimentDetailService
      .updateDataModel({
        type: 'AddProductSample',
        anchor: output.anchor,
      })
      .subscribe({});
  }

  addNoProductBatch() {
    this.experimentDetailService
      .updateDataModel({
        type: 'AddNoProductSample',
        anchor: this.reaction().anchor,
      })
      .subscribe({});
  }

  importSDF() {
    openFileDialog('.sdf')
      .pipe(
        switchMap((file) => {
          const formData = new FormData();
          formData.append('file', file);
          const operation = this.apiService.request<MutationResponse>(
            'post',
            `experiments/${this.experimentId()}/datamodel/reactions/${this.reactionAnchor()}/importSDF`,
            formData,
          );
          return this.experimentDetailService.updateDataModel2(operation);
        }),
      )
      .subscribe({});
  }
}
