import { IdentityService } from '@/core/services/identity.service';
import { CommonModule } from '@angular/common';
import { Component, ElementRef, inject, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatRippleModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { SlideInPanelService } from '@core/components/common/slide-in-panel/slide-in-panel.service';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { ReportErrorDialogService } from '@core/services/report-error-dialog.service';
import { GlobalSearchComponent } from '@pages/search/global-search/global-search.component';
import { Observable, Subject, takeUntil } from 'rxjs';
import { filter } from 'rxjs/operators';
import { SidebarComponent } from './partials/sidebar/sidebar.component';

@Component({
  selector: 'eln-master',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
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
  identityService = inject(IdentityService);
  slideInPanelService = inject(SlideInPanelService);
  reportErrorDialogService = inject(ReportErrorDialogService);
  public isCollapsed = false;
  public searchControl = new FormControl('');
  router = inject(Router);

  userName = '';
  userAvatar = 'assets/avatar-placeholder.png';

  @ViewChild('content', { static: true }) content!: ElementRef<HTMLElement>;
  @ViewChild('searchHeader') searchHeader: TemplateRef<any>;

  async logout() {
    await this.identityService.logout();
    await this.router.navigateByUrl('/');
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
    this.slideInPanelService.open(GlobalSearchComponent, {
      header: this.searchHeader,
      inputs: { initialQuery: this.searchControl.value },
    });
    this.searchControl.reset();
  }

  openReportError(): void {
    this.reportErrorDialogService.open();
  }
}
