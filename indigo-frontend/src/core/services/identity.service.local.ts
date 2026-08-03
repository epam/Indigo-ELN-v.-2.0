import { Injectable } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { CurrentUser } from '../types/entities/user.i';
import { ApiService } from '@core/services/api.service';
import Keycloak from 'keycloak-js';

@Injectable({
  providedIn: 'root',
})
export class IdentityService {
  public user$: Observable<CurrentUser>;

  constructor(
    private api: ApiService<unknown>,
    private keycloak: Keycloak,
  ) {
    this.user$ = this.api
      .request<CurrentUser>('get', 'currentUser')
      .pipe(shareReplay({ bufferSize: 1, refCount: false }));
  }

  async logout() {
    await this.keycloak.logout({ redirectUri: `${window.location.origin}/` });
  }
}
