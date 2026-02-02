import { CommonModule } from '@angular/common';
import {
  Component,
  DestroyRef,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
  ViewChild,
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
  Sample,
  StructuralSearchType,
  TextSearch,
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
import { MatChipRow, MatChipSet } from '@angular/material/chips';
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
import { ReactionAnchor } from '@core/types/entities/experiments/mutation.i';
import { distinctUntilChanged } from 'rxjs';
import { map } from 'rxjs/operators';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { DictionarySelectComponent } from '@core/components/common/dictionary-select/dictionary-select.component';
import {
  dictionarySearchSummary,
  isFormValueNotEmpty,
  numericSearchSummary,
  setEnabled,
  textSearchSummary,
} from '@core/utils/search.util';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';

export interface SampleSearchDialogData {
  experimentId: UUID;
  reactionAnchor: ReactionAnchor;
}
import { MatIcon, MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';

@Component({
  standalone: true,
  selector: 'eln-sample-search',
  styleUrls: ['./sample-search.component.scss'],
  imports: [
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    MatRadioGroup,
    MatRadioButton,
    MatExpansionPanel,
    MatExpansionPanelHeader,
    MatExpansionPanelTitle,
    TextSearchComponent,
    NumericSearchComponent,
    MatProgressSpinner,
    ExpandableTableComponent,
    ColumnDefDirective,
    ApiImageComponent,
    InfiniteLoaderComponent,
    MatTooltip,
    ToggleComponent,
    MatTabGroup,
    MatTab,
    MatIcon,
    DictionarySelectComponent,
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './sample-search.component.html',
})
export class SampleSearchComponent implements OnInit {
  @Input() experimentId: UUID;
  @Input() reactionAnchor: ReactionAnchor;

  @Output() close = new EventEmitter<void>();

  loader: InfiniteSearchLoader<FindSamplesRequest, Sample>;

  @ViewChild('advancedSearchPanel') advancedSearchPanel: MatExpansionPanel;

  apiService = inject(ApiService);
  destroyRef = inject(DestroyRef);
  dialog = inject(MatDialog);
  experimentDetailService = inject(ExperimentDetailService);

  title = 'Add Material';

  form = new FormGroup({
    quickSearch: new FormControl<string | null>(null),
    structureSearchType: new FormControl<StructuralSearchType>(
      StructuralSearchType.SUBSTRUCTURE,
    ),
    structure: new FormControl<string | null>(null),
    compoundKey: new FormControl<TextSearch | null>(null),
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
  structureImage: string | null = null;
  formNotEmpty = false;

  advancedSearchSummary: string[] | null = null;

  ngOnInit(): void {
    this.loader = new InfiniteSearchLoader<FindSamplesRequest, Sample>(
      (searchParams, pageNo) =>
        this.apiService.request(
          'post',
          `samples/search?pageNo=${pageNo}&pageSize=20`,
          searchParams,
        ),
    );
    this.form.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((formValues) => {
        setEnabled(
          this.form.get('structureSearchType'),
          formValues.structure != null,
          false,
        );
        this.formNotEmpty = Object.entries(formValues)
          .filter(([k, _]) => k !== 'structureSearchType' && k !== 'marked')
          .some(([_, v]) => isFormValueNotEmpty(v));
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
        textSearchSummary('Compound ID', this.form.value.compoundKey),
        textSearchSummary('NBK Batch Number', this.form.value.nbkBatchNumber),
        textSearchSummary(
          'Molecular Formula',
          this.form.value.molecularFormula,
        ),
        numericSearchSummary('Molecular Weight', this.form.value.molWeight),
        textSearchSummary('Chemical Name', this.form.value.chemicalName),
        dictionarySearchSummary(
          'Compound State',
          this.form.value.compoundState,
        ),
        textSearchSummary('Batch Comment', this.form.value.batchComment),
        dictionarySearchSummary(
          'Health Hazards',
          this.form.value.healthHazards,
        ),
        textSearchSummary('CAS Number', this.form.value.casNumber),
      ];
      parts = parts.filter((part) => part != null);
      this.advancedSearchSummary = parts;
    } else {
      this.advancedSearchSummary = null;
    }
  }

  performSearch() {
    const formValue = this.form.value;
    const {
      compoundKey,
      nbkBatchNumber,
      molecularFormula,
      molWeight,
      chemicalName,
      compoundState,
      batchComment,
      healthHazards,
      casNumber,
      marked,
    } = formValue;
    const body: FindSamplesRequest = {
      quickSearch: formValue.quickSearch || null,
      structure:
        formValue.structure != null
          ? { type: formValue.structureSearchType, query: formValue.structure }
          : null,
      compoundKey,
      nbkBatchNumber,
      molecularFormula,
      molWeight,
      chemicalName,
      compoundState,
      batchComment,
      healthHazards,
      casNumber,
      marked,
    };
    this.loader.search(body);
    this.advancedSearchPanel.close();
  }

  markSample(sample: Sample, mark: boolean) {
    this.apiService
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
    const mutation = {
      type: 'AddInput' as const,
      anchor: this.reactionAnchor,
      sampleId: sample.id,
    };

    this.experimentDetailService.updateDataModel(mutation).subscribe({
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
        molFile: this.form.get('structure').value,
      },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.form.get('structure').setValue(result.molFile);
        this.structureImage = URL.createObjectURL(result.molFileImage);
      }
    });
  }

  clearStructure() {
    this.form.get('structure').setValue(null);
    this.structureImage = null;
  }

  clearInput(inputName: string) {
    this.form.get(inputName)?.setValue(null);
  }

  BuildInDictionary = BuiltInDictionary;
}
