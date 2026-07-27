import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { filter } from 'rxjs';

@Component({
  selector: 'eln-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.local.html',
  standalone: true,
})
export class AppComponent implements OnInit {
  title = 'indigo-frontend';

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private titleService: Title,
  ) {}

  /**
   * Initialize Keycloak authentication when the component is loaded.
   */
  async ngOnInit(): Promise<void> {
    this.setPageTitleOnNavigation();
  }

  private setPageTitleOnNavigation(): void {
    this.router.events.pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd)).subscribe(() => {
      const title = this.getDeepestRouteTitle(this.activatedRoute) ?? 'IndigoELN';
      this.titleService.setTitle(title);
    });
  }

  private getDeepestRouteTitle(route: ActivatedRoute): string | null {
    let title = route.snapshot.data?.['title'] ?? null;
    while (route.firstChild) {
      route = route.firstChild;
      title = route.snapshot.data?.['title'] ?? title;
    }
    return title;
  }
}
