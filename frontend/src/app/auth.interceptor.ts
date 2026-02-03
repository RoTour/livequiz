import { HttpInterceptorFn } from '@angular/common/http';
import { LocalStorageKeys } from '../LocalStorageKeys';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem(LocalStorageKeys.authorization);

  // Skip adding the token for the login endpoint
  // We check for /auth/login to be safe, assuming the path structure
  if (req.url.includes('/auth/login')) {
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
