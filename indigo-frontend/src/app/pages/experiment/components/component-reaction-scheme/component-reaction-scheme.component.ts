import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ExperimentService } from '@core/services/experiment.service';
import { FormlyModule } from '@ngx-formly/core';

@Component({
  selector: 'eln-component-reaction-scheme',
  templateUrl: './component-reaction-scheme.component.html',
  imports: [FormlyModule],
})
export class ComponentReactionSchemeComponent implements OnInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  picture: string | null = null;
  loading = false;
  error = false;

  ngOnInit() {
    this.experimentService.picture$.subscribe((x) => {
      this.loading = x.state === 'loading';
      this.error = x.state === 'error';
      this.picture = x.state === 'ready' ? URL.createObjectURL(x.value) : null;
    });
    this.experimentService.experiment$.subscribe((experiment) => {
      this.experimentService.loadPicture(experiment.id);
    });
  }
}
