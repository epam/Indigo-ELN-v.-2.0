import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { ExperimentImageService } from '@/core/services/experiment/experiment-image.service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { CdkAccordionModule } from '@angular/cdk/accordion';
import { ExperimentModelService } from '@core/services/experiment/experiment-model.service';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { ReactionViewComponent } from '@pages/experiment/stoichiometry/reaction-view/reaction-view.component';
import { SampleSearchComponent } from '@pages/experiment/sample-search/sample-search.component';
import { FormlyFieldConfig, FormlyModule } from '@ngx-formly/core';
import { FormControlName, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { DictionarySelectComponent } from '@/core/components/common/dictionary-select/dictionary-select.component';
import { BuiltInDictionary } from '@/core/types/entities/dictionary.i';
@Component({
  selector: 'eln-experiment-info',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatProgressSpinner,
    CdkAccordionModule,
    ReactionViewComponent,
    ButtonComponent,
    SampleSearchComponent,
    FormlyModule,
    ReactiveFormsModule,
  ],
  providers: [ExperimentImageService],
  templateUrl: './experiment-info.component.html',
})
export class ExperimentInfoComponent implements OnInit {
  experimentDetailService = inject(ExperimentDetailService);
  experimentModelService = inject(ExperimentModelService);
  experimentImageService = inject(ExperimentImageService);

  isUpdating = signal<boolean>(false);

  isDrawerOpen = signal<boolean>(false);
  isDrawerClosedOnce = signal<boolean>(false);

  experiment = computed(() => this.experimentDetailService.experimentDetail());
  isLoading = computed(() => this.experimentDetailService.isLoading());
  hasError = computed(() => this.experimentDetailService.hasError());
  model = computed(() => this.experimentModelService.experimentModel());
  modelLoading = computed(() => this.experimentModelService.isLoading());
  modelError = computed(() => this.experimentModelService.hasError());
  experimentImageUrl = computed(() => this.experimentImageService.imageUrl());
  imageLoading = computed(
    () => this.experimentImageService.isLoading() || this.isUpdating(),
  );
  imageError = computed(() => this.experimentImageService.hasError());

  form = new FormGroup({});
  fields: FormlyFieldConfig[] = [
    {
      fieldGroupClassName: "grid grid-col-2 gap-[16px]",
      fieldGroup: [
        {
          type: "input",
          key: "title",
          name: "title",
          props: {
            label: "Experiment Title",
            placeholder: "Experiment Title",
          }
        },
        {
          type: "chip-grid",
          key: "linkedExperiment",
          props: {
            label: "Linked Experiment",
            placeholder: "Linked Experiment",
          }
        },
        {
          type: "select",
          key: "therapeutic",
          props: {
            label: "Therapeutic Area",
            multiple: false,
            required: false,
            dictionaryId: BuiltInDictionary.THERAPEUTIC_AREA
          }
        }, 
        {
          type: "chip-grid",
          key: "contToRxn",
          props: {
            label: "Cont. TO Rxn.",
            placeholder: "Cont. TO Rxn."
          }
        },
        {
          type: "select",
          key: "codeAndName",
          props: {
            label: "Project Code & Name",
            placeholder: "PROJECT_CODE",
            multiple: false,
            required: false,
            dictionaryId: BuiltInDictionary.PROJECT_CODE
          }
        },
        {
          type: "chip-grid",
          key: "contFromRxn",
          props: {
            label: "Cont. TO Rxn.",
            placeholder: "Cont. TO Rxn."
          }
        },
        {
          type: "input",
          key: "reference",
          name: "reference",
          className: "col-span-2",
          props: {
            label: "Literature Reference",
            placeholder: "Literature Reference",
          }
        },
      ]
    }
  ]

  ngOnInit(): void {
    const experimentId = this.experiment()?.id;
    if (experimentId) {
      this.experimentImageService.load(experimentId);
      this.experimentModelService.load(experimentId);
    }
  }

  onModelUpdating(isUpdating: boolean): void {
    this.isUpdating.set(isUpdating);
    if (!isUpdating) this.experimentImageService.refresh();
  }

  showAddMaterialDialog() {
    const [experiment, model] = [this.experiment(), this.model()];
    if (experiment && model) {
      this.isDrawerOpen.set(true);
    }
  }

  closeMaterialDrawer(): void {
    this.isDrawerOpen.set(false);
    this.isDrawerClosedOnce.set(true);
  }
}
