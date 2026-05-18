import { Component, input } from '@angular/core';
import { CdkAccordion, CdkAccordionItem } from '@angular/cdk/accordion';
import { CardComponent } from '@core/components/common/card/card.component';

@Component({
  selector: 'eln-experiment-component-wrapper',
  standalone: true,
  templateUrl: './experiment-component-wrapper.component.html',
  imports: [CdkAccordion, CdkAccordionItem, CardComponent],
})
export class ExperimentComponentWrapperComponent {
  title = input.required<string>();
}
