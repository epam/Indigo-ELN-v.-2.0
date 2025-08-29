import { RouteAnimationType } from '@/core/animations/route-animations';
import { Component } from '@angular/core';
import {
  AnimatedRouteContainerComponent
} from '@core/components/common/animated-route-container/animated-route-container.component';
import {RouterOutlet} from '@angular/router';

@Component({
  selector: 'eln-experiment-layout',
  templateUrl: './experiment-layout.component.html',
  standalone: true,
  imports: [AnimatedRouteContainerComponent, RouterOutlet],
})
export class ExperimentLayoutComponent {
  animationType = RouteAnimationType.SlideRight;
}
