import { ButtonComponent } from '@/core/components/common/button/button.component';
import { ListHeaderComponent } from '@/core/components/common/list-header/list-header.component';
import { InfiniteLoaderComponent } from '@/core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteScrollBase } from '@/core/components/util/infinite-scroll.base';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute } from '@angular/router';
import { ProjectOverviewWidgetDirective } from '@pages/project/projects-overview-widget/directives/project-overview-widget.directive';
import { ExperimentItemComponent } from '@pages/experiment/experiment-item/experiment-item.component';

@Component({
  selector: 'eln-notebook-notebook-experiments-tab',
  templateUrl: './notebook-experiments-tab.component.html',
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
    ExperimentItemComponent,
    MatSlideToggleModule,
    ClassPickerPipe,
    InfiniteLoaderComponent,
    ProjectOverviewWidgetDirective,
    ButtonComponent,
    ListHeaderComponent,
  ],
})
export class NotebookExperimentsTabComponent extends InfiniteScrollBase<ExperimentDetail> {
  dialog = inject(MatDialog);
  selectedView: 'grid' | 'list' = 'grid';
  notebookId: string;
  projectId: string;

  constructor(activatedRoute: ActivatedRoute) {
    super();
    const notebookId =
      activatedRoute.parent?.snapshot.paramMap.get('notebookId');
    const projectId = activatedRoute.parent?.snapshot.paramMap.get('projectId');

    this.notebookId = notebookId || '';
    this.projectId = projectId || '';

    this.setup({
      loadUrl: `notebooks/${this.notebookId}/experiments`,
      sortOptions: [
        { label: 'Name', value: 'name' },
        { label: 'Status', value: 'status' },
        { label: 'Created Date', value: 'createdAt', defaultOrder: 'LATEST' },
        { label: 'Modified Date', value: 'modifiedAt', defaultOrder: 'LATEST' },
      ],
    });
  }
}
