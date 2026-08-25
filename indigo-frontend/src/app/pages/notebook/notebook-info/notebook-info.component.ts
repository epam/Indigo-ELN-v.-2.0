import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { NotebookService } from '@core/services/notebook/notebook.service';
import { TeamComponent } from '@/core/components/common/team/team.component';
import { TeamComponentConfig } from '@/core/components/common/team/team.config';
import { NotebookEditComponent } from '@pages/notebook/notebook-edit/notebook-edit.component';
import { MatDialog } from '@angular/material/dialog';
import { AttachmentsComponent } from '@core/components/common/attachments/attachments.component';
import { Attachment } from '@core/types/entities/attachment.i';
import { PermissionService } from '@core/services/permission/permission.service';
import { ApplicationPermission } from '@core/types/entities/user.i';
import { computed } from '@angular/core';

@Component({
  selector: 'eln-notebook-info',
  standalone: true,
  imports: [CommonModule, ButtonComponent, CardComponent, TeamComponent, AttachmentsComponent],
  templateUrl: './notebook-info.component.html',
})
export class NotebookInfoComponent {
  private store = inject(NotebookService);
  private dialog = inject(MatDialog);
  private permissionService = inject(PermissionService);

  notebook = this.store.notebook;
  isLoading = this.store.isLoading;
  hasError = this.store.hasError;
  applicationPermission = ApplicationPermission;
  canEditNotebook = computed(() => {
    return this.permissionService.hasEntityPermission(ApplicationPermission.EDIT_NOTEBOOKS, this.notebook());
  });

  openEditDialog() {
    if (!this.notebook() || !this.canEditNotebook()) return;

    const dialogRef = this.dialog.open(NotebookEditComponent, {
      data: { notebook: this.notebook() },
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'refresh') this.store.refresh();
    });
  }

  notebookTeamConfig: TeamComponentConfig = {
    buildAccessEndpoint: (id: string) => `notebooks/${id}/access`,
  };

  onAttachmentsChanged(attachments: Attachment[]) {
    this.notebook.update((n) => ({ ...n, attachments }));
  }
}
