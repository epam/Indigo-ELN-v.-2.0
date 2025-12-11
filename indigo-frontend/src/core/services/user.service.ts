import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { CurrentUser } from '../types/entities/user.i';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  public user$: Observable<CurrentUser>;

  constructor(private api: ApiService<unknown>) {
    this.user$ = this.api.request<CurrentUser>('get', 'currentUser');
  }
}
