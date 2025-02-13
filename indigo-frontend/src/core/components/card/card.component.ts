import { TwsxPipe } from '@/core/pipes/twsx.pipe';
import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  standalone: true,
  imports: [CommonModule, TwsxPipe],
})
export class CardComponent {
  @Input() classList = '';
}
