import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ExperimentService } from '@core/services/experiment/experiment.service';
import { FormlyModule } from '@ngx-formly/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'eln-reaction-scheme',
  templateUrl: './reaction-scheme.component.html',
  imports: [FormlyModule],
})
export class ReactionSchemeComponent implements OnInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  destroyRef = inject(DestroyRef);

  picture: string | null = null;
  loading = false;
  error = false;

  ngOnInit() {
    this.experimentService.picture$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((x) => {
        this.loading = x.state === 'loading';
        this.error = x.state === 'error';
        this.picture =
          x.state === 'ready' ? URL.createObjectURL(x.value) : null;
      });
    this.experimentService.experiment$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((experiment) => {
        this.experimentService.loadPicture(experiment.id);
      });
  }
}
