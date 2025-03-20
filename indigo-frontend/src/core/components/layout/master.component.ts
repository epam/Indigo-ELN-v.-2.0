import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatRippleModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterOutlet } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { take } from 'rxjs';
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
  protected authService = inject(OidcSecurityService);
  public isCollapsed = false;
  public searchControl = new FormControl('');
  userName = 'John D.';
  userAvatar = 'assets/avatar-placeholder.png';

  logout() {
    this.authService.logoff().pipe(take(1)).subscribe();
  }
}
