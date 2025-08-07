import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { AsyncPipe, NgIf, NgTemplateOutlet } from '@angular/common';
import { Component, inject, Signal } from '@angular/core';
import { CardComponent } from '../../common/card/card.component';
import { ProjectsOverviewWidgetService } from './services/projects-overview-widget.service';
import { ApiService } from '@core/services/api.service';
import { toSignal } from '@angular/core/rxjs-interop';

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
  imports: [CardComponent, ClassPickerPipe, AsyncPipe, NgTemplateOutlet, NgIf],
  selector: 'eln-projects-overview-widget',
  templateUrl: './projects-overview-widget.component.html',
})
export class ProjectsOverviewWidgetComponent {
  public projectsOverviewWidgetService = inject(ProjectsOverviewWidgetService);

  apiService = inject(ApiService);

  totalCounts: Signal<TotalCounts> = toSignal(this.apiService.request('get', 'total-counts'));
}
