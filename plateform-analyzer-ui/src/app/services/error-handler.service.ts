import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

export interface ApiError {
  code: string;
  message: string;
  details?: any;
  timestamp: string;
}

export interface ErrorResponse {
  error?: ApiError | string;
  message?: string;
  details?: any;
  status?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ErrorHandlerService {

  constructor() { }

  /**
   * Handle HTTP errors and return user-friendly messages
   */
  handleError(error: HttpErrorResponse | any): { message: string; type: 'error' | 'warning' | 'info'; details?: any } {
    if (error instanceof HttpErrorResponse) {
      return this.handleHttpError(error);
    } else {
      return this.handleClientError(error);
    }
  }

  /**
   * Handle HTTP errors from backend
   */
  private handleHttpError(error: HttpErrorResponse): { message: string; type: 'error' | 'warning' | 'info'; details?: any } {
    const status = error.status;
    const errorResponse = error.error as ErrorResponse;

    // Network/Connection errors
    if (status === 0) {
      return {
        message: 'Unable to connect to the server. Please check your internet connection.',
        type: 'error',
        details: { originalError: error.message }
      };
    }

    // Handle different HTTP status codes
    switch (status) {
      case 400:
        return this.handleBadRequest(errorResponse);
      case 401:
        return {
          message: 'Your session has expired. Please log in again.',
          type: 'warning'
        };
      case 403:
        return {
          message: 'You do not have permission to perform this action.',
          type: 'error'
        };
      case 404:
        return {
          message: 'The requested resource was not found.',
          type: 'error'
        };
      case 422:
        return this.handleValidationError(errorResponse);
      case 429:
        return {
          message: 'Too many requests. Please try again later.',
          type: 'warning'
        };
      case 500:
        return {
          message: 'An internal server error occurred. Please try again later.',
          type: 'error'
        };
      case 502:
      case 503:
      case 504:
        return {
          message: 'The service is temporarily unavailable. Please try again later.',
          type: 'warning'
        };
      default:
        return this.handleGenericError(errorResponse, status);
    }
  }

  /**
   * Handle 400 Bad Request errors
   */
  private handleBadRequest(errorResponse: ErrorResponse): { message: string; type: 'error' | 'warning' | 'info'; details?: any } {
    if (typeof errorResponse?.error === 'string') {
      return {
        message: errorResponse.error,
        type: 'error'
      };
    }

    if (errorResponse?.error?.message) {
      return {
        message: errorResponse.error.message,
        type: 'error',
        details: errorResponse.error.details
      };
    }

    if (errorResponse?.message) {
      return {
        message: errorResponse.message,
        type: 'error'
      };
    }

    return {
      message: 'Invalid request. Please check your input and try again.',
      type: 'error'
    };
  }

  /**
   * Handle 422 Validation errors
   */
  private handleValidationError(errorResponse: ErrorResponse): { message: string; type: 'error' | 'warning' | 'info'; details?: any } {
    if (errorResponse?.error && typeof errorResponse.error === 'object' && errorResponse.error.details && typeof errorResponse.error.details === 'object') {
      const validationErrors = errorResponse.error.details;
      const errorMessages = Object.values(validationErrors).flat();

      if (errorMessages.length > 0) {
        return {
          message: errorMessages[0] as string,
          type: 'error',
          details: validationErrors
        };
      }
    }

    return this.handleBadRequest(errorResponse);
  }

  /**
   * Handle generic HTTP errors
   */
  private handleGenericError(errorResponse: ErrorResponse, status: number): { message: string; type: 'error' | 'warning' | 'info'; details?: any } {
    if (errorResponse?.error && typeof errorResponse.error === 'object' && errorResponse.error.message) {
      return {
        message: errorResponse.error.message,
        type: 'error',
        details: errorResponse.error.details
      };
    }

    if (errorResponse?.message) {
      return {
        message: errorResponse.message,
        type: 'error'
      };
    }

    return {
      message: `Request failed with status ${status}. Please try again.`,
      type: 'error'
    };
  }

  /**
   * Handle client-side errors
   */
  private handleClientError(error: any): { message: string; type: 'error' | 'warning' | 'info'; details?: any } {
    console.error('Client error:', error);

    return {
      message: 'An unexpected error occurred. Please try again.',
      type: 'error',
      details: { originalError: error.message || error }
    };
  }

  /**
   * Get specific authentication error messages
   */
  getAuthErrorMessage(error: any): string {
    const handled = this.handleError(error);

    // Specific auth error handling
    if (error?.error?.error === 'invalid_grant') {
      return 'Invalid username or password. Please try again.';
    }

    if (error?.error?.error === 'invalid_client') {
      return 'Authentication service configuration error. Please contact support.';
    }

    if (error?.status === 401) {
      return 'Invalid credentials. Please check your username and password.';
    }

    return handled.message;
  }

  /**
   * Check if error is authentication related
   */
  isAuthError(error: any): boolean {
    if (error?.status === 401) return true;
    if (error?.error?.error === 'invalid_grant') return true;
    if (error?.error?.error === 'invalid_client') return true;
    if (error?.message?.includes('authentication')) return true;
    if (error?.message?.includes('credentials')) return true;

    return false;
  }

  /**
   * Check if error is network related
   */
  isNetworkError(error: any): boolean {
    return error?.status === 0 || error?.message?.includes('network') || error?.message?.includes('connection');
  }
}
