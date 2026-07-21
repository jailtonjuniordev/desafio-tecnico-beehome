import {HttpInterceptorFn, HttpErrorResponse} from '@angular/common/http';
import {inject} from '@angular/core';
import {catchError, throwError} from 'rxjs';
import {AuthService} from '../services/auth.service';

function isPublicAuthRequest(url: string): boolean {
  return url.includes('/auth/login') || /\/users\/?$/.test(url);
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const authReq = token ? req.clone({
    setHeaders: {Authorization: `Bearer ${token}`},
  }) : req;

  return next(authReq).pipe(catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isPublicAuthRequest(req.url)) {
        authService.logout();
      }
      return throwError(() => error);
    })
  );
};
