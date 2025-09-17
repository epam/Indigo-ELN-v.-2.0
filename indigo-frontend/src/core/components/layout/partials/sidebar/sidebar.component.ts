import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { StarredExperimentsComponent } from './starred-experiments/starred-experiments.component';
import { map, Observable } from 'rxjs';
import { UserService } from '@/core/services/user.service';
import { Role } from '@/core/types/entities/user.i';

@Component({
  standalone: true,
  imports: [CommonModule, RouterModule, StarredExperimentsComponent],
  selector: 'eln-sidebar',
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  private fullMenu = [
    {
      name: 'Projects',
      icon: 'indicon-briefcase',
      path: '/',
    },
    {
      name: 'Templates',
      icon: 'indicon-layers',
      path: '/templates',
    },
    {
      name: 'Dictionary',
      icon: 'indicon-book',
      path: '/dictionary',
      requiredRole: 'Dictionary editor',
    },
  ];

  menu$: Observable<
    { name: string; icon: string; path: string; requiredRole?: string }[]
  >;

  isSidebarOpen = true;
  isHovered = false;

  constructor(private userService: UserService) {
    this.menu$ = this.userService.userRoles$.pipe(
      map((roles: Role[]) => {
        const roleNames = roles.map((role) => role.name);
        return this.fullMenu.filter(
          (menuItem) =>
            !menuItem.requiredRole || roleNames.includes(menuItem.requiredRole)
        );
      })
    );
  }

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
    this.isHovered = false;
  }
}
