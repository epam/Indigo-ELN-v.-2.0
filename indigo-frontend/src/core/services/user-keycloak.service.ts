import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of, switchMap, map } from 'rxjs';
import { ElnJwtPayload } from '../types/jwt-payload.i';
import { ApiService } from './api.service';
import { HttpParams } from '@angular/common/http';
import { Role, UsersResponse } from '../types/entities/user.i';
import { KeycloakService } from 'keycloak-angular';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private keycloakService = new KeycloakService(); // Keycloak Service instance
  private userSubject = new BehaviorSubject<ElnJwtPayload | null>(null);
  public user$: Observable<ElnJwtPayload> = this.userSubject.asObservable();

  constructor(private api: ApiService<unknown>) {
    this.initKeycloakUser();
  }

  private async initKeycloakUser(): Promise<void> {
      // `isLoggedIn()` returns a boolean directly
      const authenticated: boolean = this.keycloakService.isLoggedIn();

      if (authenticated) {
        try {
          const userProfile = await this.keycloakService.loadUserProfile();
          const userPayload: ElnJwtPayload = {
            family_name: userProfile.lastName || '',
            given_name: userProfile.firstName || '',
            payload: {
              iss: 'keycloak',
              sub: userProfile.id || '',
              aud: [userProfile.username || ''],
              exp: Math.floor(Date.now() / 1000) + 3600, // Set expiration to 1 hour from now
              iat: Math.floor(Date.now() / 1000), // Issued at current time
            },
          };
          this.userSubject.next(userPayload);
        } catch (error) {
          console.error('Failed to load user profile from Keycloak', error);
          this.userSubject.next(null);
        }
      } else {
        this.userSubject.next(null);
      }
    }

  public userRoles$: Observable<Role[]> = this.user$.pipe(
    map((jwtPayload) => jwtPayload['cognito:username']),
    switchMap((username) =>
      this.getUsers(username).pipe(
        map((usersResponse: UsersResponse) => {
          const user = usersResponse.items?.find((item) => item.username === username);
          return user?.roles || [];
        })
      )
    )
  );

  getUsers(username?: string): Observable<UsersResponse> {
    let params = new HttpParams();
    if (username) {
      params = params.set('username', username);
    }

    const options = { params };

    return this.api.request<UsersResponse>('get', 'users', null, options);
  }
}
