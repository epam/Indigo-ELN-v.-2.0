import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { CurrentUser } from '../types/entities/user.i';
import { KeycloakService } from 'keycloak-angular';

@Injectable({
  providedIn: 'root',
})
export class UserKeycloakService {
  private keycloakService = new KeycloakService(); // Keycloak Service instance
  private userSubject = new BehaviorSubject<CurrentUser | null>(null);
  public user$: Observable<CurrentUser | null> = this.userSubject.asObservable();

  constructor() {
    this.initKeycloakUser();
  }

  private initKeycloakUser(): void {
    if (!this.keycloakService.isLoggedIn()) {
      this.userSubject.next(null);
      return;
    }

    this.keycloakService
      .loadUserProfile()
      .then((userProfile) => {
        const currentUser: CurrentUser = {
          id: userProfile.id || '',
          username: userProfile.username || '',
          displayName: `${userProfile.firstName || ''} ${userProfile.lastName || ''}`.trim(),
          permissions: [], // Add permissions if applicable
        };
        this.userSubject.next(currentUser);
      })
      .catch((error) => {
        console.error('Failed to load user profile from Keycloak', error);
        this.userSubject.next(null);
      });
  }

  logout(): void {
    this.keycloakService.logout(); // Implement Keycloak logout functionality
  }
}
