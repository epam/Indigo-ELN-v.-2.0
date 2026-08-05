import { CommonModule } from '@angular/common';
import { Component, DestroyRef, EventEmitter, inject, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';
import { ButtonComponent } from '@core/components/common/button/button.component';
import {
  FindSamplesRequest,
  NumericSearch,
  Sample,
  SEARCH_CATALOG_MAPPING,
  SearchCatalog,
  SearchCatalogUI,
  StructuralSearchType,
  TextSearch,
} from '@core/types/entities/experiments/search.i';
import { MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle } from '@angular/material/expansion';
import { TextSearchComponent } from '@core/components/common/text-search/text-search.component';
import { BuiltInDictionary, DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { NumericSearchComponent } from '@core/components/common/numeric-search/numeric-search.component';
import { ApiService } from '@core/services/api.service';
import { SamplesSearchLoader } from '@core/components/util/infinite-scroll-search';
import { MatTooltip } from '@angular/material/tooltip';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  StructureEditorModalComponent,
  StructureEditorModalResult,
} from '@core/components/experiment/structure-editor-modal/structure-editor-modal.component';
import { ReactionAnchor } from '@core/types/entities/experiments/mutation.i';
import { DictionarySelectComponent } from '@core/components/common/dictionary-select/dictionary-select.component';
import {
  dictionarySearchSummary,
  isFormValueNotEmpty,
  numericSearchSummary,
  setEnabled,
  textSearchSummary,
} from '@core/utils/search.util';
import { MatIcon, MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { SampleSearchResultsComponent } from '@pages/experiment/sample-search-results/sample-search-results.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { withLoading } from '@core/utils/with-loading';
import { Observable, switchMap, tap } from 'rxjs';

export interface SampleSearchCriteria {
  catalogs?: SearchCatalogUI;
  quickSearch?: string;
  structureSearchType?: StructuralSearchType;
  structure?: string;
  compoundKey?: TextSearch;
  nbkBatchNumber?: TextSearch;
  molecularFormula?: TextSearch;
  molWeight?: NumericSearch;
  chemicalName?: TextSearch;
  compoundState?: DictionaryItemRef;
  batchComment?: TextSearch;
  healthHazards?: DictionaryItemRef;
  casNumber?: TextSearch;
}

const PUBCHEM_DISABLED_CONTROLS = [
  'compoundKey',
  'nbkBatchNumber',
  'molWeight',
  'chemicalName',
  'compoundState',
  'batchComment',
  'healthHazards',
  'casNumber',
];

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
    MatTooltip,
    MatIcon,
    DictionarySelectComponent,
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
    SampleSearchResultsComponent,
    ButtonComponent,
  ],
  templateUrl: './sample-search.component.html',
})
export class SampleSearchComponent implements OnInit {
  @Input() reactionAnchor: ReactionAnchor;
  @Input() defaultCriteria: SampleSearchCriteria | null;
  @Input() defaultImage: string | null;

  @Output() close = new EventEmitter<void>();

  loader: SamplesSearchLoader;

  @ViewChild('advancedSearchPanel') advancedSearchPanel: MatExpansionPanel;

  apiService = inject(ApiService);
  destroyRef = inject(DestroyRef);
  dialog = inject(MatDialog);
  experimentDetailService = inject(ExperimentDetailService);
  notificationService = inject(NotificationService);

  title = 'Add Material';
  isAddingToExperiment = false;
  loadingSampleKey: string | null = null;

  form = new FormGroup({
    catalog: new FormControl<SearchCatalogUI>(SearchCatalogUI.ALL),
    quickSearch: new FormControl<string | null>(null),
    structureSearchType: new FormControl<StructuralSearchType>(StructuralSearchType.SUBSTRUCTURE),
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
  });
  structureImage: string | null = null;
  formNotEmpty = false;

  advancedSearchSummary: string[] | null = null;
  advancedSearchMessage: string | null = null;

  ngOnInit(): void {
    this.loader = new SamplesSearchLoader(this.apiService);
    this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((formValues) => {
      setEnabled(this.form.get('structureSearchType'), formValues.structure != null, false);
      const hasPubChem = SEARCH_CATALOG_MAPPING[formValues.catalog].includes(SearchCatalog.PUBCHEM);

      for (const controlName of PUBCHEM_DISABLED_CONTROLS) {
        setEnabled(this.form.get(controlName), !hasPubChem, false);
      }
      this.advancedSearchMessage = hasPubChem
        ? 'PubChem does not support fine-grained search. Use quick search instead'
        : null;
      this.formNotEmpty = Object.entries(formValues)
        .filter(([k, _]) => k !== 'structureSearchType')
        .some(([_, v]) => isFormValueNotEmpty(v));
    });

    if (this.defaultCriteria) {
      const valuesWithDefaults = {
        ...this.form.getRawValue(),
        ...this.defaultCriteria,
      };
      this.form.setValue(valuesWithDefaults as never);
      this.structureImage = this.defaultImage
        ? URL.createObjectURL(new Blob([this.defaultImage], { type: 'image/svg+xml' }))
        : null;
    }
  }

  updateAdvancedSearchSummary(show: boolean) {
    if (show) {
      let parts = [
        textSearchSummary('Compound ID', this.form.value.compoundKey),
        textSearchSummary('NBK Batch Number', this.form.value.nbkBatchNumber),
        textSearchSummary('Molecular Formula', this.form.value.molecularFormula),
        numericSearchSummary('Molecular Weight', this.form.value.molWeight),
        textSearchSummary('Chemical Name', this.form.value.chemicalName),
        dictionarySearchSummary('Compound State', this.form.value.compoundState),
        textSearchSummary('Batch Comment', this.form.value.batchComment),
        dictionarySearchSummary('Health Hazards', this.form.value.healthHazards),
        textSearchSummary('CAS Number', this.form.value.casNumber),
      ];
      parts = parts.filter((part) => part != null);
      this.advancedSearchSummary = parts;
    } else {
      this.advancedSearchSummary = null;
    }
  }

  performSearch() {
    if (this.loader?.loading) {
      return;
    }

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
    } = formValue;
    const body: FindSamplesRequest = {
      catalogs: SEARCH_CATALOG_MAPPING[formValue.catalog],
      quickSearch: formValue.quickSearch || null,
      structure:
        formValue.structure != null ? { type: formValue.structureSearchType, query: formValue.structure } : null,
      compoundKey,
      nbkBatchNumber,
      molecularFormula,
      molWeight,
      chemicalName,
      compoundState,
      batchComment,
      healthHazards,
      casNumber,
    };
    const hasPubChem = SEARCH_CATALOG_MAPPING[formValue.catalog].includes(SearchCatalog.PUBCHEM);
    if (hasPubChem) {
      for (const controlName of PUBCHEM_DISABLED_CONTROLS) {
        delete body[controlName];
      }
    }
    this.loader.search(body);
    this.advancedSearchPanel.close();
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
    dialogRef.afterClosed().subscribe((result: StructureEditorModalResult) => {
      if (result?.success) {
        this.form.get('structure').setValue(result.molOrRxnFile);
        this.structureImage = URL.createObjectURL(result.image);
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

  addToExperiment(sample: Sample) {
    if (this.isAddingToExperiment) {
      return;
    }

    this.loadingSampleKey = this.getSampleKey(sample);
    const request$ = (
      !sample.id
        ? this.apiService
            .request<Sample>('post', '/samples/importFromSearch', sample)
            .pipe(switchMap((response) => this.doAddToExperiment(response.id)))
        : this.doAddToExperiment(sample.id)
    ).pipe(
      withLoading((loading) => {
        this.isAddingToExperiment = loading;
        if (!loading) {
          this.loadingSampleKey = null;
        }
      }),
    );

    request$.subscribe({
      error: () => {
        this.loadingSampleKey = null;
      },
    });
  }

  doAddToExperiment(sampleID: UUID): Observable<unknown> {
    const mutation = {
      type: 'AddInput' as const,
      anchor: this.reactionAnchor,
      sampleId: sampleID,
    };
    return this.experimentDetailService.updateDataModel(mutation).pipe(
      tap(() => {
        this.notificationService.notify({
          type: NotificationType.Info,
          message: 'Model updated with new sample',
          isInline: false,
        });
      }),
    );
  }

  getSampleKey(sample: Sample): string {
    return sample.id ?? sample.compoundKey ?? sample.name ?? 'new-sample';
  }

  BuildInDictionary = BuiltInDictionary;
}
