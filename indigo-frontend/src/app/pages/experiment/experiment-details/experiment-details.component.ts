import { Component, computed, DestroyRef, effect, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '@core/components/common/card/card.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { CdkAccordionModule } from '@angular/cdk/accordion';
import { FormlyFieldConfig, FormlyModule } from '@ngx-formly/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BuiltInDictionary, DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, switchMap } from 'rxjs';
import { ExperimentDetail, ExperimentEditRequest } from '@core/types/entities/experiments/experiment-detail.i';
import { ExperimentRef } from '@core/types/entities/experiments/experiment-shared.i';

@Component({
  selector: 'eln-experiment-details',
  standalone: true,
  imports: [CommonModule, CardComponent, CdkAccordionModule, FormlyModule, ReactiveFormsModule],
  templateUrl: './experiment-details.component.html',
  styleUrl: './experiment-details.component.scss',
})
export class ExperimentDetailsComponent {
  experimentId = input.required<string>();

  private experimentDetailService = inject(ExperimentDetailService);
  private destroyRef = inject(DestroyRef);

  experiment = computed(() => this.experimentDetailService.experimentDetail());

  model = this.toFormModel(this.experiment());

  form = new FormGroup({}, { updateOn: 'blur' });
  fields: FormlyFieldConfig[] = [
    {
      fieldGroupClassName: 'grid grid-cols-[1fr_1fr] gap-[16px]',
      fieldGroup: [
        {
          type: 'input',
          key: 'title',
          name: 'title',
          props: { label: 'Experiment Title', placeholder: 'Experiment Title' },
        },
        {
          type: 'experiment-select',
          key: 'linkedExperiment',
          props: { label: 'Linked Experiment' },
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
          type: 'experiment-select',
          key: 'contToRxn',
          props: { label: 'Cont. TO Rxn' },
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
          type: 'experiment-select',
          key: 'contFromRxn',
          props: { label: 'Cont. FROM Rxn' },
        },
        {
          type: 'input',
          key: 'reference',
          name: 'reference',
          className: 'col-span-2',
          props: { label: 'Literature Reference', placeholder: 'Literature Reference' },
        },
      ],
    },
  ];

  constructor() {
    effect(() => {
      const exp = this.experimentDetailService.experimentDetail();
      if (exp) this.form.patchValue(this.toFormModel(exp), { emitEvent: false });
    });

    this.form.valueChanges
      .pipe(
        switchMap((value) => {
          const exp = this.experimentDetailService.experimentDetail();
          const patch = this.buildPatch(value as Record<string, unknown>, exp);
          if (Object.keys(patch).length === 0) {
            return EMPTY;
          }
          return this.experimentDetailService.editExperiment(patch);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
  }

  private toFormModel(exp: ExperimentDetail): Record<string, unknown> {
    return {
      title: exp.title ?? null,
      therapeuticArea: exp.therapeuticArea ?? null,
      projectCode: exp.projectCode ?? null,
      reference: exp.literature ?? null,
      linkedExperiment: exp.linkedExperiments ?? [],
    };
  }

  private buildPatch(value: Record<string, unknown>, exp: ExperimentDetail): ExperimentEditRequest {
    const patch: ExperimentEditRequest = {};
    const title = value['title'] as string | null;
    const therapeuticArea = value['therapeuticArea'] as DictionaryItemRef | null;
    const projectCode = value['projectCode'] as DictionaryItemRef | null;
    const reference = value['reference'] as string | null;
    const linkedExperiment = value['linkedExperiment'] as ExperimentRef[] | null;

    if (title !== (exp.title ?? null)) patch.title = title;
    if (therapeuticArea?.id !== exp.therapeuticArea?.id) patch.therapeuticArea = therapeuticArea;
    if (projectCode?.id !== exp.projectCode?.id) patch.projectCode = projectCode;
    if (reference !== (exp.literature ?? null)) patch.literature = reference;

    const expLinkedIds = (exp.linkedExperiments ?? [])
      .map((e) => e.id)
      .sort()
      .join(',');
    const newLinkedIds = (linkedExperiment ?? [])
      .map((e) => e.id)
      .sort()
      .join(',');
    if (expLinkedIds !== newLinkedIds) patch.linkedExperiments = linkedExperiment ?? [];

    return patch;
  }
}
