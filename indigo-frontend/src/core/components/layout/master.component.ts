import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatRippleModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './partials/sidebar/sidebar.component';

@Component({
  selector: 'app-master',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    ReactiveFormsModule,
    MatSidenavModule,
    MatIconModule,
    MatListModule,
    MatButtonModule,
    MatMenuModule,
    MatRippleModule,
    SidebarComponent,
  ],
  templateUrl: './master.component.html',
  styleUrls: ['./master.component.scss'],
})
export class MasterComponent {
  isCollapsed = false;
  searchControl = new FormControl('');
  userName = 'John D.';
  userAvatar = 'assets/avatar-placeholder.png';
}
