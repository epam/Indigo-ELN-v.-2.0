import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  standalone: true,
  imports: [CommonModule, RouterModule],
  selector: 'app-sidebar',
  template: `<nav id="sidebar" [class.collapsed]="!isSidebarOpen" class="m-5">
    <ul>
      <li class="mt-4 mb-5">
        <a (click)="toggleSidebar()">
          <em
            [class.indicon-sidebar_open]="isSidebarOpen"
            [class.indicon-sidebar_close]="!isSidebarOpen"
          ></em>
          <span class="font-semibold text-h4">Menu</span>
        </a>
      </li>
      <li
        *ngFor="let item of menu"
        [routerLinkActive]="['active']"
        class="my-5"
      >
        <a [routerLink]="item.path">
          <em [class]="item.icon"></em>
          <span class="font-semibold text-h4">{{ item.name }}</span>
        </a>
      </li>
    </ul>
  </nav>`,
  styleUrls: ['./sidebar.component.scss'],
})
export class SidebarComponent {
  menu = [
    {
      name: 'All Projects',
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
      icon: 'indicon-terminal',
      path: '/dictionary',
    },
  ];
  isSidebarOpen = true;
  isHovered = false;

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
    this.isHovered = false;
  }
}
