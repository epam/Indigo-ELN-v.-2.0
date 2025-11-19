import { UserService } from '@/core/services/user.service';
import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatRippleModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { Router, RouterOutlet } from '@angular/router';
import { AuthenticatorService } from '@aws-amplify/ui-angular';
import { Subject, takeUntil } from 'rxjs';
import { SidebarComponent } from './partials/sidebar/sidebar.component';
import { GlobalSearchComponent } from '@pages/search/sample-search/global-search.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'eln-master',
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
export class MasterComponent implements OnInit, OnDestroy {
  destroy$ = new Subject<void>();
  authenticatorService = inject(AuthenticatorService);
  userService = inject(UserService);
  dialog = inject(MatDialog);
  public isCollapsed = false;
  public searchControl = new FormControl('');
  router = inject(Router);

  userName = 'John D.';
  userAvatar = 'assets/avatar-placeholder.png';

  logout() {
    this.authenticatorService.signOut();
    this.router.navigateByUrl('/');
  }

  ngOnInit(): void {
    this.userService.user$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.userName = `${user.given_name} ${user.family_name}`;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  showSearch(): void {
    this.dialog.open(GlobalSearchComponent, {});
  }
}
