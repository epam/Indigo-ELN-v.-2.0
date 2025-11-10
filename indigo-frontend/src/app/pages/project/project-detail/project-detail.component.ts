import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterOutlet } from '@angular/router';
import {
  ProjectOverviewWidgetDirective
} from '@pages/project/projects-overview-widget/directives/project-overview-widget.directive';
import { ProjectTabButtonComponent } from '@pages/project/project-tab-button/project-tab-button.component';

@Component({
  selector: 'eln-project-detail',
  templateUrl: './project-detail.component.html',
  imports: [
    RouterOutlet,
    ProjectOverviewWidgetDirective,
    ProjectTabButtonComponent,
  ],
})
export class ProjectDetailComponent implements OnInit {
  activedRoute = inject(ActivatedRoute);

  public projectUrl = '';
  public notebooksUrl = '';

  ngOnInit() {
    this.activedRoute.params.subscribe((params) => {
      this.projectUrl = `/projects/${params['id']}`;
      this.notebooksUrl = `${this.projectUrl}/notebooks`;
    });
  }
}
