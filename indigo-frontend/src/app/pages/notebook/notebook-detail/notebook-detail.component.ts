import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterOutlet } from '@angular/router';
import { ProjectTabButtonComponent } from '@/core/components/project/project-tab-button/project-tab-button.component';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from "@/core/components/common/card/card.component";
import { NotebookStore } from '../notebook.store';

@Component({
    selector: 'eln-notebook-detail',
    templateUrl: './notebook-detail.component.html',
    standalone: true,
    imports: [RouterOutlet, ProjectTabButtonComponent, ButtonComponent, CardComponent],
    providers: [NotebookStore],
})
export class NotebookDetailComponent implements OnInit {
    activedRoute = inject(ActivatedRoute);
    store = inject(NotebookStore);

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
        this.activedRoute.params.subscribe((params) => {
            const notebookId = params['notebookId'];
            const base = `/notebooks/${notebookId}`;
            this.infoUrl = base;
            this.experimentsUrl = `${base}/experiments`;
            if (notebookId) this.store.load(notebookId);
        });
    }
}
