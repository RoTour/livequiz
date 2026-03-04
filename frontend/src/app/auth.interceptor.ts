import { HttpInterceptorFn } from '@angular/common/http';
import { LocalStorageKeys } from '../LocalStorageKeys';

const SKIPPED_AUTH_ENDPOINTS = [
  '/api/auth/login',
  '/api/auth/anonymous',
  '/api/auth/students/request-login',
  '/api/auth/students/verify-email',
];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem(LocalStorageKeys.authorization);

  if (SKIPPED_AUTH_ENDPOINTS.some((endpoint) => req.url.includes(endpoint))) {
    return next(req);
  }

  if (token) {
    const updatedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${ token }`,
      },
    });
    return next(updatedReq);
  }

  return next(req);
};
