import { Component } from '@angular/core';
import {CdkAccordionModule} from '@angular/cdk/accordion';

@Component({
  selector: 'app-accordion',
  imports: [CdkAccordionModule],
  templateUrl: './accordion.component.html'
})
export class AccordionComponent {
  items = [{title: 'Item 1', description: '<table><thead><tr><th>Header 1</th><th>Header 2</th><th>Header 3</th></tr></thead><tbody><tr><td>Data 1</td><td>Data 2</td><td>Data 3</td></tr></tbody></table>'},
    {title: 'Item 2', description: '<div class="text-lg text-gray-600">Description</div>'}
  ];
  expandedIndex = 0;
}
