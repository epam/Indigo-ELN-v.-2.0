import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { fetchAuthSession } from '@aws-amplify/auth';
import { from, Observable, switchMap } from 'rxjs';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  intercept(
    req: HttpRequest<unknown>,
    next: HttpHandler,
  ): Observable<HttpEvent<unknown>> {
    return from(fetchAuthSession()).pipe(
      switchMap((session) => {
        if (session.tokens?.accessToken) {
          req = req.clone({
            headers: req.headers.set(
              'Authorization',
              `Bearer ${session.tokens.accessToken.toString()}`,
            ),
          });
        }
        return next.handle(req);
      }),
    );
  }
}
