import { UserService } from '@/core/services/user.service';
import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { take } from 'rxjs';
import { FileSizePipe } from './file-size.pipe';
import { fileTypeConfig } from './file-upload.config';

@Component({
  imports: [CommonModule, FileSizePipe],
  standalone: true,
  selector: 'eln-file-upload',
  templateUrl: './file-upload.component.html',
})
export class FileUploadComponent implements OnInit {
  @Input() maxSizeMB = 5; // Default max file size (5MB)
  @Input() allowedTypes = ['doc', 'image', 'pdf', 'xls', 'ppt', 'csv'];
  mimeTypes: string[] = [];
  acceptedExtensions = '';
  userService = inject(UserService);
  user;
  @Output() filesSelected = new EventEmitter<File[]>();

  files: File[] = [];
  previews: string[] = [];
  today = Date.now();

  ngOnInit(): void {
    this.userService.user$.pipe(take(1)).subscribe((user) => {
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
    Array.from(fileList).forEach((file) => {
      if (this.mimeTypes.length && !this.mimeTypes.includes(file.type)) {
        alert(
          `Invalid file type: ${file.name}, Please upload files with extensions ${this.allowedTypes.join(', ')}`,
        );
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
