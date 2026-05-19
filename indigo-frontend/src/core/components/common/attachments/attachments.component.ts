import { Component, DestroyRef, inject, input, output } from '@angular/core';
import { AttachmentComponent } from '@core/components/common/attachment/attachment.component';
import { FileUploadComponent } from '@core/components/common/file-upload/file-upload.component';
import { finalize, from } from 'rxjs';
import { Attachment } from '@core/types/entities/attachment.i';
import { concatMap } from 'rxjs/operators';
import { ApiService } from '@core/services/api.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';

@Component({
  selector: 'eln-attachments',
  standalone: true,
  templateUrl: './attachments.component.html',
  imports: [AttachmentComponent, FileUploadComponent],
})
export class AttachmentsComponent {
  attachments = input.required<Attachment[]>();
  baseURL = input.required<string>();

  attachmentsChanged = output<Attachment[]>();

  service = inject(ApiService);
  destroyRef = inject(DestroyRef);

  isUploadingAttachment = false;

  onUpload(newFiles: File[]): void {
    if (!newFiles.length) {
      return;
    }
    this.isUploadingAttachment = true;

    const formDatas = newFiles.map((file) => {
      const formData = new FormData();
      formData.append('file', file, file.name);
      return formData;
    });

    from(formDatas)
      .pipe(
        concatMap((formData) => this.service.request<Attachment[]>('post', `${this.baseURL()}/attachments`, formData)),
        finalize(() => (this.isUploadingAttachment = false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((attachments) => {
        this.attachmentsChanged.emit(attachments);
      });
  }

  onAttachmentDeleted(id: UUID) {
    const attachments1 = this.attachments().filter((x) => x.id !== id);
    this.attachmentsChanged.emit(attachments1);
  }
}
