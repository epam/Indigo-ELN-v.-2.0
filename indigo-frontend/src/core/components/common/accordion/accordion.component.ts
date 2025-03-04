import { Component } from '@angular/core';
import {CdkAccordionModule} from '@angular/cdk/accordion';

@Component({
  selector: 'app-accordion',
  imports: [CdkAccordionModule],
  templateUrl: './accordion.component.html',
  styleUrl: './accordion.component.scss'
})
export class AccordionComponent {
  items = [{title: 'Item 1', description: 'Description'}, {title: 'Item 2', description: 'Description'}, {title: 'Item 3',description: 'Description'}, {title: 'Item 4',description: 'Description'}, {title: 'Item 5', description: 'Description'}];
  expandedIndex = 0;
}
