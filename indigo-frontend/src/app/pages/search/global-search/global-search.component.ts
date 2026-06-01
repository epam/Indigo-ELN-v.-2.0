import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, DestroyRef, inject, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { InputComponent } from '@core/components/common/input/input.component';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';
import {
  GlobalSearchEntityType,
  GlobalSearchRequest,
  NumericSearch,
  StructuralSearchType,
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
import { ApiService } from '@core/services/api.service';
import { InfiniteLoaderComponent } from '@core/components/util/infinite-loader/infinite-loader.component';
import { GlobalSearchLoader } from '@core/components/util/infinite-scroll-search';
import {
  StructureEditorModalComponent,
  StructureEditorModalResult,
} from '@core/components/experiment/structure-editor-modal/structure-editor-modal.component';
import { ReactionRole, ReactionRoleNames } from '@core/types/entities/experiments/experiment-shared.i';
import { ReactionAnchor } from '@core/types/entities/experiments/mutation.i';
import { UserRef } from '@core/types/entities/user.i';
import { UserSelectComponent } from '@core/components/common/user-multiselect/user-select.component';
import { DictionarySelectComponent } from '@core/components/common/dictionary-select/dictionary-select.component';
import { ExperimentStatus, ExperimentStatusNames } from '@core/enums/experiment-status.enum';
import { MatDivider } from '@angular/material/divider';
import { ApiImageComponent } from '@core/components/common/image/api-image.component';
import { EnumSelectComponent } from '@core/components/common/enum-select/enum-select.component';
import { UserService } from '@core/services/user.service';
import { first } from 'rxjs';
import {
  dictionarySearchSummary,
  enumSearchSummary,
  isFormValueNotEmpty,
  numericSearchSummary,
  setEnabled,
} from '@core/utils/search.util';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

export interface GlobalSearchDialogData {
  reactionAnchor: ReactionAnchor;
  initialQuery?: string;
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
    InfiniteLoaderComponent,
    UserSelectComponent,
    DictionarySelectComponent,
    MatChipRow,
    MatChipSet,
    MatDivider,
    ApiImageComponent,
    EnumSelectComponent,
  ],
  templateUrl: './global-search.component.html',
})
export class GlobalSearchComponent implements OnInit, AfterViewInit {
  data: GlobalSearchDialogData = inject(MAT_DIALOG_DATA);

  loader: GlobalSearchLoader;

  @ViewChild('advancedSearchPanel') advancedSearchPanel: MatExpansionPanel;

  apiService = inject(ApiService);
  dialog = inject(MatDialog);
  userService = inject(UserService);
  destroyRef = inject(DestroyRef);

  title = 'Search';

  form = new FormGroup({
    quickSearch: new FormControl<string | null>(null),
    structureSearchType: new FormControl<StructuralSearchType>(StructuralSearchType.SUBSTRUCTURE),
    isReaction: new FormControl<boolean | null>(null),
    structure: new FormControl<string | null>(null),
    therapeuticArea: new FormControl<DictionaryItemRef | null>(null),
    projectCode: new FormControl<DictionaryItemRef | null>(null),
    batchYield: new FormControl<NumericSearch | null>(null),
    batchPurity: new FormControl<NumericSearch | null>(null),
    author: new FormControl<UserRef[] | null>(null),
    experimentStatus: new FormControl<ExperimentStatus>(null),
    reactionRole: new FormControl<ReactionRole>(null),
  });
  structureImage: string | null = null;
  formNotEmpty = false;

  advancedSearchSummary: string[] | null = null;

  ngOnInit(): void {
    this.loader = new GlobalSearchLoader(this.apiService);
    this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((formValues) => {
      setEnabled(this.form.get('structureSearchType'), formValues.isReaction !== null, false);
      setEnabled(this.form.get('reactionRole'), formValues.isReaction === false, false);
      this.formNotEmpty = Object.entries(formValues)
        .filter(([k, _]) => k !== 'structureSearchType')
        .some(([_, v]) => isFormValueNotEmpty(v));
    });
  }

  ngAfterViewInit(): void {
    if (this.data?.initialQuery) {
      this.form.get('quickSearch').setValue(this.data.initialQuery);
      this.performSearch();
    }
  }

  updateAdvancedSearchSummary(show: boolean) {
    let formValue = this.form.value;
    if (show) {
      let parts = [
        dictionarySearchSummary('Therapeutic Area', formValue.therapeuticArea),
        dictionarySearchSummary('Project Code', formValue.projectCode),
        numericSearchSummary('Batch Yield, %', formValue.batchYield),
        numericSearchSummary('Batch Purity, %', formValue.batchPurity),
        dictionarySearchSummary('Author', formValue.author),
        enumSearchSummary('Experiment Status', formValue.experimentStatus, ExperimentStatusNames),
        enumSearchSummary('Reaction Role', formValue.reactionRole, ReactionRoleNames),
      ];
      parts = parts.filter((part) => part != null);
      this.advancedSearchSummary = parts;
    } else {
      this.advancedSearchSummary = null;
    }
  }

  addMeAsAuthor() {
    this.userService.user$.pipe(first()).subscribe((user) => {
      let selectedUsers = this.form.get('author').value || [];
      if (!selectedUsers.some((x) => x.username === user.username)) {
        this.form.get('author').setValue([
          ...selectedUsers,
          {
            username: user.username,
            displayName: user.displayName,
          },
        ]);
      }
    });
  }

  performSearch() {
    const formValue = this.form.value;
    const structureSearch = {
      type: formValue.structureSearchType,
      query: formValue.structure,
    };
    const { therapeuticArea, projectCode, author, batchYield, batchPurity, reactionRole } = formValue;
    const body: GlobalSearchRequest = {
      query: formValue.quickSearch || null,
      moleculeStructure: formValue.isReaction === false ? structureSearch : null,
      reactionStructure: formValue.isReaction === true ? structureSearch : null,
      experimentStatus: formValue.experimentStatus != null ? [formValue.experimentStatus] : null,
      therapeuticArea,
      projectCode,
      author,
      batchYield,
      batchPurity,
      reactionRole,
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
    dialogRef.afterClosed().subscribe((result: StructureEditorModalResult) => {
      if (result?.success) {
        const isReaction = result.isReaction;
        this.form.get('isReaction').setValue(isReaction);
        this.form.get('structure').setValue(result.molOrRxnFile);
        this.structureImage = URL.createObjectURL(result.image);
      }
    });
  }

  clearStructure() {
    this.form.get('isReaction').setValue(null);
    this.form.get('structure').setValue(null);
    this.structureImage = null;
  }

  BuildInDictionary = BuiltInDictionary;
  GlobalSearchEntityType = GlobalSearchEntityType;
  ExperimentStatus = ExperimentStatus;
  ExperimentStatusNames = ExperimentStatusNames;
  ReactionRole = ReactionRole;
  ReactionRoleNames = ReactionRoleNames;
}
