import { Pipe, PipeTransform } from '@angular/core';
import { twsx } from '../utils/twsx';

@Pipe({
  name: 'classPicker',
  standalone: true,
})
export class ClassPickerPipe implements PipeTransform {
  transform(value: Record<string, boolean>): string {
    if (!value || typeof value !== 'object') {
      return '';
    }

    return twsx(
      Object.entries(value)
        .filter(([, isActive]) => isActive)
        .map(([className]) => className)
        .join(' '),
    );
  }
}
