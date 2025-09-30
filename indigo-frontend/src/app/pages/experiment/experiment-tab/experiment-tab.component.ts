import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { combineLatest } from 'rxjs';
import { ExperimentService } from '@core/services/experiment/experiment.service';
import { ExperimentDescriptionComponent } from '@pages/experiment/components/experiment-description/experiment-description.component';
import { ReactionSchemeComponent } from '@pages/experiment/components/reaction-scheme/reaction-scheme.component';
import { ExperimentDetailsComponent } from '@pages/experiment/components/experiment-details/experiment-details.component';
import { StoichiometryTableComponent } from '@pages/experiment/components/stoichiometry-table/stoichiometry-table.component';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TemplateComponent } from '@core/types/entities/template.i';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'eln-experiment-tab',
  templateUrl: './experiment-tab.component.html',
  imports: [
    ReactionSchemeComponent,
    ExperimentDetailsComponent,
    ExperimentDescriptionComponent,
    StoichiometryTableComponent,
    MatProgressSpinner,
  ],
})
export class ExperimentTabComponent implements OnInit {
  protected readonly ComponentStoichiometryTableComponent =
    StoichiometryTableComponent;

  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  destroyRef = inject(DestroyRef);

  mutating = false;

  components: TemplateComponent[] | null = null;

  loading = false;
  error = false;

  ngOnInit() {
    combineLatest({
      template: this.experimentService.templateLoad$,
      routing: this.activatedRoute.params,
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ template, routing }) => {
        let tabNo = Number.parseInt(routing['tabNo']) - 1;
        this.loading = template.state === 'loading';
        this.error = template.state === 'error';
        if (template.state === 'ready') {
          tabNo = Math.min(tabNo, template.value.templateTabs.length - 1);
          tabNo = Math.max(tabNo, 0);
          this.components = template.value.templateTabs[tabNo].components;
        }
      });
    this.experimentService.mutating$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((x) => {
        this.mutating = x;
      });
  }
}
