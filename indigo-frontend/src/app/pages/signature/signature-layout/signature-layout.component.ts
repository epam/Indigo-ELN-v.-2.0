import { RouteAnimationType } from '@/core/animations/route-animations';
import { AnimatedRouteContainerComponent } from '@/core/components/common/animated-route-container/animated-route-container.component';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'eln-project-layout',
  templateUrl: './signature-layout.component.html',
  standalone: true,
  imports: [RouterOutlet, AnimatedRouteContainerComponent],
})
export class SignatureLayoutComponent {
  animationType = RouteAnimationType.SlideRight;
}
