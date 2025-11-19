import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserSelectComponent } from '@core/components/common/user-multiselect/user-select.component';
import { DictionarySelectComponent } from '@core/components/common/dictionary-select/dictionary-select.component';
import { BuiltInDictionary, DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { MatDialog } from '@angular/material/dialog';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { GlobalSearchEntityType, NumericSearch, TextSearch } from '@core/types/entities/experiments/search.i';
import { UserMetadata } from '@core/types/entities/user.i';
import { NumericSearchComponent } from '@core/components/common/numeric-search/numeric-search.component';
import { TextSearchComponent } from '@core/components/common/text-search/text-search.component';
import { GlobalSearchComponent } from '@pages/search/sample-search/global-search.component';
import { ReactionRole, ReactionRoleNames } from '@core/types/entities/experiments/experiment-shared.i';
import { ExperimentStatus, ExperimentStatusNames } from '@core/enums/experiment-status.enum';
import { EnumSelectComponent } from '@core/components/common/enum-select/enum-select.component';

@Component({
  selector: 'eln-test',
  templateUrl: './test.component.html',
  imports: [CommonModule, UserSelectComponent, DictionarySelectComponent, ReactiveFormsModule, NumericSearchComponent, TextSearchComponent, EnumSelectComponent],
  standalone: true,
})
export class TestComponent implements OnInit {
  dialog = inject(MatDialog);
  form = new FormGroup({
    user: new FormControl<UserMetadata | null>(null),
    therapeuticArea: new FormControl<DictionaryItemRef | null>(null),
    projectCode: new FormControl<DictionaryItemRef | null>(null),
    molecularFormula: new FormControl<TextSearch | null>(null),
    molWeight: new FormControl<NumericSearch | null>(null),
    experimentStatus: new FormControl<ExperimentStatus | null>(null),
    reactionRole: new FormControl<ReactionRole[]>([]),
  });
  userValue: any = null;
  therapeuticAreaValue: any = null;
  projectCodeValue: any = null;
  molecularFormulaValue: any = null;
  molWeightValue: any = null;
  experimentStatusValue: any = null;
  reactionRoleValue: any = null;

  searchResults = [
    {type: GlobalSearchEntityType.PROJECT, name: 'Project1', fragment: 'description <em>with matched parts</em> highlighted', reactionRoles: null},
    {type: GlobalSearchEntityType.PROJECT, name: 'Project2', fragment: 'description <em>with matched parts</em> highlighted', reactionRoles: null},
    {type: GlobalSearchEntityType.NOTEBOOK, name: '00000001', fragment: 'description <em>with matched parts</em> highlighted', reactionRoles: null},
    {type: GlobalSearchEntityType.NOTEBOOK, name: '00000002', fragment: 'description <em>with matched parts</em> highlighted', reactionRoles: null},
    {type: GlobalSearchEntityType.EXPERIMENT, name: '00000001-001', fragment: 'description <em>with matched parts</em> highlighted', reactionRoles: [ReactionRole.REACTANT, ReactionRole.CATALYST]},
    {type: GlobalSearchEntityType.EXPERIMENT, name: '00000002-002', fragment: 'description <em>with matched parts</em> highlighted', reactionRoles: null},
  ]

  ngOnInit() {
    this.dialog.open(GlobalSearchComponent);
    this.form.valueChanges.subscribe(v => {
      this.userValue = v.user
      this.therapeuticAreaValue = v.therapeuticArea;
      this.projectCodeValue = v.projectCode;
      this.molecularFormulaValue = v.molecularFormula;
      this.molWeightValue = v.molWeight;
      this.experimentStatusValue = v.experimentStatus;
      this.reactionRoleValue = v.reactionRole;
    })
  }

  BuiltInDictionary = BuiltInDictionary;
  GlobalSearchEntityType = GlobalSearchEntityType;
  ExperimentStatus = ExperimentStatus;
  ExperimentStatusNames = ExperimentStatusNames;
  ReactionRole = ReactionRole;
  ReactionRoleNames = ReactionRoleNames;
}
