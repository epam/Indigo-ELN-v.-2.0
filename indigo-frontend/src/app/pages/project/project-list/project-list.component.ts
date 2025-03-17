import {
  ButtonToggleComponent,
  ToggleOption,
} from '@/core/components/common/button-toggle/button-toggle.component';
import { ToggleComponent } from '@/core/components/common/toggle/toggle.component';
import { ProjectItemComponent } from '@/core/components/project/project-item/project-item.component';
import { PaginatedComponent } from '@/core/components/util/paginated.component';
import { Project } from '@/core/types/entities/project.i';
import { getRandomStr } from '@/core/utils/string.util';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

const mock_users = [
  'assets/avatar1.png',
  'assets/avatar2.png',
  'assets/avatar3.png',
  '-',
  '-',
  '-',
  '-',
  '-',
  '-',
  '-',
  '-',
  '-',
  '-',
  '-',
  '-',
];

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
  ],
})
export class ProjectListComponent extends PaginatedComponent<Project> {
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

  projects = [
    { id: getRandomStr(), name: 'Project 1', description: 'Description 1' },
    { id: getRandomStr(), name: 'Project 2', description: 'Description 2' },
    { id: getRandomStr(), name: 'Project 3', description: 'Description 2' },
    { id: getRandomStr(), name: 'Project 4', description: 'Description 2' },
    { id: getRandomStr(), name: 'Project 5', description: 'Description 2' },
    { id: getRandomStr(), name: 'Project 6', description: 'Description 2' },
    { id: getRandomStr(), name: 'Project 7', description: 'Description 2' },
  ].map((project) => ({
    ...project,
    users: mock_users,
  }));
}
