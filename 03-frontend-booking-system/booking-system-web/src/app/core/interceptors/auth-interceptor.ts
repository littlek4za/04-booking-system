import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { LoggerService } from '@core/services/logger-service';
import { AuthService } from '@features/auth/auth-service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);
  const logger = inject(LoggerService);

  const userSession = authService.getSession();
  const guestSession = authService.getGuestSession();

  const url = req.url;
  const method = req.method;

  // 1. PUBLIC endpoints (no auth at all)

  const isPublic =
    url.includes('/login') ||
    url.includes('/register');

  if (isPublic) {
    logger.debug(`[AuthInterceptor] Public url: ${method} ${url}`);
    return next(req);
  }

  // 2. GUEST allowed endpoints

  // GET /bookings/{token}
  const isGetBookingByToken =
    method === 'GET' &&
    /\/bookings\/[^/]+$/.test(url);

  // POST /slots/{slotId}/bookings
  const isCreateBooking =
    method === 'POST' &&
    /\/slots\/\d+\/bookings$/.test(url);

  // PATCH /bookings/{bookingId}/delete
  const isDeleteBookingAsAttendee =
    method === 'PATCH' &&
    /\/bookings\/\d+\/delete$/.test(url);

  const isGuestAllowed =
    isGetBookingByToken || isCreateBooking || isDeleteBookingAsAttendee;


  // 3. Decide token priority

  let token: string | null = null;

  // User always wins
  if (userSession?.accessToken && authService.hasUserValidToken()) {
    token = userSession.accessToken;
    logger.debug(`[AuthInterceptor] User token attached for\n ${method} ${url}`);
  }

  // Guest only for specific endpoints
  else if (guestSession?.accessToken && isGuestAllowed) {
    token = guestSession.accessToken;
    logger.debug(`[AuthInterceptor] Guest token attached for\n ${method} ${url}`);
  }

  // 4. Attach token if exists

  if (token) {
    return next(
      req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      })
    );
  }

  logger.debug(`[AuthInterceptor] No token will be attached for\n ${method} ${url}`);

  return next(req);

}
