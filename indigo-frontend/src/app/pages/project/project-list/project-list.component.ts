import {
  ButtonToggleComponent,
  ToggleOption,
} from '@/core/components/common/button-toggle/button-toggle.component';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { ToggleComponent } from '@/core/components/common/toggle/toggle.component';
import { ProjectItemComponent } from '@/core/components/project/project-item/project-item.component';
import { ProjectOverviewWidgetDirective } from '@/core/components/project/projects-overview-widget/directives/project-overview-widget.directive';
import { InfiniteLoaderComponent } from '@/core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteScrollComponent } from '@/core/components/util/infinite-scroll.component';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { Project } from '@/core/types/entities/project.i';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Subscription } from 'rxjs';
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
    ButtonToggleComponent,
    MatSlideToggleModule,
    ToggleComponent,
    ClassPickerPipe,
    InfiniteLoaderComponent,
    ProjectOverviewWidgetDirective,
    ButtonComponent,
  ],
})
export class ProjectListComponent
  extends InfiniteScrollComponent<Project>
  implements OnDestroy
{
  dialog = inject(MatDialog);
  selectedView: 'grid' | 'list' = 'grid';
  private refreshSub!: Subscription;

  constructor() {
    super();
    this.setup({
      controller: 'projects',
    });
  }

  options: ToggleOption[] = [{ value: 'grid', icon: 'indicon-grid' }];

  viewOptions: ToggleOption[] = [
    { value: 'grid', icon: 'indicon-grid' },
    { value: 'list', icon: 'indicon-list' },
  ];

  refreshList(): void {
    this.reload();
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  async openModal() {
    this.dialog.open(ProjectAddComponent, {
      data: {
        title: 'Add Project',
        fields: [],
      },
    });
  }
}
