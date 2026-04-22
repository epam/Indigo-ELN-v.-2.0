import { Component } from '@angular/core';
import { TemplateListComponent } from '@pages/template/template-list/template-list.component';
import { RouteAnimationType } from '@core/animations/route-animations';

@Component({
  selector: 'eln-template-layout',
  templateUrl: './template-layout.component.html',
  standalone: true,
  imports: [TemplateListComponent],
})
export class TemplateLayoutComponent {
  animationType = RouteAnimationType.SlideRight;
}
