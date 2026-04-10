import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CurrentUser } from '../types/entities/user.i';
import { environment } from '../../environments/environment'

import { UserService } from '@/core/services/user.service';
import { UserKeycloakService } from './user-keycloak.service';

@Injectable({
  providedIn: 'root',
})
export class IdentityService {
  private identityService: UserService | UserKeycloakService;

  public user$: Observable<CurrentUser>;

  constructor(
    private userService: UserService,
    private userKeycloakService: UserKeycloakService
  ) {
    if (environment.authProvider == 'keycloak') {
      this.identityService = this.userKeycloakService;
    } else {
      this.identityService = this.userService;
    }

    this.user$ = this.identityService.user$;
  }

  logout(): void {
    if (this.identityService instanceof UserKeycloakService) {
      this.identityService.logout(); // Logout for Keycloak
    } else {
    }
  }
}
