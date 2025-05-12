import { Injectable } from '@angular/core';
import { fetchAuthSession } from 'aws-amplify/auth';
import { from, map, Observable } from 'rxjs';
import { ElnJwtPayload } from '../types/jwt-payload.i';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  public user$: Observable<ElnJwtPayload> = from(fetchAuthSession()).pipe(
    map(
      (session) => session.tokens.idToken.payload as unknown as ElnJwtPayload,
    ),
  );
}
