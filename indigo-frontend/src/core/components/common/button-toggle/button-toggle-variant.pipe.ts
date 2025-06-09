import { twsx } from '@/core/utils/twsx';
import { Pipe, PipeTransform } from '@angular/core';
import {
  buttonToggleButtonVariants,
  buttonToggleVariants,
} from './button-toggle.variant';

@Pipe({
  name: 'buttonToggleVariant',
  standalone: true,
})
export class ButtonToggleVariantPipe implements PipeTransform {
  transform(
    type: 'group' | 'button',
    variant: 'default' | 'alpha',
    classList?: string,
  ): string {
    return twsx(
      type === 'group'
        ? buttonToggleVariants({ variant })
        : buttonToggleButtonVariants({ variant }),
      classList,
    );
  }
}
