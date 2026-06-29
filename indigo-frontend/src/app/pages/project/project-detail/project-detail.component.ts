import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ProjectOverviewWidgetDirective } from '@pages/project/projects-overview-widget/directives/project-overview-widget.directive';
import { ProjectTabButtonComponent } from '@pages/project/project-tab-button/project-tab-button.component';

@Component({
  selector: 'eln-project-detail',
  templateUrl: './project-detail.component.html',
  imports: [RouterOutlet, ProjectOverviewWidgetDirective, ProjectTabButtonComponent],
})
export class ProjectDetailComponent implements OnChanges {
  @Input() projectId!: string;

  public projectUrl = '';
  public notebooksUrl = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projectId']) {
      this.projectUrl = `/projects/${this.projectId}`;
      this.notebooksUrl = `${this.projectUrl}/notebooks`;
    }
  }
}
