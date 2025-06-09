import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filesize'
})
export class FileSizePipe implements PipeTransform {

  transform(size: number, decimalPlaces: number = 2): string {
    if(size === 0) return '0 Bytes';
    const units = ['Bytes','KB','MB'];
    const i = Math.floor(Math.log(size)/Math.log(1024));
    const formattedSize = (size/Math.pow(1024,i)).toFixed(decimalPlaces);
    return `${formattedSize} ${units[i]}`;
  }
}
