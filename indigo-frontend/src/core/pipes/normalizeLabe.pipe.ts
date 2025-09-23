import { Pipe, PipeTransform } from '@angular/core';
import { normalizeLabel } from '../utils/string.util';

@Pipe({
    name: 'normalizeLabel',
    standalone: true,
})
export class NormalizeLabelPipe implements PipeTransform {
    transform(key: string): string {
        return normalizeLabel(key);
    }
}