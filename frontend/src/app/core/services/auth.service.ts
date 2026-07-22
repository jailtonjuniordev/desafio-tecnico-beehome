import { Injectable, computed, inject, signal, effect, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, exhaustMap } from 'rxjs';

import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  AuthUser,
  LoginRequest,
  RegisterRequest,
  UpdateProfileRequest,
  UserProfile,
} from '../../features/auth/models/auth.model';

const TOKEN_KEY = 'beehome_access_token';
const USER_KEY = 'beehome_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly authUrl = `${environment.apiUrl}/auth`;
  private readonly usersUrl = `${environment.apiUrl}/users`;
  private readonly tokenSignal = signal<string | null>(null);
  private readonly userSignal = signal<AuthUser | null>(null);
  readonly currentUser = this.userSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.tokenSignal());

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      this.tokenSignal.set(this.readStoredToken());
      this.userSignal.set(this.readStoredUser());
    }

    effect(() => {
      if (!isPlatformBrowser(this.platformId)) return;

      const token = this.tokenSignal();
      const user = this.userSignal();

      if (token) {
        localStorage.setItem(TOKEN_KEY, token);
      } else {
        localStorage.removeItem(TOKEN_KEY);
      }

      if (user) {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
      } else {
        localStorage.removeItem(USER_KEY);
      }
    });
  }

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.authUrl}/login`, payload)
      .pipe(
        tap((response) => {
          if (!response?.token) {
            throw new Error('Token ausente na resposta de autenticacao.');
          }

          this.tokenSignal.set(response.token);

          this.userSignal.set({
            email: payload.email,
            username: payload.email
          });
        })
      );
  }

  register(payload: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<unknown>(this.usersUrl, payload).pipe(
      exhaustMap(() =>
        this.login({ email: payload.email, password: payload.password })
      ),
      tap(() => {
        this.userSignal.update((currentUser) =>
          currentUser ? { ...currentUser, username: payload.username } : null
        );
      })
    );
  }

  getMe(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.usersUrl}/me`).pipe(
      tap((profile) => {
        this.userSignal.update((currentUser) =>
          currentUser ? { ...currentUser, ...profile } : null
        );
      })
    );
  }

  updateMe(payload: UpdateProfileRequest): Observable<UserProfile> {
    return this.http
      .put<UserProfile>(`${this.usersUrl}/me`, payload)
      .pipe(
        tap((profile) => {
          this.userSignal.update((currentUser) =>
            currentUser ? { ...currentUser, ...profile } : null
          );
        })
      );
  }

  logout(): void {
    this.tokenSignal.set(null);
    this.userSignal.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.tokenSignal();
  }

  private readStoredToken(): string | null {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token || token === 'undefined' || token === 'null') {
      localStorage.removeItem(TOKEN_KEY);
      return null;
    }
    return token;
  }

  private readStoredUser(): AuthUser | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as AuthUser;
    } catch {
      localStorage.removeItem(USER_KEY);
      return null;
    }
  }
}
