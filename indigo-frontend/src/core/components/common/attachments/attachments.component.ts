import { Component, computed, DestroyRef, inject, input, output } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AttachmentComponent } from '@core/components/common/attachment/attachment.component';
import { FileUploadComponent } from '@core/components/common/file-upload/file-upload.component';
import { ApiService } from '@core/services/api.service';
import { PermissionService } from '@core/services/permission/permission.service';
import { Attachment } from '@core/types/entities/attachment.i';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { PermissionedEntity } from '@core/types/entities/permission.i';
import { ApplicationPermission } from '@core/types/entities/user.i';
import { finalize, from } from 'rxjs';
import { concatMap } from 'rxjs/operators';

@Component({
  selector: 'eln-attachments',
  standalone: true,
  templateUrl: './attachments.component.html',
  imports: [AttachmentComponent, FileUploadComponent],
})
export class AttachmentsComponent {
  attachments = input.required<Attachment[]>();
  baseURL = input.required<string>();
  requiredPermission = input<ApplicationPermission | null>(null);
  entity = input<PermissionedEntity | null>(null);

  attachmentsChanged = output<Attachment[]>();

  service = inject(ApiService);
  destroyRef = inject(DestroyRef);
  private permissionService = inject(PermissionService);

  isUploadingAttachment = false;

  // Defaults to true when no permission is configured, so consumers that don't opt in
  // (e.g. notebooks/experiments, until their own permission tickets are implemented) keep
  // their current behavior.
  canEdit = computed(() => {
    const permission = this.requiredPermission();
    return permission == null || this.permissionService.hasPermission(permission, this.entity());
  });

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
