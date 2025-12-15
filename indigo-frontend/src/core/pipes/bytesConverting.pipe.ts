import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'bytesConverting'
})
export class BytesConvertingPipe implements PipeTransform {

  transform(value: number): string {
    let result: number;
    let unit: string;

    switch (true) {
      case value >= 1024 ** 3:
        result = value / 1024 ** 3;
        unit = 'GB';
        break;
      case value >= 1024 ** 2:
        result = value / 1024 ** 2;
        unit = 'MB';
        break;
      case value >= 1024:
        result = value / 1024;
        unit = 'KB';
        break;
      default:
        result = value;
        unit = 'B';
    }

    return `${Math.round(result)}${unit}`;
  }
}
