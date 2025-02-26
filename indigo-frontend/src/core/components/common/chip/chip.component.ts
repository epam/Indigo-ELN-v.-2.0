import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ChipVariantPipe } from './chip-variant.pipe';
import { ChipVariantProps } from './chip.variant';

@Component({
  standalone: true,
  imports: [CommonModule, ChipVariantPipe],
  selector: 'app-chip',
  templateUrl: './chip.component.html',
})
export class ChipComponent {
  @Input() deleteable = false;
  @Output() emitClose = new EventEmitter<void>();
  @Input() active: ChipVariantProps['active'] = true;
  @Input() error: ChipVariantProps['error'] = false;
  @Input() size: ChipVariantProps['size'] = 'default';
  @Input() classList = '';
}
