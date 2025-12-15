import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'convertBytesToKilobytes'
})
export class ConvertBytesToKilobytesPipe implements PipeTransform {

  transform(value: number): number {
    return Math.round(value / 1024);
  }

}
