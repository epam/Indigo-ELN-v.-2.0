import { AttachmentComponent } from '@/core/components/common/attachment/attachment.component';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ChipComponent } from '@/core/components/common/chip/chip.component';
import { TeamComponent } from '@/core/components/common/team/team.component';
import { ApiService } from '@/core/services/api.service';
import { Project } from '@/core/types/entities/project.i';
import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { of, Subject, take } from 'rxjs';
import { catchError, takeUntil } from 'rxjs/operators';
import { FileUploadComponent } from "@/core/components/common/file-upload/file-upload.component";
import { Attachment } from '@/core/types/entities/attachment.i';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ProjectAddComponent } from '../project-add/project-add.component';
import { TeamComponentConfig } from '@/core/components/common/team/team.config';
import { NotebookAddComponent } from '../notebook/notebook-add/notebook-add.component';
import { ProjectOverviewWidgetDirective } from '../../../../core/components/project/projects-overview-widget/directives/project-overview-widget.directive';

enum projectInfoModalEnum {
  EDIT = 'edit',
  NOTEBOOK = 'notebook'
}
@Component({
  selector: 'eln-project-info',
  standalone: true,
  imports: [
    CommonModule,
    ButtonComponent,
    ChipComponent,
    AttachmentComponent,
    TeamComponent,
    CardComponent,
    FileUploadComponent,
    ProjectOverviewWidgetDirective
  ],
  templateUrl: './project-info.component.html',
})
export class ProjectInfoComponent implements OnInit, OnDestroy {
  projectInfoModalEnum = projectInfoModalEnum;

  activatedRoute = inject(ActivatedRoute);
  dialog = inject(MatDialog);
  service = inject(ApiService);

  project: Project | null = null;

  isLoading = false;
  hasError = false;
  isUploadingAttachment = false;

  private destroy$ = new Subject<void>();

  projectTeamConfig: TeamComponentConfig = {
    title: 'Project Team',
    buildAccessEndpoint: (id: string) => `projects/${id}/access`,
  };

  ngOnInit() {
    this.activatedRoute.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ id }) => {
        if (id) this.loadProject(id);
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onAttachmentDeleted(attachmentId: string): void {
    if (this.project) {
      // Remove the deleted attachment from the local array
      this.project.attachments = this.project.attachments.filter(
        attachment => attachment.id !== attachmentId
      );
    }
  }

  onUpload(files: File[]): void {
    this.isUploadingAttachment = true;

    // Validate file existence
    const file = files[0];
    if (!file || !this.project) {
      console.error('No project or file selected for upload');
      this.isUploadingAttachment = false;
      return;
    }
    const formData = new FormData();
    formData.append('file', file, file.name);

    // Use the ApiService request method for file upload
    this.service.request<Attachment[]>(
      'post',
      `projects/${this.project.id}/attachments`,
      formData,
    )
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          console.error('Failed to upload attachment:', err);
          this.isUploadingAttachment = false;
          return of(null);
        })
      )
      .subscribe({
        next: (attachments) => {
          if (this.project && attachments) this.project.attachments = attachments;
        },
        error: (err) => {
          console.error('Upload error:', err);
        }, complete: () => {
          this.isUploadingAttachment = false;
        }
      });
  }

  private loadProject(id: string): void {
    this.isLoading = true;
    this.hasError = false;
    this.service.request<Project>('get', `projects/${id}`)
      .pipe(
        takeUntil(this.destroy$),
        catchError((err) => {
          console.error('Failed to load project:', err);
          this.hasError = true;
          this.isLoading = false;
          return of(null);
        })
      )
      .subscribe({
        next: (project) => {
          this.isLoading = false;
          if (project) this.project = project;
        },
      });
  }

  async openModal(mode: projectInfoModalEnum) {
    let ref: MatDialogRef<ProjectAddComponent | NotebookAddComponent>;
    
    if (mode === projectInfoModalEnum.EDIT) {
      ref = this.dialog.open(ProjectAddComponent, {
        data: {
          project: this.project,
        },
      });
    }

    if (mode === projectInfoModalEnum.NOTEBOOK) {
      ref = this.dialog.open(NotebookAddComponent);
      (ref.componentInstance as NotebookAddComponent).projectId = this.project.id;
    }
    
    ref?.afterClosed()
      .pipe(take(1))
      .subscribe((result) => {
        if (result === 'refresh') {
          this.loadProject(this.project.id);
        }
      });
  }
}
