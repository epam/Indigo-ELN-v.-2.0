import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'eln-chip-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './chip-list.component.html',
  styleUrl: './chip-list.component.scss',
})
export class ChipListComponent {
  items = input.required<string[]>();
}
