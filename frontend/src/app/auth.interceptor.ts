import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = sessionStorage.getItem('token');

  // Skip adding the token for the login endpoint
  // We check for /auth/login to be safe, assuming the path structure
  if (req.url.includes('/auth/login')) {
    return next(req);
  }

  if (token) {
    const updatedReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
    return next(updatedReq);
  }

  return next(req);
};
