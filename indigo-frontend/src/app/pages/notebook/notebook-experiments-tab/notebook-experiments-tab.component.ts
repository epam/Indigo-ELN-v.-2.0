import { ButtonComponent } from '@/core/components/common/button/button.component';
import { DropdownMenuItem } from '@/core/components/common/dropdown-menu/dropdown-menu.i';
import { ListHeaderComponent, SortChangeEvent } from '@/core/components/common/list-header/list-header.component';
import { InfiniteLoaderComponent } from '@/core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteScrollBase } from '@/core/components/util/infinite-scroll.base';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ExperimentItemComponent } from '@pages/experiment/experiment-item/experiment-item.component';
import { ProjectOverviewWidgetDirective } from '@pages/project/projects-overview-widget/directives/project-overview-widget.directive';
import { ExperimentStatusNames } from '@/core/enums/experiment-status.enum';
import { CheckboxDropdownItem } from '@/core/components/common/checkbox-dropdown/checkbox-dropdown.i';

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
export class NotebookExperimentsTabComponent extends InfiniteScrollBase<ExperimentDetail> implements OnChanges, OnInit {
  dialog = inject(MatDialog);
  selectedView: 'grid' | 'list' = 'grid';
  headerSortOptions: DropdownMenuItem[] = [];
  @Input() notebookId!: string;
  headerFilterOptions: CheckboxDropdownItem[] = [];

  ngOnInit() {
    this.headerSortOptions = this.getSortOptions().map((option) => ({
      label: `${option.label}`,
      value: `${option.value}:${option.defaultOrder}`,
      icon: 'indicon-sort',
    }));

    this.headerFilterOptions = this.getFilterOptions();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['notebookId']) {
      this.setup({
        loadUrl: `notebooks/${this.notebookId}/experiments`,
        sortOptions: [
          {
            label: 'Sorting by: Earliest',
            value: 'createdAt',
            defaultOrder: 'EARLIEST',
          },
          {
            label: 'Sorting by: Latest',
            value: 'createdAt',
            defaultOrder: 'LATEST',
          },
        ],
        filterOptions: [...this.getStatusOptions()],
        defaultSort: {
          sortBy: 'createdAt',
          sort: 'EARLIEST',
        },
      });
    }
  }

  onSearch(value: string) {
    this.search(value);
  }

  onSortChange(event: SortChangeEvent) {
    this.sort(event.sortBy, event.sort);
  }

  onViewChange(view: string) {
    this.selectedView = view as 'grid' | 'list';
  }

  onMyEntitiesOnlyChange(value: boolean) {
    this.setBooleanFilter('createdByMe', value);
    this.reload();
  }

  onFilterChange(items: CheckboxDropdownItem[]) {
    this.headerFilterOptions = items.map((item) => ({ ...item }));

    const selectedValues = items.filter((item) => item.checked).map((item) => item.value);

    if (selectedValues.length > 0) {
      this.filters['status'] = selectedValues;
    } else {
      delete this.filters['status'];
    }

    this.reload();
  }

  getStatusOptions() {
    return Object.entries(ExperimentStatusNames).map(([key, value]) => ({
      label: value,
      value: key,
      checked: false,
    }));
  }
}
