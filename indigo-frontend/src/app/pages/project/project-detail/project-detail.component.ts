import { ProjectTabButtonComponent } from '@/core/components/project/project-tab-button/project-tab-button.component';
import { ProjectOverviewWidgetDirective } from '@/core/components/project/projects-overview-widget/directives/project-overview-widget.directive';
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterOutlet } from '@angular/router';

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
