import {
  RouteAnimationType,
  fadeAnimation,
  slideDownAnimation,
  slideLeftAnimation,
  slideRightAnimation,
  slideUpAnimation,
} from '@/core/animations/route-animations';
import { CommonModule } from '@angular/common';
import { Component, ContentChild, Input } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-animated-route-container',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './animated-route-container.component.html',
  animations: [
    fadeAnimation,
    slideLeftAnimation,
    slideRightAnimation,
    slideUpAnimation,
    slideDownAnimation,
  ],
})
export class AnimatedRouteContainerComponent {
  @Input() animationType: RouteAnimationType = RouteAnimationType.Fade;
  @ContentChild(RouterOutlet) outlet: RouterOutlet | undefined;

  prepareRouteTransition(outlet: RouterOutlet | undefined) {
    if (!outlet) return null;
    return outlet.isActivated ? outlet.activatedRoute : '';
  }
}
