import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { AsyncPipe, NgIf, NgTemplateOutlet } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { Router } from '@angular/router';
import { CardComponent } from '../../common/card/card.component';
import { ProjectsOverviewWidgetService } from './services/projects-overview-widget.service';

const regex = /^\/projects\/[a-zA-Z0-9]+$/;

interface ProjectsOverviewWidgetData {
  openExperiments: number;
  waitingSignature: number;
  completed: number;
  rejected: number;
  projects: number;
  notebooks: number;
  experiments: number;
}

@Component({
  standalone: true,
  imports: [CardComponent, ClassPickerPipe, AsyncPipe, NgTemplateOutlet, NgIf],
  selector: 'eln-projects-overview-widget',
  templateUrl: './projects-overview-widget.component.html',
})
export class ProjectsOverviewWidgetComponent {
  public projectsOverviewWidgetService = inject(ProjectsOverviewWidgetService);
  @Input() data: ProjectsOverviewWidgetData = {
    openExperiments: 0,
    waitingSignature: 0,
    completed: 0,
    rejected: 0,
    projects: 0,
    notebooks: 0,
    experiments: 0,
  };

  constructor(protected router: Router) {}

  get shouldRenderProjectLinks() {
    return regex.test(this.router.url);
  }
}
