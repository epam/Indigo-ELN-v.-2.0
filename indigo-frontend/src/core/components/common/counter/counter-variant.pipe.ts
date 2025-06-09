import { twsx } from '@/core/utils/twsx';
import { Pipe, PipeTransform } from '@angular/core';
import { CounterVariants, counterVariants } from './counter.variant';

@Pipe({
  name: 'counterVariant',
  standalone: true,
})
export class CounterVariantPipe implements PipeTransform {
  transform(
    variant: CounterVariants['variant'],
    size: CounterVariants['size'],
    classList?: string,
  ): string {
    return twsx(counterVariants({ variant, size }), classList);
  }
}
