import { CommonModule } from '@angular/common';
import { Component, effect, inject, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, RouterOutlet } from '@angular/router';
import { take } from 'rxjs';

import { BreadcrumbsComponent } from '@/core/components/breadcrumbs/breadcrumbs.component';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { BreadcrumbsStateService } from '@/core/services/breadcrumbs/breadcrumbs.state.service';
import { NotebookService } from '@core/services/notebook/notebook.service';
import { ExperimentAddComponent } from '@pages/experiment/experiment-add/experiment-add.component';
import { ProjectTabButtonComponent } from '@pages/project/project-tab-button/project-tab-button.component';

@Component({
  selector: 'eln-notebook-detail',
  templateUrl: './notebook-detail.component.html',
  standalone: true,
  imports: [
    RouterOutlet,
    ProjectTabButtonComponent,
    ButtonComponent,
    CardComponent,
    BreadcrumbsComponent,
    CommonModule,
  ],
  providers: [NotebookService],
})
export class NotebookDetailComponent implements OnInit {
  activatedRoute = inject(ActivatedRoute);
  store = inject(NotebookService);
  dialog = inject(MatDialog);
  breadcrumbsState = inject(BreadcrumbsStateService);

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

  private readonly breadcrumbsEffect = effect(() => {
    const notebook = this.store.notebook();
    this.breadcrumbsState.setItems([
      { label: 'All Projects', url: '/projects', active: false },
      {
        label: `Project:  ${notebook?.projectName ?? ''}`,
        url: notebook ? `/projects/${notebook.projectId}` : null,
        active: false,
      },
      {
        label: `Notebook: ${notebook?.name ?? ''}`,
        active: true,
      },
    ]);
  });

  ngOnInit(): void {
    const notebookId = this.activatedRoute.snapshot.paramMap.get('notebookId');

    if (!notebookId) {
      return;
    }

    this.store.load(notebookId).subscribe((notebook) => {
      this.infoUrl = `/notebooks/${notebook.id}`;
      this.experimentsUrl = `/notebooks/${notebook.id}/experiments`;
    });
  }

  async openExperimentModal(): Promise<void> {
    const ref = this.dialog.open(ExperimentAddComponent);
    const notebook = this.notebook;

    ref.componentInstance.notebookId = notebook?.id;

    ref
      .afterClosed()
      .pipe(take(1))
      .subscribe((result) => {
        if (result === 'refresh') {
          this.store.refresh();
        }
      });
  }
}
