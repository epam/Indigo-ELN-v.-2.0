import { CardComponent } from '@/core/components/common/card/card.component';
import { ProjectItemComponent } from '@/core/components/project/project-item/project-item.component';
import { getRandomStr } from '@/core/utils/string.util';
import { Component } from '@angular/core';

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
  imports: [CardComponent, ProjectItemComponent],
})
export class ProjectListComponent {
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
