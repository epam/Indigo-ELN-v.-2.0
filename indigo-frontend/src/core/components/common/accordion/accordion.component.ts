import { CdkAccordionModule } from '@angular/cdk/accordion';
import { Component, ContentChild, TemplateRef } from '@angular/core';

@Component({
  selector: 'eln-accordion',
  imports: [CdkAccordionModule],
  templateUrl: './accordion.component.html',
})
export class AccordionComponent {
  @ContentChild('header') header: TemplateRef<unknown> | null = null;
  @ContentChild('body') body: TemplateRef<unknown> | null = null;
}
