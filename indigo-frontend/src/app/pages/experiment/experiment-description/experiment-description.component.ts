import { Component, DestroyRef, effect, inject, input } from '@angular/core';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { CdkAccordionModule } from '@angular/cdk/accordion';
import { FormlyFieldConfig, FormlyModule } from '@ngx-formly/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, switchMap } from 'rxjs';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';

@Component({
  selector: 'eln-experiment-description',
  standalone: true,
  imports: [CdkAccordionModule, FormlyModule, ReactiveFormsModule],
  templateUrl: './experiment-description.component.html',
})
export class ExperimentDescriptionComponent {
  experimentId = input.required<UUID>();

  private experimentDetailService = inject(ExperimentDetailService);
  private destroyRef = inject(DestroyRef);

  model = this.toFormModel(this.experimentDetailService.experimentDetail());
  form = new FormGroup({}, { updateOn: 'blur' });
  fields: FormlyFieldConfig[] = [
    {
      type: 'editor',
      key: 'description',
      props: { label: 'Description', placeholder: 'Description' },
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
          const description = (value as Record<string, unknown>)['description'] as string | null;
          console.log(description);
          if (description === (exp.description ?? null)) return EMPTY;
          return this.experimentDetailService.editExperiment({ description });
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
  }

  private toFormModel(exp: ExperimentDetail | null): Record<string, unknown> {
    return { description: exp?.description ?? null };
  }
}
