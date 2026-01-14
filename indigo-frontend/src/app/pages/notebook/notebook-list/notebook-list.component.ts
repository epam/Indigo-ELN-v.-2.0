import { ButtonComponent } from '@core/components/common/button/button.component';
import { DropdownMenuItem } from '@core/components/common/dropdown-menu/dropdown-menu.i';
import { ListHeaderComponent, SortChangeEvent } from '@core/components/common/list-header/list-header.component';
import { InfiniteLoaderComponent } from '@core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteScrollBase } from '@core/components/util/infinite-scroll.base';
import { ClassPickerPipe } from '@core/pipes/classPicker.pipe';
import { Notebook } from '@core/types/entities/notebook.i';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute } from '@angular/router';
import { Subscription, take } from 'rxjs';
import { NotebookAddComponent } from '@pages/notebook/notebook-add/notebook-add.component';
import { NotebookItemComponent } from '@pages/notebook/notebook-item/notebook-item.component';
import {
  ProjectOverviewWidgetDirective
} from '@pages/project/projects-overview-widget/directives/project-overview-widget.directive';

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
    ListHeaderComponent
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
  headerSortOptions: DropdownMenuItem[] = [];

  constructor(activatedRoute: ActivatedRoute) {
    super();
    activatedRoute.parent.params.pipe(take(1)).subscribe((params) => {
      this.projectId = params['id'];
      this.config.loadUrl = `projects/${this.projectId}/notebooks`;
      this.setup({
        loadUrl: `projects/${this.projectId}/notebooks`,
        sortOptions: [
          {
            label: 'Sort by: Earliest',
            value: 'createdAt',
            defaultOrder: 'asc',
          },
          {
            label: 'Sort by: Latest',
            value: 'createdAt',
            defaultOrder: 'desc',
          },
        ],
        defaultSort: {
          sortBy: 'createdAt',
          sortOrder: 'asc',
        },
      });

      this.headerSortOptions = this.getSortOptions().map((option) => ({
        label: `${option.label}`,
        value: `${option.value}:${option.defaultOrder}`,
        icon: 'indicon-sort',
      }));
    });
    this.initialize();
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

  onSearch(value: string) {
    this.search(value);
  }

  onSortChange(event: SortChangeEvent) {
    this.sort(event.sortBy, event.sortOrder);
  }

  onViewChange(view: string) {
    this.selectedView = view as 'grid' | 'list';
  }

  onMyEntitiesOnlyChange(value: boolean) {
    this.filters['createdByMe'] = value;
    this.dataBh.next([]);
    this.fetchDataAndUpdateQueryParams();
  }
}
