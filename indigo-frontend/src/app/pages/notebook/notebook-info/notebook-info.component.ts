import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { NotebookDetail } from '@/core/types/entities/notebook-detail.i';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { NotebookService } from '../../../../core/services/notebook/notebook.service';
import { TeamComponent } from '@/core/components/common/team/team.component';
import { TeamComponentConfig } from '@/core/components/common/team/team.config';
import { NotebookEditComponent } from '@pages/notebook/notebook-edit/notebook-edit.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
    selector: 'eln-notebook-info',
    standalone: true,
    imports: [CommonModule, ButtonComponent, CardComponent, TeamComponent],
    templateUrl: './notebook-info.component.html',
})
export class NotebookInfoComponent {
    private store = inject(NotebookService);
    private dialog = inject(MatDialog);

    openEditDialog() {
        if (!this.notebook) return;

        const dialogRef = this.dialog.open(NotebookEditComponent, {
            data: { notebook: this.notebook },
            disableClose: true,
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result === 'refresh') this.store.refresh();
        });
    }

    notebookTeamConfig: TeamComponentConfig = {
      buildAccessEndpoint: (id: string) => `notebooks/${id}/access`,
      buildNestedAccessEndpoint: (id: string) => `notebooks/${id}/nestedAccess`,
    };

    get notebook(): NotebookDetail | null {
        return this.store.notebook();
    }

    get isLoading(): boolean {
        return this.store.isLoading();
    }

    get hasError(): boolean {
        return this.store.hasError();
    }
}
