import { Attachment } from '@/core/types/entities/attachment.i';
import { DatePipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { CardComponent } from '../card/card.component';

@Component({
  standalone: true,
  imports: [CardComponent, MatMenuModule, MatIconModule, DatePipe],
  selector: 'eln-attachment',
  templateUrl: './attachment.component.html',
})
export class AttachmentComponent {
  @Input() icon = '';
  @Input() attachment: Attachment = {
    id: '',
    name: '',
    size: 0,
  };

  get computedIcon() {
    if (this.icon.length) {
      return this.icon;
    }

    const extension = this.attachment.name.split('.').pop();
    switch (extension) {
      case 'pdf':
      case 'xlsx':
      case 'xls':
      case 'doc':
      case 'docx':
        return 'indicon-file-text';
      case 'png':
        return 'indicon-image';
      default:
        return 'indicon-file';
    }
  }
}
