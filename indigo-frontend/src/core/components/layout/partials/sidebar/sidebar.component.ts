import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { StarredExperimentsComponent } from './starred-experiments/starred-experiments.component';
import { map, Observable } from 'rxjs';
import { UserService } from '@/core/services/user.service';
import { Role } from '@/core/types/entities/user.i';
import { MatIconModule } from '@angular/material/icon';

interface MenuItem {
  name: string;
  path: string;
  requiredRole?: string;
  icon?: string;
  materialIcon?: string;
}

@Component({
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    StarredExperimentsComponent,
    MatIconModule,
  ],
  selector: 'eln-sidebar',
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  private userService = inject(UserService);
  private router = inject(Router);

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
      materialIcon: 'import_contacts',
      path: '/dictionary',
      requiredRole: 'Dictionary editor',
    },
  ];

  menu$: Observable<MenuItem[]> = this.userService.userRoles$.pipe(
    map((roles: Role[]) => {
      const roleNames = roles.map((role) => role.name);
      return this.fullMenu.filter(
        (menuItem) =>
          !menuItem.requiredRole || roleNames.includes(menuItem.requiredRole),
      );
    }),
  );

  isSidebarOpen = true;
  isHovered = false;

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
    this.isHovered = false;
  }

  isMenuItemActive(path: string) {
    const currentPath = this.router.url.split('?')[0];
    const isProjectPathActive = true;

    if (path === '/' && currentPath === '/projects') {
      return isProjectPathActive;
    }

    return currentPath === path;
  }
}
