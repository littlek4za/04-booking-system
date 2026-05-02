import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '@features/auth/auth-service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);

  const userSession = authService.getSession();
  const guestSession = authService.getGuestSession();

  const url = req.url;
  const method = req.method;

  // -----------------------------
  // 1. PUBLIC endpoints (no auth at all)
  // -----------------------------
  const isPublic =
    url.includes('/login') ||
    url.includes('/register');

  if (isPublic) {
    return next(req);
  }

  // -----------------------------
  // 2. GUEST allowed endpoints
  // -----------------------------

  // GET /bookings/{token}
  const isGetBookingByToken =
    method === 'GET' &&
    /\/bookings\/[^/]+$/.test(url);

  // POST /slots/{slotId}/bookings
  const isCreateBooking =
    method === 'POST' &&
    /\/slots\/\d+\/bookings$/.test(url);

  const isGuestAllowed =
    isGetBookingByToken || isCreateBooking;

  // -----------------------------
  // 3. Decide token priority
  // -----------------------------

  let token: string | null = null;

  // User always wins
  if (userSession?.accessToken && authService.isLoggedInUser()) {
    token = userSession.accessToken;
    console.log('userToken used');
  }

  // Guest only for specific endpoints
  else if (guestSession?.accessToken && isGuestAllowed) {
    token = guestSession.accessToken;
    console.log('guestToken used');
  }

  // -----------------------------
  // 4. Attach token if exists
  // -----------------------------
  if (token) {
    return next(
      req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      })
    );
  }

  return next(req);

}
