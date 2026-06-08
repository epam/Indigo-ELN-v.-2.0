import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { StarredExperimentsComponent } from './starred-experiments/starred-experiments.component';
import { IdentityService } from '@/core/services/identity.service';
import { ApplicationPermission } from '@/core/types/entities/user.i';
import { MatIconModule } from '@angular/material/icon';
import { SvgIconComponent } from '@core/components/common/svg-icon/svg-icon.component';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

interface MenuItem {
  name: string;
  path: string;
  requiredPermission?: string;
  icon?: string;
  materialIcon?: string;
}

@Component({
  standalone: true,
  imports: [CommonModule, RouterModule, StarredExperimentsComponent, MatIconModule, SvgIconComponent],
  selector: 'eln-sidebar',
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent implements OnInit {
  private identityService = inject(IdentityService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

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
      icon: 'dictionaries',
      path: '/dictionary',
    },
    {
      name: 'Signatures',
      icon: 'indicon-layers', // TODO icon
      path: '/signatures',
    },
  ];

  menu = signal<MenuItem[]>([]);

  isSidebarOpen = true;
  isHovered = false;

  ngOnInit() {
    this.menu.set(this.fullMenu.filter((x) => !x.requiredPermission));
    this.identityService.user$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((user) => {
      this.menu.set(
        this.fullMenu.filter((x) => !x.requiredPermission || user.permissions.includes(x.requiredPermission)),
      );
    });
  }

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
