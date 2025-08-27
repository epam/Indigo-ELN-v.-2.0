import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterOutlet } from '@angular/router';
import { ProjectTabButtonComponent } from '@/core/components/project/project-tab-button/project-tab-button.component';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from "@/core/components/common/card/card.component";
import { NotebookService } from '../../../../core/services/notebook/notebook.service';

@Component({
    selector: 'eln-notebook-detail',
    templateUrl: './notebook-detail.component.html',
    standalone: true,
    imports: [RouterOutlet, ProjectTabButtonComponent, ButtonComponent, CardComponent],
    providers: [NotebookService],
})
export class NotebookDetailComponent implements OnInit {
    activatedRoute = inject(ActivatedRoute);
    store = inject(NotebookService);

    get notebook() {
        return this.store.notebook();
    }

    get isLoading() {
        return this.store.isLoading();
    }

    get hasError() {
        return this.store.hasError();
    }

    public infoUrl = '';
    public experimentsUrl = '';

    ngOnInit() {
        const notebookId = this.activatedRoute.snapshot.paramMap.get('notebookId');
        if (notebookId) {
            const base = `/notebooks/${notebookId}`;
            this.infoUrl = base;
            this.experimentsUrl = `${base}/experiments`;
            this.store.load(notebookId);
        }
    }
}
