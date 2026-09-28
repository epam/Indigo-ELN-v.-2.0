import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { AmplifyAuthenticatorModule } from '@aws-amplify/ui-angular';
import { defaultStorage, sessionStorage } from 'aws-amplify/utils';
import { cognitoUserPoolsTokenProvider } from 'aws-amplify/auth/cognito';
import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { filter } from 'rxjs';

@Component({
  selector: 'eln-root',
  imports: [RouterOutlet, AmplifyAuthenticatorModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
})
export class AppComponent implements OnInit {
  title = 'indigo-frontend';
  isRemembered = false;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private titleService: Title,
  ) {}

  ngOnInit() {
    const hasLocalStorage = Object.keys(localStorage).some((key) => key.startsWith('CognitoIdentityServiceProvider'));

    if (hasLocalStorage) {
      this.isRemembered = true;
      cognitoUserPoolsTokenProvider.setKeyValueStorage(defaultStorage);
    } else {
      this.isRemembered = false;
      cognitoUserPoolsTokenProvider.setKeyValueStorage(sessionStorage);
    }

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

  onRememberMeChange(event: any) {
    const isChecked = (event.target as HTMLInputElement).checked;
    this.isRemembered = isChecked;
    if (isChecked) {
      cognitoUserPoolsTokenProvider.setKeyValueStorage(defaultStorage);
    } else {
      cognitoUserPoolsTokenProvider.setKeyValueStorage(sessionStorage);
    }
  }
}
