import { Component, Input } from '@angular/core';
import { ButtonComponent } from '../../common/button/button.component';
import { CardComponent } from '../../common/card/card.component';

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
  imports: [CardComponent, ButtonComponent],
  selector: 'app-projects-overview-widget',
  templateUrl: './projects-overview-widget.component.html',
})
export class ProjectsOverviewWidgetComponent {
  @Input() data: ProjectsOverviewWidgetData = {
    openExperiments: 0,
    waitingSignature: 0,
    completed: 0,
    rejected: 0,
    projects: 0,
    notebooks: 0,
    experiments: 0,
  };
}
