import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router,
  UrlTree
} from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { APP_CONSTANTS } from '../constants/app.constants';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const isAuthenticated = this.authService.isAuthenticated();
    console.log('AuthGuard - isAuthenticated:', isAuthenticated, 'route:', state.url);
    
    if (isAuthenticated) {
      return true;
    }

    // Store the attempted URL for redirecting after login
    localStorage.setItem('redirectUrl', state.url);
    
    // Redirect to login page
    return this.router.createUrlTree([APP_CONSTANTS.ROUTES.LOGIN]);
  }
}
