import { Component, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { ExperimentImageService } from '@/core/services/experiment/experiment-image.service';
import { CdkAccordionModule } from '@angular/cdk/accordion';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { ReactionViewComponent } from '@pages/experiment/stoichiometry/reaction-view/reaction-view.component';
import { SampleSearchComponent } from '@pages/experiment/sample-search/sample-search.component';
import { SlideInPanelService } from '@core/components/common/slide-in-panel/slide-in-panel.service';
import { FormlyFieldConfig, FormlyModule } from '@ngx-formly/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BuiltInDictionary } from '@/core/types/entities/dictionary.i';

@Component({
  selector: 'eln-experiment-info',
  styleUrl: './experiment-info.component.scss',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    CdkAccordionModule,
    ReactionViewComponent,
    ButtonComponent,
    FormlyModule,
    ReactiveFormsModule,
  ],
  providers: [ExperimentImageService],
  templateUrl: './experiment-info.component.html',
})
export class ExperimentInfoComponent {
  experimentDetailService = inject(ExperimentDetailService);
  private slideInPanel = inject(SlideInPanelService);

  experiment = computed(() => this.experimentDetailService.experimentDetail());
  experimentId = computed(() => this.experimentDetailService.currentId());
  isLoading = computed(() => this.experimentDetailService.isLoading());
  hasError = computed(() => this.experimentDetailService.hasError());
  model = computed(() => this.experimentDetailService.experimentModel());

  form = new FormGroup({});
  fields: FormlyFieldConfig[] = [
    {
      fieldGroupClassName: 'grid grid-cols-[1fr_1fr] gap-[16px]',
      fieldGroup: [
        {
          type: 'input',
          key: 'title',
          name: 'title',
          props: {
            label: 'Experiment Title',
            placeholder: 'Experiment Title',
          },
        },
        {
          type: 'select-chips',
          key: 'linkedExperiment',
          props: {
            label: 'Linked Experiment',
            placeholder: 'Linked Experiment',
          },
        },
        {
          type: 'select',
          key: 'therapeuticArea',
          props: {
            label: 'Therapeutic Area',
            multiple: false,
            required: false,
            dictionaryId: BuiltInDictionary.THERAPEUTIC_AREA,
          },
        },
        {
          type: 'select-chips',
          key: 'contToRxn',
          props: {
            label: 'Cont. TO Rxn',
            placeholder: 'Cont. TO Rxn',
          },
        },
        {
          type: 'select',
          key: 'projectCode',
          props: {
            label: 'Project Code & Name',
            placeholder: 'PROJECT_CODE',
            multiple: false,
            required: false,
            dictionaryId: BuiltInDictionary.PROJECT_CODE,
          },
        },
        {
          type: 'select-chips',
          key: 'contFromRxn',
          props: {
            label: 'Cont. FROM Rxn',
            placeholder: 'Cont. TO Rxn',
          },
        },
        {
          type: 'input',
          key: 'reference',
          name: 'reference',
          className: 'col-span-2',
          props: {
            label: 'Literature Reference',
            placeholder: 'Literature Reference',
          },
        },
      ],
    },
  ];

  showAddMaterialDialog() {
    const [experiment, model] = [this.experiment(), this.model()];
    if (experiment && model) {
      const ref = this.slideInPanel.open(SampleSearchComponent, {
        inputs: { reactionAnchor: model.reactions[0]?.anchor },
      });
      ref.instance.close.subscribe(() => ref.close());
    }
  }
}
