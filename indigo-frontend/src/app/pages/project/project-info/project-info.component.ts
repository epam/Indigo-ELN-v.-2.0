import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ChipComponent } from '@/core/components/common/chip/chip.component';
import { TeamComponent } from '@/core/components/common/team/team.component';
import { ApiService } from '@/core/services/api.service';
import { Project } from '@/core/types/entities/project.i';
import { CommonModule } from '@angular/common';
import { Component, effect, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { finalize, Subject, take } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ProjectAddComponent } from '../project-add/project-add.component';
import { TeamComponentConfig } from '@/core/components/common/team/team.config';
import { NotebookAddComponent } from '@pages/notebook/notebook-add/notebook-add.component';
import { ProjectOverviewWidgetDirective } from '@pages/project/projects-overview-widget/directives/project-overview-widget.directive';
import { BreadcrumbsStateService } from '@/core/services/breadcrumbs/breadcrumbs.state.service';
import { AttachmentsComponent } from '@core/components/common/attachments/attachments.component';
import { Attachment } from '@core/types/entities/attachment.i';

enum projectInfoModalEnum {
  EDIT = 'edit',
  NOTEBOOK = 'notebook',
}

@Component({
  selector: 'eln-project-info',
  standalone: true,
  imports: [
    CommonModule,
    ButtonComponent,
    ChipComponent,
    TeamComponent,
    CardComponent,
    ProjectOverviewWidgetDirective,
    AttachmentsComponent,
  ],
  templateUrl: './project-info.component.html',
})
export class ProjectInfoComponent implements OnInit, OnDestroy {
  projectInfoModalEnum = projectInfoModalEnum;

  activatedRoute = inject(ActivatedRoute);
  dialog = inject(MatDialog);
  service = inject(ApiService);
  breadcrumbsState = inject(BreadcrumbsStateService);

  project: Project | null = null;

  isLoading = false;
  hasError = false;

  private destroy$ = new Subject<void>();

  private readonly breadcrumbsEffect = effect(() => {});

  projectTeamConfig: TeamComponentConfig = {
    buildAccessEndpoint: (id: string) => `projects/${id}/access`,
    removeMemberCascadeCheckboxLabel: 'Also remove this member from all Notebooks and Experiments in this Project.',
  };

  ngOnInit() {
    this.breadcrumbsState.setItems([
      { label: 'All Projects', url: '/projects', active: false },
      { label: `Project: `, active: true },
    ]);
    this.activatedRoute.params.pipe(takeUntil(this.destroy$)).subscribe(({ id }) => {
      if (id) {
        this.loadProject(id);
      }
    });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onAttachmentsChanged(attachments: Attachment[]) {
    this.project.attachments = attachments;
  }

  private loadProject(id: string): void {
    this.isLoading = true;
    this.hasError = false;

    this.service
      .request<Project>('get', `projects/${id}`)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (project) => {
          this.project = project;

          this.breadcrumbsState.setItems([
            { label: 'All Projects', url: '/projects', active: false },
            { label: `Project: ${project.name}`, active: true },
          ]);
        },
        error: () => {
          this.hasError = true;
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

    ref
      ?.afterClosed()
      .pipe(take(1))
      .subscribe((result) => {
        if (result === 'refresh') {
          this.loadProject(this.project.id);
        }
      });
  }
}
