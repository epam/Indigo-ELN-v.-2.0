import {
  ButtonToggleComponent,
  ToggleOption,
} from '@/core/components/common/button-toggle/button-toggle.component';
import { ToggleComponent } from '@/core/components/common/toggle/toggle.component';
import { ProjectItemComponent } from '@/core/components/project/project-item/project-item.component';
import { InfiniteScrollComponent } from '@/core/components/util/infinite-scroll.component';
import { IsInViewportDirective } from '@/core/directives/is-in-viewport.directive';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { Project } from '@/core/types/entities/project.i';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  selector: 'app-project-list',
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
    IsInViewportDirective,
    ClassPickerPipe,
  ],
})
export class ProjectListComponent extends InfiniteScrollComponent<Project> {
  selectedView: 'grid' | 'list' = 'grid';

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

  onEnter() {
    console.log('viewpopo');
  }
}
