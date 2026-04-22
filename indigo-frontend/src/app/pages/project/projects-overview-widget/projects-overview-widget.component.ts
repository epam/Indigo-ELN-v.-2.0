import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { AsyncPipe, NgIf, NgTemplateOutlet } from '@angular/common';
import { Component, inject, Signal } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProjectsOverviewWidgetService } from './services/projects-overview-widget.service';
import { ApiService } from '@core/services/api.service';
import { toSignal } from '@angular/core/rxjs-interop';
import { CardComponent } from '@core/components/common/card/card.component';
import { TextOverflowTooltipDirective } from '@core/directives/text-overflow-tooltip.directive';
import { BreadcrumbsComponent } from '@/core/components/breadcrumbs/breadcrumbs.component';

interface ExperimentStatus {
  OPEN: number;
  REOPEN: number;
  COMPLETED: number;
  SUBMITTED: number;
  REJECTED: number;
  WAITING: number;
  SIGNING: number;
  SIGNED: number;
  ARCHIVED: number;
  CANCELLED: number;
}

interface TotalCounts {
  projects: number;
  notebooks: number;
  experiments: number;
  experimentsByStatus: ExperimentStatus;
}

@Component({
  standalone: true,
  imports: [
    CardComponent,
    ClassPickerPipe,
    AsyncPipe,
    NgTemplateOutlet,
    NgIf,
    MatTooltipModule,
    TextOverflowTooltipDirective,
    BreadcrumbsComponent
  ],
  selector: 'eln-projects-overview-widget',
  templateUrl: './projects-overview-widget.component.html',
  styleUrls: ['./project-overview-widget.component.scss'],
})
export class ProjectsOverviewWidgetComponent {
  public projectsOverviewWidgetService = inject(ProjectsOverviewWidgetService);

  apiService = inject(ApiService);

  totalCounts: Signal<TotalCounts> = toSignal(this.apiService.request('get', 'total-counts'));
}
