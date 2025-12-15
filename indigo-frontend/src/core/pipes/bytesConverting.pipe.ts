import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'bytesConverting'
})
export class BytesConvertingPipe implements PipeTransform {

  transform(value: number): string {
    const sizeConfig = { 
      GB: 1024 ** 3,
      MB: 1024 ** 2,
      KB: 1024,
      B: 1
    };
    for (const [unit, threshold] of Object.entries(sizeConfig)) {
      if (value >= threshold) {
        return `${(value / threshold).toFixed(1)}${unit}`;
      }
    }
    return `${value}B`; 
  }
}
