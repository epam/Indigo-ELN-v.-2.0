import { BytesConvertingPipe } from '@/core/pipes/bytesConverting.pipe';
import { ApiService } from '@/core/services/api.service';
import { Attachment } from '@/core/types/entities/attachment.i';
import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { DownloadService } from '@core/services/download.service';
import { CardComponent } from '../card/card.component';

@Component({
  standalone: true,
  imports: [CardComponent, MatMenuModule, MatIconModule, DatePipe, BytesConvertingPipe],
  selector: 'eln-attachment',
  templateUrl: './attachment.component.html',
})
export class AttachmentComponent {
  attachment = input.required<Attachment>();
  baseURL = input.required<string>();
  canDelete = input<boolean>(true);
  canDownload = input<boolean>(true);

  attachmentDeleted = output<string>();

  service = inject(ApiService);
  downloadService = inject(DownloadService);
  destroyRef = inject(DestroyRef);

  get computedIcon() {
    const extension = this.attachment().name.split('.').pop();
    switch (extension) {
      case 'pdf':
      case 'doc':
      case 'docx':
        return 'indicon-file-text';
      case 'png':
      case 'jpg':
      case 'jpeg':
        return 'indicon-image';
      case 'xls':
      case 'xlsx':
        return 'indicon-table';
      default:
        return 'indicon-file';
    }
  }

  downloadAttachment() {
    this.downloadService
      .download('get', `${this.baseURL()}/attachments/${this.attachment().id}`, 'attachment')
      .subscribe();
  }

  deleteAttachment() {
    this.service
      .request<void>('delete', `${this.baseURL()}/attachments/${this.attachment().id}`)
      .subscribe(() => this.attachmentDeleted.emit(this.attachment().id));
  }
}
