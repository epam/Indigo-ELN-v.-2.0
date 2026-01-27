import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'eln-chip-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (items().length) {
      <div class="flex items-center gap-1.5 flex-wrap">
        @for (item of items(); track $index) {
          <span class="px-2 py-1 rounded-full text-xs whitespace-nowrap chip-uniform">
            {{ item }}
          </span>
        }
      </div>
    } @else {
      <span class="text-gray-400">—</span>
    }
  `
})
export class ChipListComponent {
  items = input.required<string[]>();
}
