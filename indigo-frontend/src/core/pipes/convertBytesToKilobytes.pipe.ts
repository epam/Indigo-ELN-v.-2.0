import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'bytesConverting'
})
export class BytesConvertingPipe implements PipeTransform {

  transform(value: number): number {
    return Math.round(value / 1024);
  }

}
