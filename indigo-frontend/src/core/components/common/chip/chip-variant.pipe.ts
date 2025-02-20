import { twsx } from '@/core/utils/twsx';
import { Pipe, PipeTransform } from '@angular/core';
import { ChipVariantProps, chipVariants } from './chip.variant';

@Pipe({
  name: 'chipVariant',
  standalone: true,
})
export class ChipVariantPipe implements PipeTransform {
  transform(
    active: ChipVariantProps['active'],
    error: ChipVariantProps['error'],
    size: ChipVariantProps['size'],
    classList?: string,
  ): string {
    return twsx(chipVariants({ active, error, size }), classList);
  }
}
