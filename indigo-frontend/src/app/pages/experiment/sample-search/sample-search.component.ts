import { CommonModule } from '@angular/common';
import {
  Component,
  DestroyRef,
  inject,
  OnInit,
  ViewChild,
  Input,
  Output,
  EventEmitter,
} from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { InputComponent } from '@core/components/common/input/input.component';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';
import {
  FindSamplesRequest,
  NumericSearch,
  NumericSearchTypeNames,
  Sample,
  StructuralSearchType,
  TextSearch,
  TextSearchTypeNames,
} from '@core/types/entities/experiments/search.i';
import {
  MatExpansionPanel,
  MatExpansionPanelDescription,
  MatExpansionPanelHeader,
  MatExpansionPanelTitle,
} from '@angular/material/expansion';
import { TextSearchComponent } from '@core/components/common/text-search/text-search.component';
import {
  BuiltInDictionary,
  DictionaryItemRef,
} from '@core/types/entities/dictionary.i';
import { NumericSearchComponent } from '@core/components/common/numeric-search/numeric-search.component';
import { DropdownValueComponent } from '@core/components/experiment/dropdown-value/dropdown-value.component';
import { MatChip } from '@angular/material/chips';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import {
  ColumnDefDirective,
  ExpandableTableComponent,
} from '@core/components/common/expandable-table/expandable-table.component';
import { ApiImageComponent } from '@core/components/common/image/api-image.component';
import { ApiService } from '@core/services/api.service';
import { InfiniteLoaderComponent } from '@core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteSearchLoader } from '@core/components/util/infinite-scroll-search';
import { MatTooltip } from '@angular/material/tooltip';
import { ToggleComponent } from '@core/components/common/toggle/toggle.component';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { StructureEditorModalComponent } from '@core/components/experiment/structure-editor-modal/structure-editor-modal.component';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { MutateModelForm } from '@core/types/entities/experiments/experiment-mutate-form.i';
import { ExperimentModelService } from '@core/services/experiment/experiment-model.service';
import { ReactionAnchor } from '@core/types/entities/experiments/mutation.i';
import { distinctUntilChanged } from 'rxjs';
import { map } from 'rxjs/operators';
import { MatButton } from '@angular/material/button';
import { MatTab, MatTabGroup } from '@angular/material/tabs';

@Component({
  standalone: true,
  selector: 'eln-sample-search',
  imports: [
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    InputComponent,
    MatRadioGroup,
    MatRadioButton,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    MatExpansionPanelDescription,
    TextSearchComponent,
    NumericSearchComponent,
    DropdownValueComponent,
    MatChip,
    MatProgressSpinner,
    ExpandableTableComponent,
    ColumnDefDirective,
    ApiImageComponent,
    InfiniteLoaderComponent,
    MatTooltip,
    ToggleComponent,
    MatButton,
    MatTabGroup,
    MatTab,
  ],
  templateUrl: './sample-search.component.html',
})
export class SampleSearchComponent implements OnInit {

  @Input() experimentId: UUID;
  @Input() reactionAnchor: ReactionAnchor;

  @Output() close = new EventEmitter<void>();

  loader: InfiniteSearchLoader<FindSamplesRequest, Sample>;

  @ViewChild('advancedSearchPanel') advancedSearchPanel: MatExpansionPanel;

  service = inject(ApiService);
  destroyRef = inject(DestroyRef);
  dialog = inject(MatDialog);
  experimentModelService = inject(ExperimentModelService);

  title = 'Add Material';

  form = new FormGroup({
    quickSearch: new FormControl<string | null>(null),
    structureSearchType: new FormControl<StructuralSearchType>(
      StructuralSearchType.EXACT,
    ),
    strCode: new FormControl<TextSearch | null>(null),
    nbkBatchNumber: new FormControl<TextSearch | null>(null),
    molecularFormula: new FormControl<TextSearch | null>(null),
    molWeight: new FormControl<NumericSearch | null>(null),
    chemicalName: new FormControl<TextSearch | null>(null),
    compoundState: new FormControl<DictionaryItemRef | null>(null),
    batchComment: new FormControl<TextSearch | null>(null),
    healthHazards: new FormControl<DictionaryItemRef | null>(null),
    casNumber: new FormControl<TextSearch | null>(null),
    marked: new FormControl<boolean>(false),
  });
  structureMolFile: string | null = null;
  structureImage: string | null = null;

  advancedSearchSummary: string[][] | null = null;
  compoundStateOptions: DictionaryItemRef[];
  healthHazardsOptions: DictionaryItemRef[];

  ngOnInit(): void {
    this.loader = new InfiniteSearchLoader<FindSamplesRequest, Sample>(
      (searchParams, pageNo) =>
        this.service.request(
          'post',
          `samples/search?pageNo=${pageNo}&pageSize=20`,
          searchParams,
        ),
    );
    this.service
      .request<
        DictionaryItemRef[]
      >('get', `dictionaries/${BuiltInDictionary.COMPONENT_STATE}`)
      .subscribe((list) => {
        this.compoundStateOptions = list;
      });
    this.service
      .request<
        DictionaryItemRef[]
      >('get', `dictionaries/${BuiltInDictionary.HEALTH_HAZARD}`)
      .subscribe((list) => {
        this.healthHazardsOptions = list;
      });

    this.form.valueChanges
      .pipe(
        map((form) => form.marked),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          if (this.loader.started) {
            this.performSearch();
          }
        },
      });
  }

  updateAdvancedSearchSummary(show: boolean) {
    if (show) {
      let parts = [
        this.textSearchSummary('Compound ID', this.form.value.strCode),
        this.textSearchSummary(
          'NBK Batch Number',
          this.form.value.nbkBatchNumber,
        ),
        this.textSearchSummary(
          'Molecular Formula',
          this.form.value.molecularFormula,
        ),
        this.numericSearchSummary(
          'Molecular Weight',
          this.form.value.molWeight,
        ),
        this.textSearchSummary('Chemical Name', this.form.value.chemicalName),
        this.dictionarySearchSummary(
          'Compound State',
          this.form.value.compoundState,
        ),
        this.textSearchSummary('Batch Comment', this.form.value.batchComment),
        this.dictionarySearchSummary(
          'Health Hazards',
          this.form.value.healthHazards,
        ),
        this.textSearchSummary('CAS Number', this.form.value.casNumber),
      ];
      parts = parts.filter((part) => part != null);
      this.advancedSearchSummary = parts;
    } else {
      this.advancedSearchSummary = null;
    }
  }

  performSearch() {
    const body: FindSamplesRequest = {
      ...this.form.value,
      quickSearch: this.form.value.quickSearch || null,
      marked: this.form.value.marked ? true : null,
      structure: this.structureMolFile
        ? {
            type: this.form.value.structureSearchType,
            query: this.structureMolFile,
          }
        : null,
    };
    this.loader.search(body);
    this.advancedSearchPanel.close();
  }

  markSample(sample: Sample, mark: boolean) {
    this.service
      .request<Sample>(
        'post',
        `samples/${sample.id}/${mark ? 'mark' : 'unmark'}`,
      )
      .subscribe({
        next: (response) =>
          this.loader.replace((s) => s.id === sample.id, response),
        error: (error) => {
          console.error('Failed to mark/unmark sample: ', error);
        },
      });
  }

  addToExperiment(sample: Sample) {
    // TODO should probably be done via experiment screen to lock entire screen; currently the only mutation implemented is done at ReactionSchemaViewComponent
    console.log(
      'addToExperiment',
      this.experimentModelService,
      this.experimentModelService.experimentModel(),
    );
    const payload: MutateModelForm = {
      model: this.experimentModelService.experimentModel(),
      mutation: {
        type: 'AddInput',

        anchor: this.reactionAnchor,
        sampleId: sample.id,
      },
    };

    this.experimentModelService
      .updateDataModel(this.experimentId, payload)
      .subscribe({
        next: () => {
          console.log('Model updated with new sample');

          this.close.emit();
        },
        error: (error) => {
          console.error('Failed to update experiment model:', error);
        },
      });
  }

  editStructure() {
    const dialogRef = this.dialog.open(StructureEditorModalComponent, {
      width: '90vw',
      height: '80vh',
      maxWidth: '1200px',
      maxHeight: '800px',
      disableClose: false,
      data: {
        height: '600px',
        width: '100%',
        isReaction: false,
        molFile: this.structureMolFile,
      },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.structureMolFile = result.molFile;
        this.structureImage = URL.createObjectURL(result.molFileImage);
      }
    });
  }

  clearStructure() {
    this.structureMolFile = null;
    this.structureImage = null;
  }

  private textSearchSummary(
    name: string,
    search: TextSearch | null,
  ): string[] | null {
    if (search == null) {
      return null;
    } else if (search.type !== 'between') {
      return [name, `${TextSearchTypeNames[search.type]}: ${search.value}`];
    } else {
      return [
        name,
        `${TextSearchTypeNames[search.type]}: ${search.from} and ${search.to}`,
      ];
    }
  }

  private numericSearchSummary(
    name: string,
    search: NumericSearch | null,
  ): string[] | null {
    if (search == null) {
      return null;
    } else {
      return [name, `${NumericSearchTypeNames[search.type]} ${search.value}`];
    }
  }

  private dictionarySearchSummary(
    name: string,
    value: DictionaryItemRef | null | any,
  ): string[] | null {
    if (value == null) {
      return null;
    } else {
      return [name, value.name];
    }
  }
}
