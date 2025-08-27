import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { NotebookDetail } from '@/core/types/entities/notebook-detail.i';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { NotebookService } from '../../../../core/services/notebook/notebook.service';

@Component({
    selector: 'eln-notebook-info',
    standalone: true,
    imports: [CommonModule, ButtonComponent, CardComponent],
    templateUrl: './notebook-info.component.html',
})
export class NotebookInfoComponent {
    private store = inject(NotebookService);

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
