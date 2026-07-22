import {HttpInterceptorFn, HttpErrorResponse} from '@angular/common/http';
import {inject} from '@angular/core';
import {catchError, throwError} from 'rxjs';
import {AuthService} from '../services/auth.service';
import {NotificationService} from '../services/notification.service';

function isPublicAuthRequest(url: string): boolean {
  return url.includes('/auth/login') || /\/users\/?$/.test(url);
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const notification = inject(NotificationService);
  const token = authService.getToken();

  const authReq = token ? req.clone({setHeaders: {Authorization: `Bearer ${token}`},}) : req;

  return next(authReq).pipe(catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isPublicAuthRequest(req.url)) {
        notification.error('Sua sessao expirou, entre novamente.');
        authService.logout();
      } else if (error.status === 403 && !isPublicAuthRequest(req.url)) {
        notification.error('Acesso Negado');
      }
      return throwError(() => error);
    })
  );
};
