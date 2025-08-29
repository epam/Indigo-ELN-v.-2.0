import {
  AfterViewInit,
  Component,
  inject,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { Experiment } from '@core/types/entities/experiment.i';
import { Template } from '@core/types/entities/template.i';
import { ExperimentService } from '@core/services/experiment.service';
import { ComponentExperimentDescriptionComponent } from '@pages/experiment/components/component-experiment-description/component-experiment-description.component';
import { ComponentReactionSchemeComponent } from '@pages/experiment/components/component-reaction-scheme/component-reaction-scheme.component';
import {
  ComponentExperimentDetailsComponent
} from '@pages/experiment/components/component-experiment-details/component-experiment-details.component';

@Component({
  selector: 'eln-experiment-tab',
  templateUrl: './experiment-tab.component.html',
  imports: [],
})
export class ExperimentTabComponent implements AfterViewInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  tabNo: number | null = null;

  experiment: Experiment | null = null;
  template: Template | null = null;

  @ViewChild('children', { read: ViewContainerRef, static: true })
  childrenContainer!: ViewContainerRef;

  private destroy$ = new Subject<void>();

  ngAfterViewInit() {
    this.experimentService.data$
      .pipe(takeUntil(this.destroy$))
      .subscribe((x) => {
        this.experiment = this.template = null;
        if (x.state == 'ready') {
          this.experiment = x.value.experiment;
          this.template = x.value.template;
          for (const component of this.template.components) {
            switch (component.type) {
              case 'reactionScheme': {
                const ref = this.childrenContainer.createComponent(ComponentReactionSchemeComponent);
                ref.instance.experiment = this.experiment;
                break;
              }
              case 'experimentDescription': {
                const ref = this.childrenContainer.createComponent(ComponentExperimentDescriptionComponent);
                ref.instance.experiment = this.experiment;
                break;
              }
              case 'experimentDetails': {
                const ref = this.childrenContainer.createComponent(ComponentExperimentDetailsComponent);
                ref.instance.experiment = this.experiment;
                break;
              }
              default:
                console.error('Unknown template component type', component);
                continue;
            }
          }
        }
      });
    this.activatedRoute.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ tabNo }) => {
        this.tabNo = tabNo;
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
