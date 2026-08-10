import { ButtonComponent } from '@/core/components/common/button/button.component';
import { DropdownMenuItem } from '@/core/components/common/dropdown-menu/dropdown-menu.i';
import { ListHeaderComponent, SortChangeEvent } from '@/core/components/common/list-header/list-header.component';
import { InfiniteLoaderComponent } from '@/core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteScrollBase } from '@/core/components/util/infinite-scroll.base';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { BreadcrumbsStateService } from '@/core/services/breadcrumbs/breadcrumbs.state.service';
import { PermissionService } from '@/core/services/permission/permission.service';
import { Project } from '@/core/types/entities/project.i';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Subscription, take } from 'rxjs';

import { ApplicationPermission } from '@core/types/entities/user.i';
import { ProjectItemComponent } from '@pages/project/project-item/project-item.component';
import { ProjectOverviewWidgetDirective } from '@pages/project/projects-overview-widget/directives/project-overview-widget.directive';
import { ProjectAddComponent } from '../project-add/project-add.component';

@Component({
  selector: 'eln-project-list',
  templateUrl: './project-list.component.html',
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
    ProjectItemComponent,
    MatSlideToggleModule,
    ClassPickerPipe,
    InfiniteLoaderComponent,
    ProjectOverviewWidgetDirective,
    ButtonComponent,
    ListHeaderComponent,
  ],
})
export class ProjectListComponent extends InfiniteScrollBase<Project> implements OnInit, OnDestroy {
  dialog = inject(MatDialog);
  breadcrumbsState = inject(BreadcrumbsStateService);
  permissionService = inject(PermissionService);
  applicationPermission = ApplicationPermission;
  canCreateProject = computed(() => this.permissionService.hasPermission(ApplicationPermission.CREATE_PROJECTS));

  selectedView: 'grid' | 'list' = 'grid';
  private refreshSub!: Subscription;

  headerSortOptions: DropdownMenuItem[] = [];

  constructor() {
    super();

    // breadcrumbs are shown in ProjectsOverviewWidgetComponent, but initialized here, because
    // ProjectsOverviewWidgetComponent is not reinitialized when navigating inside /projects paths
    this.breadcrumbsState.setItems([{ label: 'All Projects', url: '/projects', active: true }]);

    this.setup({
      loadUrl: 'projects',
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
      defaultSort: {
        sortBy: 'createdAt',
        sort: 'EARLIEST',
      },
    });

    this.headerSortOptions = this.getSortOptions().map((option) => ({
      label: `${option.label}`,
      value: `${option.value}:${option.defaultOrder}`,
      icon: 'indicon-sort',
    }));
  }

  ngOnInit(): void {
    this.breadcrumbsState.setItems([
      {
        label: 'All Projects',
        url: '/projects',
        active: true,
      },
    ]);
  }

  refreshList(): void {
    this.reload();
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  async openModal() {
    if (!this.canCreateProject()) {
      return;
    }

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
    this.sort(event.sortBy, event.sort);
  }

  onViewChange(view: string) {
    this.selectedView = view as 'grid' | 'list';
  }

  onMyEntitiesOnlyChange(value: boolean) {
    this.setBooleanFilter('createdByMe', value);
    this.reload();
  }
}
