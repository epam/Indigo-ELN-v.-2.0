import { twsx } from '@/core/utils/twsx';
import { Pipe, PipeTransform } from '@angular/core';
import { BadgeVariantProps, badgeVariants } from './badge.variant';

@Pipe({
  name: 'badgeVariant',
  standalone: true,
})
export class BadgeVariantPipe implements PipeTransform {
  transform(
    variant: BadgeVariantProps['variant'],
    size: BadgeVariantProps['size'],
    classList?: string,
  ): string {
    return twsx(badgeVariants({ variant, size }), classList);
  }
}
