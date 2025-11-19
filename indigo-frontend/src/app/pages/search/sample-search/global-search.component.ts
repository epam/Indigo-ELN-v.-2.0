import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { InputComponent } from '@core/components/common/input/input.component';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';
import {
  GlobalSearchEntityType,
  GlobalSearchRequest,
  GlobalSearchResult,
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
import { BuiltInDictionary, DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { NumericSearchComponent } from '@core/components/common/numeric-search/numeric-search.component';
import { MatChipRow, MatChipSet } from '@angular/material/chips';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ApiService } from '@core/services/api.service';
import { InfiniteLoaderComponent } from '@core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteSearchLoader } from '@core/components/util/infinite-scroll-search';
import {
  StructureEditorModalComponent,
} from '@core/components/experiment/structure-editor-modal/structure-editor-modal.component';
import { ReactionRole, ReactionRoleNames, UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { ExperimentModelService } from '@core/services/experiment/experiment-model.service';
import { ReactionAnchor } from '@core/types/entities/experiments/mutation.i';
import { UserMetadata } from '@core/types/entities/user.i';
import { UserSelectComponent } from '@core/components/common/user-multiselect/user-select.component';
import { DictionarySelectComponent } from '@core/components/common/dictionary-select/dictionary-select.component';
import { SelectComponent } from '@core/components/common/select/select.component';
import { DropdownMenuItem } from '@core/components/common/dropdown-menu/dropdown-menu.i';
import { ExperimentStatus, ExperimentStatusNames } from '@core/enums/experiment-status.enum';
import { MatDivider } from '@angular/material/divider';
import { ApiImageComponent } from '@core/components/common/image/api-image.component';
import { EnumSelectComponent } from '@core/components/common/enum-select/enum-select.component';

export interface SampleSearchDialogData {
  experimentId: UUID;
  reactionAnchor: ReactionAnchor;
}

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
    NumericSearchComponent,
    MatProgressSpinner,
    InfiniteLoaderComponent,
    UserSelectComponent,
    DictionarySelectComponent,
    SelectComponent,
    MatChipRow,
    MatChipSet,
    MatDivider,
    ApiImageComponent,
    EnumSelectComponent,
  ],
  templateUrl: './global-search.component.html',
})
export class GlobalSearchComponent implements OnInit {
  data: SampleSearchDialogData = inject(MAT_DIALOG_DATA);

  loader: InfiniteSearchLoader<GlobalSearchRequest, GlobalSearchResult>;

  @ViewChild('advancedSearchPanel') advancedSearchPanel: MatExpansionPanel;

  service = inject(ApiService);
  dialog = inject(MatDialog);
  experimentModelService = inject(ExperimentModelService);

  title = 'Search';

  form = new FormGroup({
    quickSearch: new FormControl<string | null>(null),
    structureSearchType: new FormControl<StructuralSearchType>(
      StructuralSearchType.EXACT,
    ),
    isReaction: new FormControl<boolean | null>(null),
    structure: new FormControl<string | null>(null),
    therapeuticArea: new FormControl<DictionaryItemRef | null>(null),
    projectCode: new FormControl<DictionaryItemRef | null>(null),
    // ...
    batchYield: new FormControl<NumericSearch | null>(null),
    batchPurity: new FormControl<NumericSearch | null>(null),
    author: new FormControl<UserMetadata[] | null>(null),
    // ...
    experimentStatus: new FormControl<ExperimentStatus>(null),
    reactionRole: new FormControl<ReactionRole>(null),
  });
  structureImage: string | null = null;
  formNotEmpty = false;

  advancedSearchSummary: string[] | null = null;

  ngOnInit(): void {
    this.loader = new InfiniteSearchLoader<GlobalSearchRequest, GlobalSearchResult>(
      (searchParams, pageNo) =>
        this.service.request(
          'post',
          `search?pageNo=${pageNo}&pageSize=20`,
          searchParams,
        ),
    );
    this.form.get('isReaction').valueChanges.subscribe(isReaction => {
      if (isReaction != null) {
        this.form.get('structureSearchType').enable();
      } else {
        this.form.get('structureSearchType').disable();
      }
      if (isReaction === false) {
        this.form.get('reactionRole').enable();
      } else {
        this.form.get('reactionRole').disable();
      }
    });
    this.form.valueChanges.subscribe(formValues => {
      this.formNotEmpty = (formValues.quickSearch != null && formValues.quickSearch.trim() !== '')
          || formValues.structure != null
          || formValues.therapeuticArea != null
          || formValues.projectCode != null
          || formValues.batchYield != null
          || formValues.batchPurity != null
          || formValues.author != null
          || formValues.experimentStatus?.length > 0
      console.log('formNotEmpty', this.formNotEmpty, formValues);
    });
    this.form.get('isReaction').setValue(null); // trigger update
  }

  updateAdvancedSearchSummary(show: boolean) {
    let formValue = this.form.value;
    if (show) {
      let parts = [
        this.dictionarySearchSummary('Therapeutic Area', formValue.therapeuticArea),
        this.dictionarySearchSummary('Project Code', formValue.projectCode),
        this.numericSearchSummary('Batch Yield, %', formValue.batchYield),
        this.numericSearchSummary('Batch Purity, %', formValue.batchPurity,),
        this.dictionarySearchSummary('Author', formValue.author),
        this.enumSearchSummary('Experiment Status', formValue.experimentStatus, ExperimentStatusNames),
        this.enumSearchSummary('Reaction Role', formValue.reactionRole, ReactionRoleNames),
      ];
      parts = parts.filter((part) => part != null);
      this.advancedSearchSummary = parts;
    } else {
      this.advancedSearchSummary = null;
    }
  }

  addMeAsAuthor() {
    window.alert('Not implemented!');
  }

  performSearch() {
    const formValue = this.form.value;
    const structureSearch = {type: formValue.structureSearchType, query: formValue.structure};
    const {therapeuticArea, projectCode, author, batchYield, batchPurity, reactionRole} = formValue;
    const body: GlobalSearchRequest = {
      query: formValue.quickSearch || null,
      moleculeStructure: formValue.isReaction === false ? structureSearch : null,
      reactionStructure: formValue.isReaction === true ? structureSearch : null,
      experimentStatus: formValue.experimentStatus != null ? [formValue.experimentStatus] : null,
      therapeuticArea, projectCode, author, batchYield, batchPurity, reactionRole
    };
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
        isReaction: null,
        molFile: this.form.get('structure').value,
      },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        let isReaction = result.rxnFile != null;
        this.form.get('isReaction').setValue(isReaction);
        this.form.get('structure').setValue(isReaction ? result.rxnFile : result.molFile);
        this.structureImage = URL.createObjectURL(isReaction ? result.rxnFileImage : result.molFileImage);
      }
    });
  }

  clearStructure() {
    this.form.get('isReaction').setValue(null);
    this.form.get('structure').setValue(null);
    this.structureImage = null;
  }

  private textSearchSummary(
    name: string,
    search: TextSearch | null,
  ): string | null {
    if (search?.type === 'between') {
      return `<b>${name}</b>&ensp;${TextSearchTypeNames[search.type]}&ensp;${search.from}&nbsp;and&nbsp;${search.to}`;
    } else if (search != null) {
      return `<b>${name}</b>&ensp;${TextSearchTypeNames[search.type]}&ensp;${search.value}`;
    }
    return null;
  }

  private numericSearchSummary(
    name: string,
    search: NumericSearch | null,
  ): string | null {
    if (search != null) {
      return `<b>${name}</b>&ensp;${NumericSearchTypeNames[search.type]}&ensp;${search.value}`;
    }
    return null;
  }

  private dictionarySearchSummary(
    name: string,
    value: DictionaryItemRef | DictionaryItemRef[] | UserMetadata | UserMetadata[] | null,
  ): string | null {
    if (value != null) {
      const array = Array.isArray(value) ? value : [value];
      if (array.length) {
        return `<b>${name}</b>&ensp;is&ensp;${array.map(this.referenceDisplayName).join('&ensp;or&ensp;')}`;
      }
    }
    return null;
  }

  private enumSearchSummary<T extends string>(
    name: string,
    value: T | T[] | null,
    enumNames: Record<T, string>
  ): string | null {
    if (value != null) {
      const array = Array.isArray(value) ? value : [value];
      if (array.length) {
        return `<b>${name}</b>&ensp;is&ensp;${array.map(v => enumNames[v]).join('&ensp;or&ensp;')}`;
      }
    }
    return null;
  }

  private referenceDisplayName(ref: any) {
    return ref.username || ref.name;
  }

  BuildInDictionary = BuiltInDictionary;
  GlobalSearchEntityType = GlobalSearchEntityType
  ExperimentStatus = ExperimentStatus;
  ExperimentStatusNames = ExperimentStatusNames;
  ReactionRole = ReactionRole;
  ReactionRoleNames = ReactionRoleNames;
}
