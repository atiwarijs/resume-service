import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, catchError, of, throwError } from 'rxjs';
import { APP_CONSTANTS } from '../constants/app.constants';
import { ErrorHandlerService } from './error-handler.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isLoggedInSubject = new BehaviorSubject<boolean>(this.checkInitialLoginState());
  isLoggedIn$ = this.isLoggedInSubject.asObservable();

  constructor(
    private http: HttpClient,
    private errorHandler: ErrorHandlerService
  ) {}

  private checkInitialLoginState(): boolean {
    return localStorage.getItem(APP_CONSTANTS.STORAGE.IS_LOGGED_IN) === 'true';
  }

  // Login with username and password
  login(username: string, password: string): Observable<any> {
    const body = { username, password };
    return this.http.post<any>(`${APP_CONSTANTS.API.BASE_URL}${APP_CONSTANTS.API.AUTH.LOGIN}`, body).pipe(
      tap(response => {
        this.handleAuthSuccess(response);
      }),
      catchError(error => {
        const errorMessage = this.errorHandler.getAuthErrorMessage(error);
        console.error('Login error:', error);
        return throwError(() => ({
          error: true,
          message: errorMessage,
          originalError: error
        }));
      })
    );
  }

  // Register new user
  register(userData: any): Observable<any> {
    return this.http.post<any>(`${APP_CONSTANTS.API.BASE_URL}${APP_CONSTANTS.API.AUTH.REGISTER}`, userData).pipe(
      tap(response => {
        // Registration doesn't automatically log in, so don't call handleAuthSuccess
        console.log('User registered successfully');
      }),
      catchError(error => {
        const errorResponse = this.errorHandler.handleError(error);
        console.error('Registration error:', error);
        return throwError(() => ({
          error: true,
          message: errorResponse.message,
          type: errorResponse.type,
          originalError: error
        }));
      })
    );
  }

  // Logout
  logout(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.post<any>(`${APP_CONSTANTS.API.BASE_URL}${APP_CONSTANTS.API.AUTH.LOGOUT}`, {}, { headers }).pipe(
      tap(() => {
        this.clearAuthData();
      }),
      catchError(error => {
        this.clearAuthData();
        const errorResponse = this.errorHandler.handleError(error);
        console.error('Logout error:', error);
        return of(null); // Logout should always succeed on client side
      })
    );
  }

  // Refresh access token
  refreshToken(): Observable<any> {
    const refreshToken = localStorage.getItem(APP_CONSTANTS.STORAGE.REFRESH_TOKEN);
    if (!refreshToken) {
      this.logout();
      return of(null);
    }

    const headers = new HttpHeaders({
      'refresh_token': refreshToken
    });
    return this.http.post<any>(`${APP_CONSTANTS.API.BASE_URL}${APP_CONSTANTS.API.AUTH.REFRESH_TOKEN}`, {}, { headers }).pipe(
      tap(response => {
        if (response.accessToken) {
          localStorage.setItem(APP_CONSTANTS.STORAGE.ACCESS_TOKEN, response.accessToken);
        }
      }),
      catchError(error => {
        const errorResponse = this.errorHandler.handleError(error);
        console.error('Token refresh error:', error);
        this.logout();
        return of(null);
      })
    );
  }

  // Get user profile
  getUserProfile(username: string, email: string): Observable<any> {
    const headers = this.getAuthHeaders();
    const url = `${APP_CONSTANTS.API.BASE_URL}${APP_CONSTANTS.API.USER.USER_INFO}
      .replace('{username}', username)
      .replace('{email}', email)`;
    return this.http.get<any>(url, { headers }).pipe(
      catchError(error => {
        const errorResponse = this.errorHandler.handleError(error);
        console.error('Get user profile error:', error);
        return throwError(() => ({
          error: true,
          message: errorResponse.message,
          type: errorResponse.type,
          originalError: error
        }));
      })
    );
  }

  // Update user profile
  updateProfile(updates: any): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put<any>(`${APP_CONSTANTS.API.BASE_URL}${APP_CONSTANTS.API.AUTH.UPDATE_USER}`, updates, { headers }).pipe(
      catchError(error => {
        const errorResponse = this.errorHandler.handleError(error);
        console.error('Update profile error:', error);
        return throwError(() => ({
          error: true,
          message: errorResponse.message,
          type: errorResponse.type,
          originalError: error
        }));
      })
    );
  }

  // Check if email exists
  checkEmailExists(email: string): Observable<boolean> {
    const url = `${APP_CONSTANTS.API.BASE_URL}${APP_CONSTANTS.API.USER.EMAIL_CHECK}`
      .replace('{email}', email);
    return this.http.get<boolean>(url).pipe(
      catchError(error => {
        const errorResponse = this.errorHandler.handleError(error);
        console.error('Check email error:', error);
        return throwError(() => ({
          error: true,
          message: errorResponse.message,
          type: errorResponse.type,
          originalError: error
        }));
      })
    );
  }

  // Update password
  updatePassword(passwordData: any): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.post<any>(`${APP_CONSTANTS.API.BASE_URL}${APP_CONSTANTS.API.AUTH.UPDATE_PASSWORD}`, passwordData, { headers }).pipe(
      catchError(error => {
        const errorResponse = this.errorHandler.handleError(error);
        console.error('Update password error:', error);
        return throwError(() => ({
          error: true,
          message: errorResponse.message,
          type: errorResponse.type,
          originalError: error
        }));
      })
    );
  }

  // Helper methods
  private handleAuthSuccess(response: any): void {
    // Handle different token property names
    const accessToken = response.accessToken || response.access_token || response.token;
    const refreshToken = response.refreshToken || response.refresh_token;
    
    if (accessToken) {
      localStorage.setItem(APP_CONSTANTS.STORAGE.ACCESS_TOKEN, accessToken);
    }
    if (refreshToken) {
      localStorage.setItem(APP_CONSTANTS.STORAGE.REFRESH_TOKEN, refreshToken);
    }
    if (response.user) {
      localStorage.setItem(APP_CONSTANTS.STORAGE.USER_DATA, JSON.stringify(response.user));
    }
    localStorage.setItem(APP_CONSTANTS.STORAGE.IS_LOGGED_IN, 'true');
    this.isLoggedInSubject.next(true);
    console.log('Auth success - user authenticated');
  }

  private clearAuthData(): void {
    localStorage.removeItem(APP_CONSTANTS.STORAGE.ACCESS_TOKEN);
    localStorage.removeItem(APP_CONSTANTS.STORAGE.REFRESH_TOKEN);
    localStorage.removeItem(APP_CONSTANTS.STORAGE.USER_DATA);
    localStorage.removeItem(APP_CONSTANTS.STORAGE.IS_LOGGED_IN);
    this.isLoggedInSubject.next(false);
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem(APP_CONSTANTS.STORAGE.ACCESS_TOKEN);
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // Public methods to check auth state
  isAuthenticated(): boolean {
    return this.isLoggedInSubject.value;
  }

  getAccessToken(): string | null {
    return localStorage.getItem(APP_CONSTANTS.STORAGE.ACCESS_TOKEN);
  }

  getUserData(): any {
    const userData = localStorage.getItem(APP_CONSTANTS.STORAGE.USER_DATA);
    return userData ? JSON.parse(userData) : null;
  }
}
