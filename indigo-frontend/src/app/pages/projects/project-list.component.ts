import {
  ButtonToggleComponent,
  ToggleOption,
} from '@/core/components/common/button-toggle/button-toggle.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ToggleComponent } from '@/core/components/common/toggle/toggle.component';
import { ProjectItemComponent } from '@/core/components/project/project-item/project-item.component';
import { getRandomStr } from '@/core/utils/string.util';
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
  imports: [
    CommonModule,
    FormsModule,
    CardComponent,
    ProjectItemComponent,
    ButtonToggleComponent,
    MatSlideToggleModule,
    ToggleComponent,
  ],
})
export class ProjectListComponent {
  selectedView = 'grid';

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
