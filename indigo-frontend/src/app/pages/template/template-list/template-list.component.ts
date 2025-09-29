import { Component, inject, Input, OnDestroy } from '@angular/core';
import { ListHeaderComponent, SortChangeEvent } from '@core/components/common/list-header/list-header.component';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ClassPickerPipe } from '@core/pipes/classPicker.pipe';
import { InfiniteLoaderComponent } from '@core/components/util/infinite-loader/infinite-loader.component';
import { ProjectOverviewWidgetDirective } from '@core/components/project/projects-overview-widget/directives/project-overview-widget.directive';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { InfiniteScrollBase } from '@core/components/util/infinite-scroll.base';
import { Project } from '@core/types/entities/project.i';
import { MatDialog } from '@angular/material/dialog';
import { Subscription, take } from 'rxjs';
import { DropdownMenuItem } from '@core/components/common/dropdown-menu/dropdown-menu.i';
import { ProjectAddComponent } from '@pages/project/project-add/project-add.component';
import { RouteAnimationType } from '@core/animations/route-animations';
import { TemplateItemComponent } from '@core/components/template/template-item/template-item.component';

@Component({
  selector: 'eln-template-list',
  templateUrl: './template-list.component.html',
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
    ListHeaderComponent,
    CommonModule,
    FormsModule,
    MatSlideToggleModule,
    ClassPickerPipe,
    InfiniteLoaderComponent,
    ProjectOverviewWidgetDirective,
    ButtonComponent,
    ListHeaderComponent,
    TemplateItemComponent,
  ],
})
export class TemplateListComponent
  extends InfiniteScrollBase<Project>
  implements OnDestroy
{
  @Input() animationType!: RouteAnimationType;
  dialog = inject(MatDialog);
  selectedView: 'list' | 'grid' = 'list';
  private refreshSub!: Subscription;

  headerSortOptions: DropdownMenuItem[] = [];

  constructor() {
    super();
    this.setup({
      loadUrl: 'projects',
      sortOptions: [
        { label: 'Sort by: Earliest', value: 'createdAt', defaultOrder: 'asc' },
        { label: 'Sort by: Latest', value: 'createdAt', defaultOrder: 'desc' },
      ],
      defaultSort: {
        sortBy: 'createdAt',
        sortOrder: 'asc',
      },
    });

    // Convert sort options to dropdown menu items
    this.headerSortOptions = this.getSortOptions().map((option) => ({
      label: `${option.label}`,
      value: `${option.value}:${option.defaultOrder}`,
      icon: 'indicon-sort',
    }));
  }

  refreshList(): void {
    this.reload();
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  async openModal() {
    const ref = this.dialog.open(ProjectAddComponent);
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
}
