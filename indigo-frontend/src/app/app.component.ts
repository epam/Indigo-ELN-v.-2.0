import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<router-outlet />',
  standalone: true,
})
export class AppComponent {
  title = 'indigo-frontend';
  private oidcSecurityService = inject(OidcSecurityService);

  ngOnInit() {
    this.oidcSecurityService.checkAuth().subscribe();
  }
}
