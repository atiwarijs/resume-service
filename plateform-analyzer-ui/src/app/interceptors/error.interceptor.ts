import {
  HttpRequest,
  HttpHandlerFn,
  HttpEvent,
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { inject } from '@angular/core';
import { ErrorHandlerService } from '../services/error-handler.service';

export const errorInterceptor: HttpInterceptorFn = (request: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> => {
  const errorHandler = inject(ErrorHandlerService);
  
  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      // Don't handle errors for requests that should be handled manually
      if (shouldSkipErrorHandling(request)) {
        return throwError(() => error);
      }

      // Handle the error using the error handler service
      const errorResponse = errorHandler.handleError(error);

      // Log the error for debugging
      console.error('HTTP Error intercepted:', {
        url: request.url,
        method: request.method,
        status: error.status,
        message: errorResponse.message,
        type: errorResponse.type,
        timestamp: new Date().toISOString()
      });

      // Return a formatted error object
      return throwError(() => ({
        error: true,
        message: errorResponse.message,
        type: errorResponse.type,
        status: error.status,
        details: errorResponse.details,
        originalError: error,
        timestamp: new Date().toISOString()
      }));
    })
  );
};

/**
 * Determine if error handling should be skipped for certain requests
 */
function shouldSkipErrorHandling(request: HttpRequest<unknown>): boolean {
  // Skip error handling for authentication endpoints as they are handled manually
  const authEndpoints = [
    '/auth/login',
    '/auth/register-user',
    '/auth/refresh-token'
  ];

  return authEndpoints.some(endpoint => request.url.includes(endpoint));
}
