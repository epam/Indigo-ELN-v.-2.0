import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { combineLatest } from 'rxjs';
import { ExperimentService } from '@core/services/experiment.service';
import { ComponentExperimentDescriptionComponent } from '@pages/experiment/components/component-experiment-description/component-experiment-description.component';
import { ComponentReactionSchemeComponent } from '@pages/experiment/components/component-reaction-scheme/component-reaction-scheme.component';
import { ComponentExperimentDetailsComponent } from '@pages/experiment/components/component-experiment-details/component-experiment-details.component';
import { ComponentStoichiometryTableComponent } from '@pages/experiment/components/component-stoichiometry-table/component-stoichiometry-table.component';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TemplateComponent } from '@core/types/entities/template.i';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'eln-experiment-tab',
  templateUrl: './experiment-tab.component.html',
  imports: [
    ComponentReactionSchemeComponent,
    ComponentExperimentDetailsComponent,
    ComponentExperimentDescriptionComponent,
    ComponentStoichiometryTableComponent,
    MatProgressSpinner,
  ],
  styles: `
    .mutating-spinnner {
      position: fixed;
      top: 120px;
      right: 60px;
      z-index: 1000;
    }
  `,
})
export class ExperimentTabComponent implements OnInit {
  protected readonly ComponentStoichiometryTableComponent =
    ComponentStoichiometryTableComponent;

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
