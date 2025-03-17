import { Component, ContentChild, ElementRef, TemplateRef } from '@angular/core';
import { CdkAccordionModule } from '@angular/cdk/accordion';

@Component({
  selector: 'app-accordion',
  imports: [CdkAccordionModule],
  templateUrl: './accordion.component.html'
})
export class AccordionComponent {
  @ContentChild('header') header: TemplateRef<unknown> | null = null;
  @ContentChild('body') body: TemplateRef<unknown> | null = null;
}
