import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatRippleModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { Router, RouterOutlet } from '@angular/router';
import { AuthenticatorService } from '@aws-amplify/ui-angular';
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
})
export class MasterComponent {
  authenticatorService = inject(AuthenticatorService);
  public isCollapsed = false;
  public searchControl = new FormControl('');
  router = inject(Router);

  userName = 'John D.';
  userAvatar = 'assets/avatar-placeholder.png';

  logout() {
    this.authenticatorService.signOut();
    this.router.navigateByUrl('/');
  }
}
