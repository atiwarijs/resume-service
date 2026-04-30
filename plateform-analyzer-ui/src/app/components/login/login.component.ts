import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { APP_CONSTANTS } from '../../constants/app.constants';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email: string = 'ashish';
  password: string = 'ashish@123';
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  onLogin(): void {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please enter both username and password';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.email, this.password).subscribe({
      next: (response) => {
        this.isLoading = false;
        console.log('Login response:', response);
        // Check for token in various property names
        const hasToken = response && (response.accessToken || response.access_token || response.token);
        if (hasToken) {
          console.log('Token found, navigating to dashboard...');
          this.navigateToRedirectUrl();
        } else {
          console.error('No token in response');
          this.errorMessage = 'Login failed. Please check your credentials.';
        }
      },
      error: (error: any) => {
        this.isLoading = false;
        console.error('Login error:', error);
        this.errorMessage = error.error?.message || 'Login failed. Please try again.';
      }
    });
  }

  private navigateToRedirectUrl(): void {
    const redirectUrl = localStorage.getItem('redirectUrl') || APP_CONSTANTS.ROUTES.DASHBOARD;
    localStorage.removeItem('redirectUrl');
    this.router.navigate([redirectUrl]);
  }
}
