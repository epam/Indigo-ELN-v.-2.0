import { twsx } from '@/core/utils/twsx';
import { Pipe, PipeTransform } from '@angular/core';
import { button, buttonGroup } from './toggle-group.variant';

@Pipe({
  name: 'toggleGroupVariant',
  standalone: true,
})
export class ToggleGroupVariantPipe implements PipeTransform {
  transform(
    type: 'group' | 'button',
    variant: 'default' | 'seamless',
    classList?: string,
  ): string {
    return twsx(
      type === 'group' ? buttonGroup({ variant }) : button({ variant }),
      classList,
    );
  }
}
