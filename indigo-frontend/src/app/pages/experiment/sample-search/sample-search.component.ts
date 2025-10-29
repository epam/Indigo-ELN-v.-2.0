import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { InputComponent } from '@core/components/common/input/input.component';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';
import {
  FindSamplesResult,
  NumericSearch,
  NumericSearchTypeNames,
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
import { MatTableDataSource } from '@angular/material/table';
import {
  ColumnDefDirective,
  ExpandableTableComponent,
} from '@core/components/common/expandable-table/expandable-table.component';
import { PaginatedResponse } from '@core/types/response/paginated-response.i';
import { ImageComponent } from '@core/components/common/image/image.component';

@Component({
  standalone: true,
  selector: 'eln-project-add',
  imports: [
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    FormDialogComponent,
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
    ImageComponent,
  ],
  templateUrl: './sample-search.component.html',
})
export class SampleSearchComponent implements OnInit {
  dialogRef = inject(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  title = 'Add material';

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
  });

  advancedSearchSummary: string[][] | null = null;
  compoundStateOptions: DictionaryItemRef[];
  healthHazardsOptions: DictionaryItemRef[];

  searchStarted = false;
  loading = false;
  results = new MatTableDataSource<FindSamplesResult>();

  constructor(protected service: ApiService<any>) {}

  ngOnInit(): void {
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
        this.textSearchSummary('Batch Comment', this.form.value.batchComment),
        this.textSearchSummary('CAS Number', this.form.value.casNumber),
      ];
      parts = parts.filter((part) => part != null);
      this.advancedSearchSummary = parts;
    } else {
      this.advancedSearchSummary = null;
    }
  }

  performSearch() {
    const body = this.form.value;
    this.searchStarted = true;
    this.loading = true;
    this.service
      .request<
        PaginatedResponse<FindSamplesResult>
      >('post', 'samples/search', body)
      .subscribe({
        next: (results) => {
          console.log(results);
          this.results.data = results.items;
        },
        error: (err) => {
          console.error('Failed to perform search:', err);
        },
        complete: () => {
          this.loading = false;
        },
      });
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
}
