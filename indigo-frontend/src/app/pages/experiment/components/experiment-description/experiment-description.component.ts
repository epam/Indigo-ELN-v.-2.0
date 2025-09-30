import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';
import { ExperimentService } from '@core/services/experiment/experiment.service';
import { FormlyFieldConfig, FormlyModule } from '@ngx-formly/core';
import { FormGroup } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'eln-experiment-description',
  templateUrl: './experiment-description.component.html',
  imports: [FormlyModule],
})
export class ExperimentDescriptionComponent implements OnInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  destroyRef = inject(DestroyRef);

  experiment: ExperimentDetail | null;

  fields: FormlyFieldConfig[] = [];

  form = new FormGroup({});

  ngOnInit() {
    this.experimentService.experiment$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((experiment) => {
        this.experiment = experiment;
        this.fields = [
          {
            type: 'editor',
            key: 'description',
            defaultValue: experiment.description,
            props: {
              label: 'Description',
              placeholder: 'Description',
            },
          },
        ];
      });
  }
}
