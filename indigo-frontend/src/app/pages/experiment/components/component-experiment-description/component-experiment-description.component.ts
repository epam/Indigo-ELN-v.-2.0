import { Component, inject, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Experiment } from '@core/types/entities/experiment.i';
import { ExperimentService } from '@core/services/experiment.service';
import { FormlyFieldConfig, FormlyModule } from '@ngx-formly/core';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'eln-component-experiment-description',
  templateUrl: './component-experiment-description.component.html',
  imports: [FormlyModule],
})
export class ComponentExperimentDescriptionComponent implements OnInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  @Input() experiment: Experiment;

  fields: FormlyFieldConfig[] = [];
  form = new FormGroup({});

  ngOnInit() {
    this.fields = [
      {
        type: 'editor',
        key: 'description',
        defaultValue: this.experiment.description,
        props: {
          label: 'Description',
          placeholder: 'Description',
        },
      },
    ];
  }
}
