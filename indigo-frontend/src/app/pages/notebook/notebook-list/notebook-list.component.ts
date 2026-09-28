import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { DropdownMenuItem } from '@core/components/common/dropdown-menu/dropdown-menu.i';
import { ListHeaderComponent, SortChangeEvent } from '@core/components/common/list-header/list-header.component';
import { InfiniteLoaderComponent } from '@core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteScrollBase } from '@core/components/util/infinite-scroll.base';
import { ClassPickerPipe } from '@core/pipes/classPicker.pipe';
import { Notebook } from '@core/types/entities/notebook.i';
import { ProjectService } from '@core/services/project/project.service';
import { NotebookAddComponent } from '@pages/notebook/notebook-add/notebook-add.component';
import { NotebookItemComponent } from '@pages/notebook/notebook-item/notebook-item.component';
import { ProjectOverviewWidgetDirective } from '@pages/project/projects-overview-widget/directives/project-overview-widget.directive';
import { take } from 'rxjs';

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
  ],
})
export class NotebookListComponent extends InfiniteScrollBase<Notebook> implements OnInit, OnChanges {
  dialog = inject(MatDialog);
  projectService = inject(ProjectService);
  selectedView: 'grid' | 'list' = 'grid';
  @Input() projectId!: string;
  headerSortOptions: DropdownMenuItem[] = [];

  ngOnInit() {
    this.headerSortOptions = this.getSortOptions().map((option) => ({
      label: `${option.label}`,
      value: option.value,
      icon: 'indicon-sort',
    }));
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['projectId']) {
      this.setup({
        loadUrl: `projects/${this.projectId}/notebooks`,
        sortOptions: [
          {
            label: 'Sorting by: Earliest',
            value: 'EARLIEST',
          },
          {
            label: 'Sorting by: Latest',
            value: 'LATEST',
          },
        ],
        defaultSort: {
          sort: 'EARLIEST',
        },
      });
    }
  }
  refreshList(): void {
    this.reload();
  }

  async openModal() {
    if (!this.projectService.canCreateNotebook()) return;

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
    this.sort(event.sort);
  }

  onViewChange(view: string) {
    this.selectedView = view as 'grid' | 'list';
  }

  onMyEntitiesOnlyChange(value: boolean) {
    this.setBooleanFilter('createdByMe', value);
    this.reload();
  }
}
