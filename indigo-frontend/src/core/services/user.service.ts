import { Injectable } from '@angular/core';
import { fetchAuthSession } from 'aws-amplify/auth';
import { from, map, Observable, switchMap } from 'rxjs';
import { ElnJwtPayload } from '../types/jwt-payload.i';
import { ApiService } from './api.service';
import { HttpParams } from '@angular/common/http';
import { Role, UsersResponse } from '../types/entities/user.i';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private api: ApiService<unknown>) { }
  
  public user$: Observable<ElnJwtPayload> = from(fetchAuthSession()).pipe(
    map(
      (session) => session.tokens.idToken.payload as unknown as ElnJwtPayload,
    ),
  );

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
