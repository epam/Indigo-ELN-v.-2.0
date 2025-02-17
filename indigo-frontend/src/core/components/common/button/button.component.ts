import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { ButtonVariantPipe } from './button-variant.pipe';
import { ButtonVariants } from './button.variant';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [CommonModule, ButtonVariantPipe],
  templateUrl: './button.component.html',
})
export class ButtonComponent {
  @Input() variant: ButtonVariants['variant'] = 'green';
  @Input() disabled = false;
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() classList = '';
}
