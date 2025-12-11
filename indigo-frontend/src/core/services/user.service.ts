import { Injectable } from '@angular/core';
import { fetchAuthSession } from 'aws-amplify/auth';
import { from, map, Observable, switchMap } from 'rxjs';
import { ElnJwtPayload } from '../types/jwt-payload.i';
import { ApiService } from './api.service';
import { HttpParams } from '@angular/common/http';
import { CurrentUser, Role, UsersResponse } from '../types/entities/user.i';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  public user$: Observable<CurrentUser>;

  constructor(private api: ApiService<unknown>) {
    this.user$ = this.api.request<CurrentUser>('get', 'currentUser');
  }
}
