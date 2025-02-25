import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ProjectsOverviewWidgetComponent } from '@core/components/project/projects-overview-widget/projects-overview-widget.component';
@Component({
  selector: 'app-project-layout',
  templateUrl: './project-layout.component.html',
  standalone: true,
  imports: [ProjectsOverviewWidgetComponent, RouterOutlet],
})
export class ProjectLayoutComponent {}
