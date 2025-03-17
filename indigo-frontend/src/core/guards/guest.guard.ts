import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { map, tap } from 'rxjs';

export const GuestGuard: CanActivateFn = () => {
  const router = inject(Router);
  const authService = inject(OidcSecurityService);

  return authService.isAuthenticated().pipe(
    tap((isAuthenticated) => isAuthenticated && router.navigate(['/'])),
    map((val) => !val),
  );
};
