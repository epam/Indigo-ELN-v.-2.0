import { AttachmentComponent } from '@/core/components/common/attachment/attachment.component';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ChipComponent } from '@/core/components/common/chip/chip.component';
// import { TeamComponent } from '@/core/components/project/team/team.component';
import { ApiService } from '@/core/services/api.service';
import { Project } from '@/core/types/entities/project.i';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'eln-project-info',
  standalone: true,
  imports: [
    CommonModule,
    ButtonComponent,
    ChipComponent,
    AttachmentComponent,
    // TeamComponent TODO Show team members (available in project.team response? or where?),
    CardComponent,
  ],
  templateUrl: './project-info.component.html',
})
export class ProjectInfoComponent implements OnInit, OnDestroy {
  activatedRoute = inject(ActivatedRoute);
  private destroy$ = new Subject<void>();

  constructor(protected service: ApiService<Project>) { }

  project: Project | null = null;
  isLoading = false;
  hasError = false;

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
}
