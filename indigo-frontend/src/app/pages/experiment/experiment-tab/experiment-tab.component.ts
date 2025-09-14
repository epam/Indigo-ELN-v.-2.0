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
import { ExperimentService } from '@core/services/experiment.service';
import { ComponentExperimentDescriptionComponent } from '@pages/experiment/components/component-experiment-description/component-experiment-description.component';
import { ComponentReactionSchemeComponent } from '@pages/experiment/components/component-reaction-scheme/component-reaction-scheme.component';
import { ComponentExperimentDetailsComponent } from '@pages/experiment/components/component-experiment-details/component-experiment-details.component';
import { ComponentStoichiometryTableComponent } from '@pages/experiment/components/component-stoichiometry-table/component-stoichiometry-table.component';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'eln-experiment-tab',
  templateUrl: './experiment-tab.component.html',
  imports: [MatProgressSpinner],
  styles: `
    .mutating-spinnner {
      position: fixed;
      top: 120px;
      right: 60px;
      z-index: 1000;
    }
  `,
})
export class ExperimentTabComponent implements AfterViewInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  tabNo: number | null = null;
  mutating = false;

  @ViewChild('children', { read: ViewContainerRef, static: true })
  childrenContainer!: ViewContainerRef;

  private destroy$ = new Subject<void>();

  private componentTypes = {
    reactionScheme: ComponentReactionSchemeComponent,
    experimentDescription: ComponentExperimentDescriptionComponent,
    experimentDetails: ComponentExperimentDetailsComponent,
    stoichiometryTable: ComponentStoichiometryTableComponent,
  };

  loading = false;
  error = false;

  ngAfterViewInit() {
    this.experimentService.templateLoad$
      .pipe(takeUntil(this.destroy$))
      .subscribe((x) => {
        this.loading = x.state === 'loading';
        this.error = x.state === 'error';
        this.childrenContainer.clear();
        if (x.state === 'ready') {
          const template = x.value;
          for (const component of template.components) {
            const componentType = this.componentTypes[component.type];
            if (componentType) {
              const ref = this.childrenContainer.createComponent(componentType);
              if (component.type === 'stoichiometryTable') {
                (
                  ref.instance as ComponentStoichiometryTableComponent
                ).showReactantsReagentsSolvents =
                  component.reactantsReagentsSolvents;
                (
                  ref.instance as ComponentStoichiometryTableComponent
                ).showReactionProducts = component.reactionProducts;
              }
            } else {
              console.error('Unknown template component type', component);
            }
          }
        }
      });
    this.activatedRoute.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ tabNo }) => {
        this.tabNo = tabNo;
      });
    this.experimentService.mutating$
      //   .pipe(takeUntil(this.destroy$))
      .subscribe((x) => (this.mutating = x));
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
