import {HttpInterceptorFn, HttpErrorResponse} from '@angular/common/http';
import {inject} from '@angular/core';
import {catchError, throwError} from 'rxjs';
import {translateApiErrorMessage} from '../constants/api-error-messages';
import {AuthService} from '../services/auth.service';
import {NotificationService} from '../services/notification.service';

function isPublicAuthRequest(url: string): boolean {
  return url.includes('/auth/login') || /\/users\/?$/.test(url);
}

function resolveInterceptorMessage(
  error: HttpErrorResponse,
  fallbackKey: string,
): string {
  if (typeof error.error?.message === 'string') {
    return translateApiErrorMessage(error.error.message);
  }
  return translateApiErrorMessage(fallbackKey);
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const notification = inject(NotificationService);
  const token = authService.getToken();

  const authReq = token ? req.clone({setHeaders: {Authorization: `Bearer ${token}`},}) : req;

  return next(authReq).pipe(catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isPublicAuthRequest(req.url)) {
        notification.error(resolveInterceptorMessage(error, 'Authentication required'),);
        authService.logout();
      } else if (error.status === 403 && !isPublicAuthRequest(req.url)) {
        notification.error(resolveInterceptorMessage(error, 'Access denied'));
      }
      return throwError(() => error);
    })
  );
};
