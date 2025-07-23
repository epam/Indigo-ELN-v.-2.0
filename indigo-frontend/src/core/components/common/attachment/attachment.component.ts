import { Attachment } from '@/core/types/entities/attachment.i';
import { DatePipe } from '@angular/common';
import { Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { CardComponent } from '../card/card.component';
import { ApiService } from '@/core/services/api.service';
import { catchError, of, Subject, takeUntil } from 'rxjs';

@Component({
  standalone: true,
  imports: [CardComponent, MatMenuModule, MatIconModule, DatePipe],
  selector: 'eln-attachment',
  templateUrl: './attachment.component.html',
})
export class AttachmentComponent implements OnDestroy {
  @Input() icon = '';
  @Input() attachment: Attachment = {
    id: '',
    name: '',
    size: 0,
  };
  @Input() projectId = '';
  @Output() attachmentDeleted = new EventEmitter<string>();

  private destroy$ = new Subject<void>();

  constructor(protected service: ApiService<Attachment>) { }


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

  downloadAttachment() {
    this.service.request<Blob>(
      'get',
      `project/${this.projectId}/attachments/${this.attachment.id}`,
      undefined,
      {
        responseType: 'blob',
      }
    )
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          console.error('Failed to download attachment:', err);
          return of(null);
        })
      )
      .subscribe({
        next: (blob: Blob | null) => {
          if (blob) {
            // Create a link and trigger download
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = this.attachment.name || 'download';
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            a.remove();
          }
        },
      });
  }

  deleteAttachment() {
    this.service.request<void>('delete', `projects/${this.projectId}/attachments/${this.attachment.id}`)
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          console.error('Failed to delete attachment:', err);
          return of(null);
        })
      )
      .subscribe({ next: () => this.attachmentDeleted.emit(this.attachment.id) });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
