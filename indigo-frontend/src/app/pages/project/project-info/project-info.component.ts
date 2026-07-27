import { AttachmentsComponent } from '@/core/components/common/attachments/attachments.component';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ChipComponent } from '@/core/components/common/chip/chip.component';
import { TeamComponent } from '@/core/components/common/team/team.component';
import { TeamComponentConfig } from '@/core/components/common/team/team.config';
import { Attachment } from '@/core/types/entities/attachment.i';
import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { NotebookAddComponent } from '@pages/notebook/notebook-add/notebook-add.component';
import { ProjectOverviewWidgetDirective } from '@pages/project/projects-overview-widget/directives/project-overview-widget.directive';
import { take } from 'rxjs';
import { ProjectAddComponent } from '../project-add/project-add.component';
import { ProjectService } from '@core/services/project/project.service';
import { Router } from '@angular/router';

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
export class ProjectInfoComponent {
  projectInfoModalEnum = projectInfoModalEnum;

  @Input() projectId!: string;
  dialog = inject(MatDialog);
  projectService = inject(ProjectService);
  router = inject(Router);

  project = this.projectService.project;
  isLoading = this.projectService.isLoading;
  hasError = this.projectService.hasError;

  projectTeamConfig: TeamComponentConfig = {
    buildAccessEndpoint: (id: string) => `projects/${id}/access`,
    removeMemberCascadeCheckboxLabel: 'Also remove this member from all Notebooks and Experiments in this Project.',
  };

  onAttachmentsChanged(attachments: Attachment[]) {
    if (!this.project()) {
      return;
    }

    this.project.update((p) => ({ ...p, attachments }));
  }

  async openModal(mode: projectInfoModalEnum) {
    let ref: MatDialogRef<ProjectAddComponent | NotebookAddComponent> | undefined;

    if (mode === projectInfoModalEnum.EDIT) {
      ref = this.dialog.open(ProjectAddComponent, {
        data: {
          project: this.project(),
        },
      });
    }

    if (mode === projectInfoModalEnum.NOTEBOOK) {
      ref = this.dialog.open(NotebookAddComponent);
      (ref.componentInstance as NotebookAddComponent).projectId = this.projectId;
    }

    ref
      ?.afterClosed()
      .pipe(take(1))
      .subscribe((result) => {
        if (result === 'refresh' && mode === projectInfoModalEnum.EDIT) {
          this.projectService.refresh();
        }
      });
  }
}
