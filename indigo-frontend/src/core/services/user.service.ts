import { Injectable } from '@angular/core';
import { fetchAuthSession } from 'aws-amplify/auth';
import { from, map } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  public user$ = from(fetchAuthSession()).pipe(
    map((session) => session.tokens.accessToken.payload),
  );
}
