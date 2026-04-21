import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterOutlet } from '@angular/router';
import { take } from 'rxjs';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { NotebookService } from '@core/services/notebook/notebook.service';
import { ExperimentAddComponent } from '@pages/experiment/experiment-add/experiment-add.component';
import { MatDialog } from '@angular/material/dialog';
import { ProjectTabButtonComponent } from '@pages/project/project-tab-button/project-tab-button.component';

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
  dialog = inject(MatDialog);

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
    const projectId = this.activatedRoute.snapshot.paramMap.get('projectId');

    if (notebookId && projectId) {
      const base = `/projects/${projectId}/notebooks/${notebookId}`;
      this.infoUrl = base;
      this.experimentsUrl = `${base}/experiments`;
      this.store.load(notebookId);
    }
  }

  async openExperimentModal() {
    const ref = this.dialog.open(ExperimentAddComponent);
    ref.componentInstance.notebookId = this.activatedRoute.snapshot.paramMap.get('notebookId');
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
