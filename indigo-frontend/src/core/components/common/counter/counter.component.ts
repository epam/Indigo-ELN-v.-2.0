import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { CounterVariantPipe } from './counter-variant.pipe';
import { CounterVariants } from './counter.variant';

@Component({
  standalone: true,
  imports: [CommonModule, CounterVariantPipe],
  selector: 'app-counter',
  templateUrl: './counter.component.html',
})
export class CounterComponent {
  @Input() count = 0;
  @Input() variant: CounterVariants['variant'] = 'blue';
  @Input() size: CounterVariants['size'] = 'default';
  @Input() classList = '';
  @Input() filled = false;
}
