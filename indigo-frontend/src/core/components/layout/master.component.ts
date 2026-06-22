import { IdentityService } from '@/core/services/identity.service';
import { CommonModule } from '@angular/common';
import { Component, ElementRef, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatRippleModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { AuthenticatorService } from '@aws-amplify/ui-angular';
import { Observable, Subject, takeUntil } from 'rxjs';
import { filter } from 'rxjs/operators';
import { SidebarComponent } from './partials/sidebar/sidebar.component';
import { GlobalSearchComponent } from '@pages/search/global-search/global-search.component';
import { MatDialog } from '@angular/material/dialog';
import { ReportErrorDialogService } from '@core/services/report-error-dialog.service';

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
    FormsModule,
  ],
  templateUrl: './master.component.html',
})
export class MasterComponent implements OnInit, OnDestroy {
  destroy$ = new Subject<void>();
  authenticatorService = inject(AuthenticatorService);
  identityService = inject(IdentityService);
  dialog = inject(MatDialog);
  reportErrorDialogService = inject(ReportErrorDialogService);
  public isCollapsed = false;
  public searchControl = new FormControl('');
  router = inject(Router);

  userName = 'John D.';
  userAvatar = 'assets/avatar-placeholder.png';

  @ViewChild('content', { static: true }) content!: ElementRef<HTMLElement>;

  logout() {
    this.authenticatorService.signOut();
    this.router.navigateByUrl('/');
  }

  ngOnInit(): void {
    this.identityService.user$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.userName = `${user.displayName}`;
    });

    (this.router.events as Observable<NavigationEnd>)
      .pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntil(this.destroy$),
      )
      .subscribe(() => {
        this.content.nativeElement.scrollTop = 0;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  showSearch(): void {
    this.dialog.open(GlobalSearchComponent, { data: { initialQuery: this.searchControl.value } });
    this.searchControl.reset();
  }

  openReportError(): void {
    this.reportErrorDialogService.openBlank();
  }
}
