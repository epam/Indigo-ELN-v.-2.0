import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { FileUploadComponent } from '@/core/components/common/file-upload/file-upload.component';
import { InputComponent } from '@/core/components/common/input/input.component';
import { ModalComponent } from '@/core/components/common/modal/modal.component';
import { ApiService } from '@/core/services/api.service';
import { ProjectRefreshService } from '../project-refresh.service';

@Component({
  standalone: true,
  selector: 'app-project-add',
  imports: [ModalComponent, ButtonComponent, InputComponent, MatInputModule, FileUploadComponent, FormsModule, ReactiveFormsModule, CommonModule],
  templateUrl: './project-add.component.html'
})
export class ProjectAddComponent<T> implements OnInit {
  @ViewChild('addProjectModal') modalComponent!: ModalComponent;
  @ViewChild(FileUploadComponent) fileUpload!: FileUploadComponent;
  files: File[] = [];
  formGroup!: FormGroup;
  chips: string[] = [];
  isSubmitted = false;
  allowedFileTypes = ['doc'];

  constructor(private service: ApiService<T>, private fb: FormBuilder, private projectRefreshService: ProjectRefreshService){
    this.service = inject(ApiService);
  }

  ngOnInit(): void {
    this.service.setup('projects');
    this.formGroup = this.fb.group({
      name: ['', [Validators.required]],
      keywords: [''],
      literature: [''],
      description: ['']
    });
  }

  addChip(event: any) {
    if (this.formGroup.value.keywords.trim() && event.key === 'Enter') {
      this.chips.push(this.formGroup.value.keywords.trim());
      this.formGroup.patchValue({'keywords': ''});
    }
  }

  removeChip(index: number) {
    this.chips.splice(index, 1);
  }

  async open() {
    return await this.modalComponent.open();
  }

  close(reason) {
    this.formGroup.reset();
    this.isSubmitted = false;
    this.chips = [];
    this.fileUpload.clearFiles();
    this.modalComponent.close(reason);
  }

  onFilesUploaded(e: any) {
    this.files.push(e);
  }

  createProject() {
    this.isSubmitted = true;
    if (this.formGroup.valid) {
      const data = this.formGroup.value;
      data.keywords = this.chips;
      this.service.create(data).subscribe({
        next: (res: any) => {
        if(res?.id && this.files.length > 0) {
          this.service.setup('projects', {
            createUrl: '{projectId}/attachments'
          });
          const formData = new FormData();
          this.files.forEach((file) => {
            formData.append('file', file[0]);
          });
          this.service.uploadAttachment(res.id, formData).subscribe({
            next: (response) => {
              this.close('projectAdded');
              this.projectRefreshService.triggerRefresh();
            },
            error: (error) => {
              this.close('projectAdded');
              alert(error.message); // Display error message to user
            }
          })
        } else {
          this.close('projectAdded');
          this.projectRefreshService.triggerRefresh();
        }
      },
      error: (error) => {
        this.close('projectAddError');
        alert(error.message); // Display error message to user
      }}
      )
    }
  }
}
