import { Attachment } from '@/core/types/attachment.i';
import { DatePipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { CardComponent } from '../card/card.component';

@Component({
  standalone: true,
  imports: [CardComponent, DatePipe, MatMenuModule, MatIconModule],
  selector: 'app-attachment',
  templateUrl: './attachment.component.html',
})
export class AttachmentComponent {
  @Input() icon = '';
  @Input() attachment: Attachment = {
    name: '',
    url: '',
    user: '',
  };

  /// TODO: Remove this
  get date() {
    return new Date();
  }

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
