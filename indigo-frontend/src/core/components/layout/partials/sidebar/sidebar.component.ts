import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { StarredExperimentsComponent } from './starred-experiments/starred-experiments.component';
import { map, Observable } from 'rxjs';
import { IdentityService } from '@/core/services/identity.service';
import { ApplicationPermission, CurrentUser } from '@/core/types/entities/user.i';
import { MatIconModule } from '@angular/material/icon';

interface MenuItem {
  name: string;
  path: string;
  requiredPermission?: string;
  icon?: string;
  materialIcon?: string;
}

@Component({
  standalone: true,
  imports: [CommonModule, RouterModule, StarredExperimentsComponent, MatIconModule],
  selector: 'eln-sidebar',
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  private identityService = inject(IdentityService);
  private router = inject(Router);

  private fullMenu = [
    {
      name: 'All Projects',
      icon: 'indicon-briefcase',
      path: '/',
    },
    {
      name: 'Templates',
      icon: 'indicon-layers',
      path: '/templates',
      requiredPermission: ApplicationPermission.MANAGE_TEMPLATES,
    },
    {
      name: 'Dictionaries',
      materialIcon: 'import_contacts',
      path: '/dictionary',
      requiredPermission: ApplicationPermission.MANAGE_DICTIONARIES,
    },
  ];

  menu$: Observable<MenuItem[]> = this.identityService.user$.pipe(
    map((user: CurrentUser) => {
      return this.fullMenu.filter(
        (menuItem) => !menuItem.requiredPermission || user.permissions.includes(menuItem.requiredPermission),
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
