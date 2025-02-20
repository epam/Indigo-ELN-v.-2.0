import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { BadgeVariantPipe } from './badge-variant.pipe';
import { BadgeVariantProps } from './badge.variant';

@Component({
  standalone: true,
  imports: [CommonModule, BadgeVariantPipe],
  selector: 'app-badge',
  templateUrl: './badge.component.html',
})
export class BadgeComponent {
  @Input() variant: BadgeVariantProps['variant'] = 'blue';
  @Input() size: BadgeVariantProps['size'] = 'default';
  @Input() icon?: string;
  @Input() classList = '';
}
