import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { NotebookDetail } from '@/core/types/entities/notebook-detail.i';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { NotebookService } from '../../../../core/services/notebook/notebook.service';
import { TeamComponent, TeamComponentConfig } from '@/core/components/common/team/team.component';

@Component({
    selector: 'eln-notebook-info',
    standalone: true,
    imports: [CommonModule, ButtonComponent, CardComponent, TeamComponent],
    templateUrl: './notebook-info.component.html',
})
export class NotebookInfoComponent {
    private store = inject(NotebookService);

    notebookTeamConfig: TeamComponentConfig = {
      title: 'Notebook Team',
      buildAccessEndpoint: (id: string) => `notebooks/${id}/access`,
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
