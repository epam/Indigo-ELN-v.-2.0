import { Pipe, PipeTransform } from '@angular/core';
import { ClassValue } from 'clsx';
import { twsx } from '../utils/twsx';

@Pipe({
  name: 'twsx',
  standalone: true,
})
export class TwsxPipe implements PipeTransform {
  transform(...classvalues: ClassValue[]): string {
    return twsx(...classvalues);
  }
}
