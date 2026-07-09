import { Component, effect, inject, Input, OnChanges, SimpleChanges } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ProjectOverviewWidgetDirective } from '@pages/project/projects-overview-widget/directives/project-overview-widget.directive';
import { ProjectTabButtonComponent } from '@pages/project/project-tab-button/project-tab-button.component';
import { ProjectService } from '@core/services/project/project.service';
import { BreadcrumbsStateService } from '@core/services/breadcrumbs/breadcrumbs.state.service';

@Component({
  selector: 'eln-project-detail',
  templateUrl: './project-detail.component.html',
  imports: [RouterOutlet, ProjectOverviewWidgetDirective, ProjectTabButtonComponent],
})
export class ProjectDetailComponent implements OnChanges {
  @Input() projectId!: string;

  projectService = inject(ProjectService);
  breadcrumbsState = inject(BreadcrumbsStateService);

  private readonly breadcrumbsEffect = effect(() => {
    const project = this.projectService.project();
    this.breadcrumbsState.setItems([
      { label: 'All Projects', url: '/projects', active: false },
      { label: `Project: ${project?.name ?? ''}`, active: true },
    ]);
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projectId']) {
      this.projectService.load(this.projectId).subscribe();
    }
  }
}
