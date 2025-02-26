import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-copy',
  templateUrl: './copy.component.html',
})
export class CopyComponent {
  @Input() text = '';

  copy() {
    navigator.clipboard.writeText(this.text);
  }
}
