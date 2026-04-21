import { IdentityService } from '@/core/services/identity.service';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { take } from 'rxjs';
import { FileSizePipe } from './file-size.pipe';
import { fileTypeConfig } from './file-upload.config';
import { NotificationService } from '@/core/services/notification/notification.service';
import { NotificationType } from '@/core/types/notification.i';

@Component({
  imports: [CommonModule, FileSizePipe],
  standalone: true,
  selector: 'eln-file-upload',
  templateUrl: './file-upload.component.html',
})
export class FileUploadComponent implements OnInit {
  @Input() maxSizeMB = 5; // Default max file size (5MB)
  @Input() allowedTypes = ['doc', 'image', 'pdf', 'xls', 'ppt', 'csv'];
  @Input() uploadingFile = false;
  @Input() withPreview = true;
  @Input() loadingText = 'Uploading...';
  mimeTypes: string[] = [];
  acceptedExtensions = '';
  identityService = inject(IdentityService);
  user;
  @Output() filesSelected = new EventEmitter<File[]>();

  files: File[] = [];
  previews: string[] = [];
  today = Date.now();

  private notificationService = inject(NotificationService);

  ngOnInit(): void {
    this.identityService.user$.pipe(take(1)).subscribe((user) => {
      this.user = user;
    });
    this.allowedTypes.forEach((type) => {
      const config = fileTypeConfig[type];
      if (config) {
        this.mimeTypes.push(...config.mimeTypes);
        this.acceptedExtensions += `${config.extensions},`;
      }
    });
  }

  onFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;

    if (!input.files) return;

    this.handleFiles(input.files);
  }

  handleFiles(fileList: FileList) {
    const files = Array.from(fileList)
      .map((file) => {
        if (this.mimeTypes.length && !this.mimeTypes.includes(file.type)) {
          this.notificationService.notify({
            type: NotificationType.Error,
            message: `Invalid file type: ${file.name}, Please upload files with extensions ${this.allowedTypes.join(', ')}`,
            isInline: false,
          });
        }

        if (file.size > this.maxSizeMB * 1024 * 1024) {
          this.notificationService.notify({
            type: NotificationType.Error,
            message: `File '${file.name}' is too large. Max size is ${file.size}MB.`,
            isInline: false,
          });
        }

        this.previewFile(file);
        this.files.push(file);

        return file;
      })
      .filter(Boolean);

    this.filesSelected.emit(files);
  }

  previewFile(file: File) {
    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = (e) => this.previews.push(e.target?.result as string);
      reader.readAsDataURL(file);
    }
  }

  removeFile(index: number) {
    this.files.splice(index, 1);
    this.previews.splice(index, 1);
  }

  clearFiles() {
    this.files = [];
    this.previews = [];
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }
}
