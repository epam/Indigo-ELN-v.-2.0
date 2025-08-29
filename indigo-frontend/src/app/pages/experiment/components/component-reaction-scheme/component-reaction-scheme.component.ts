import { Component, inject, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Experiment } from '@core/types/entities/experiment.i';
import { ExperimentService } from '@core/services/experiment.service';

@Component({
  selector: 'eln-component-reaction-scheme',
  templateUrl: './component-reaction-scheme.component.html',
  imports: [],
})
export class ComponentReactionSchemeComponent implements OnInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  @Input() experiment: Experiment;

  picture: string | null = null;
  loading = false;
  error = false;

  ngOnInit() {
    this.experimentService.picture$.subscribe((x) => {
      this.loading = x.state === 'loading';
      this.error = x.state === 'error';
      this.picture = x.state === 'ready' ? URL.createObjectURL(x.value) : null;
    });
    this.experimentService.loadPicture(this.experiment.id);
  }
}
