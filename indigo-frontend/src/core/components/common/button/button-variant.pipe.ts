import { twsx } from '@/core/utils/twsx';
import { Pipe, PipeTransform } from '@angular/core';
import { ButtonVariant, ButtonVariants } from './button.variant';
@Pipe({
  name: 'buttonVariant',
  standalone: true,
})
export class ButtonVariantPipe implements PipeTransform {
  transform(variant: ButtonVariants['variant'], classList?: string): string {
    return twsx(ButtonVariant({ variant }), classList);
  }
}
