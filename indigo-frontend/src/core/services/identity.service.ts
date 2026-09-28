import { inject, Injectable } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { CurrentUser } from '../types/entities/user.i';
import { ApiService } from '@core/services/api.service';
import { AuthenticatorService } from '@aws-amplify/ui-angular';

@Injectable({
  providedIn: 'root',
})
export class IdentityService {
  authenticatorService = inject(AuthenticatorService);

  public user$: Observable<CurrentUser>;

  constructor(private api: ApiService<unknown>) {
    this.user$ = this.api
      .request<CurrentUser>('get', 'currentUser')
      .pipe(shareReplay({ bufferSize: 1, refCount: false }));
  }

  async logout() {
    this.authenticatorService.signOut();
  }
}
