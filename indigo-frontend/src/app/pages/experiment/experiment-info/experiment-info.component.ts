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

  BuildInDictionary = BuiltInDictionary;

  form = new FormGroup({});
  fields: FormlyFieldConfig[] = [
    {
      type: "input",
      key: "title",
      name: "title",
      wrappers: ['raw'],
      props: {
        label: "Experiment Title",
        placeholder: "Experiment Title",
      }
    },
    {
      type: "select",
      key: "therapeutic",
      wrappers: ['raw'],
      props: {
        label: "Therapeutic Area",
        dictionaryId: "THERAUPETIC_AREA",
        multiple: false,
        required: false,
        dictioinaryId: this.BuildInDictionary.THERAPEUTIC_AREA
      }
    }, 
    {
      type: "select",
      key: "code-and-name",
      wrappers: ['raw'],
      props: {
        label: "Project Code & Name",
        placeholder: "PROJECT_CODE",
        multiple: false,
        required: false,
      }
    },
    {
      type: "input",
      key: "literature",
      wrappers: ['raw'],
      props: {
        label: "Literature Reference",
        placeholder: "Literature Reference",
      }
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
