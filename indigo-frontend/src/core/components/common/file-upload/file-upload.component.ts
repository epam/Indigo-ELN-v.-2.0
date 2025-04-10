import { CommonModule } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { FileSizePipe } from './file-size.pipe';
import { UserConfig } from './user.i';

@Component({
  imports: [CommonModule, FileSizePipe],
  standalone: true,
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html'
})
export class FileUploadComponent implements OnInit {
  @Input() maxSizeMB = 5; // Default max file size (5MB)
  @Input() allowedTypes = ['doc', 'image', 'pdf', 'xls', 'ppt', 'csv'];
  mimeTypes: string[] = [];
  acceptedExtensions: string = '';
  user: UserConfig;
  
  @Output() filesSelected = new EventEmitter<File[]>();

  protected authService = inject(OidcSecurityService);
  
  files: File[] = [];
  previews: string[] = [];
  today = Date.now();

  ngOnInit(): void {
    this.authService.checkAuth().subscribe((res: any) => {
      this.user = res.userData;
    });
    if (this.allowedTypes.includes('doc')) {
      this.mimeTypes.push(...['application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']);
      this.acceptedExtensions = `${this.acceptedExtensions}.doc,.docx,`;
    }
    if (this.allowedTypes.includes('image')) {
      this.mimeTypes.push(...['image/png', 'image/jpeg']);
      this.acceptedExtensions = `${this.acceptedExtensions}.jpg, .jpeg, .png,`;
    }
    if (this.allowedTypes.includes('pdf')) {
      this.mimeTypes.push(...['application/pdf']);
      this.acceptedExtensions = `${this.acceptedExtensions}.pdf,`;
    }
    if (this.allowedTypes.includes('xls')) {
      this.mimeTypes.push(...['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet']);
      this.acceptedExtensions = `${this.acceptedExtensions}.xls, .xlsx,`;
    }
    if (this.allowedTypes.includes('ppt')) {
      this.mimeTypes.push(...['application/vnd.openxmlformats-officedocument.presentationml.presentation', 'application/vnd.ms-powerpoint']);
      this.acceptedExtensions = `${this.acceptedExtensions}.ppt, .pptx,`;
    }
    if (this.allowedTypes.includes('csv')) {
      this.mimeTypes.push(...['text/csv']);
      this.acceptedExtensions = `${this.acceptedExtensions}.csv,`;
    }
  }

  onFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    this.handleFiles(input.files);
  }

  handleFiles(fileList: FileList) {
    Array.from(fileList).forEach((file) => {
      if (this.mimeTypes.length && !this.mimeTypes.includes(file.type)) {
        alert(`Invalid file type: ${file.name}, Please upload files with extensions ${this.allowedTypes.join(', ')}`);
        return;
      }

      if (file.size > this.maxSizeMB * 1024 * 1024) {
        alert(`File too large: ${file.name} (Max: ${this.maxSizeMB}MB)`);
        return;
      }

      this.previewFile(file);
      this.files.push(file);
    });
    this.filesSelected.emit(this.files);
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
