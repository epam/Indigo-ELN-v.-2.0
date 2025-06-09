import { Pipe, PipeTransform } from '@angular/core';
import { ClassNameValue } from 'tailwind-merge';
import { twsx } from '../utils/twsx';

@Pipe({
  name: 'twsx',
  standalone: true,
})
export class TwsxPipe implements PipeTransform {
  transform(...classvalues: ClassNameValue[]): string {
    return twsx(...classvalues);
  }
}
