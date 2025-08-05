import { ButtonComponent } from '@/core/components/common/button/button.component';
import { ListHeaderComponent } from '@/core/components/common/list-header/list-header.component';
import { NotebookItemComponent } from '@/core/components/project/notebook/notebook-item/notebook-item.component';
import { ProjectOverviewWidgetDirective } from '@/core/components/project/projects-overview-widget/directives/project-overview-widget.directive';
import { InfiniteLoaderComponent } from '@/core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteScrollBase } from '@/core/components/util/infinite-scroll.base';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { Notebook } from '@/core/types/entities/notebook.i';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute } from '@angular/router';
import { Subscription, take } from 'rxjs';
import { NotebookAddComponent } from '../notebook-add/notebook-add.component';
import { ExperimentCardComponent } from '@/app/pages/experiment/experiment-card/experiment-card.component';
import { Experiment } from '@/core/types/entities/experiment.i';

@Component({
  selector: 'eln-notebook-list',
  templateUrl: './notebook-list.component.html',
  standalone: true,
  animations: [
    trigger('viewChange', [
      transition('grid <=> list', [
        style({ opacity: 0, transform: 'scale(0.95)' }),
        animate('200ms ease-out', style({ opacity: 1, transform: 'scale(1)' })),
      ]),
    ]),
  ],
  imports: [
    CommonModule,
    FormsModule,
    NotebookItemComponent,
    MatSlideToggleModule,
    ClassPickerPipe,
    InfiniteLoaderComponent,
    ProjectOverviewWidgetDirective,
    ButtonComponent,
    ListHeaderComponent,
    ExperimentCardComponent
  ],
})
export class NotebookListComponent
  extends InfiniteScrollBase<Notebook>
  implements OnDestroy
{
  dialog = inject(MatDialog);
  selectedView: 'grid' | 'list' = 'grid';
  private refreshSub!: Subscription;
  projectId: string;
  dummyExperiment: Experiment = {
    status: 'Rejected',
    id: '',
    name: 'Experiment 1',
    reactionSchemaUrl: 'assets/reaction-schema-placeholder.png',
    modifiedAt: new Date(2025,6,23),
    modifiedBy: {
      id: 'gg',
      username: 'Jhon',
      displayName: 'Jhon'
    },
    createdAt: new Date(2020,1,1),
    createdBy: {
      id: 'aspdof',
      username: 'Wick',
      displayName: 'Wick'
    }
  }

  constructor(activatedRoute: ActivatedRoute) {
    super();
    activatedRoute.parent.params.pipe(take(1)).subscribe((params) => {
      this.projectId = params['id'];
      this.config.loadUrl = `projects/${this.projectId}/notebooks`;
      this.initialize();
    });
  }

  refreshList(): void {
    this.reload();
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  async openModal() {
    const ref = this.dialog.open(NotebookAddComponent);
    ref.componentInstance.projectId = this.projectId;
    ref
      .afterClosed()
      .pipe(take(1))
      .subscribe((result) => {
        if (result === 'refresh') {
          this.refreshList();
        }
      });
  }
}
