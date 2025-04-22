import { ExperimentAddComponent } from '@/app/pages/experiment/experiment-add/experiment-add.component';
import { ProjectAddComponent } from '@/app/pages/project/project-add/project-add.component';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { Component, Input, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { ButtonComponent } from '../../common/button/button.component';
import { CardComponent } from '../../common/card/card.component';

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
  imports: [CardComponent, ButtonComponent, ProjectAddComponent, ExperimentAddComponent, ClassPickerPipe],
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
  @ViewChild('projectAddModal') projectAddModal!: ProjectAddComponent<any>;
  @ViewChild('experimentAddModal') experimentAddModal!: ExperimentAddComponent<any>;


  constructor(protected router: Router) {}

  get shouldRenderProjectLinks() {
    return regex.test(this.router.url);
  }

  async openModal() {
    return await this.projectAddModal.open();
  }

  async openExperimentModal() {
    return await this.experimentAddModal.open();
  }
}
