import { RouteAnimationType } from '@/core/animations/route-animations';
import { AnimatedRouteContainerComponent } from '@/core/components/common/animated-route-container/animated-route-container.component';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ProjectsOverviewWidgetComponent } from '@core/components/project/projects-overview-widget/projects-overview-widget.component';

@Component({
  selector: 'app-project-layout',
  templateUrl: './project-layout.component.html',
  standalone: true,
  imports: [
    ProjectsOverviewWidgetComponent,
    RouterOutlet,
    AnimatedRouteContainerComponent,
  ],
})
export class ProjectLayoutComponent {
  animationType = RouteAnimationType.SlideRight;
}
