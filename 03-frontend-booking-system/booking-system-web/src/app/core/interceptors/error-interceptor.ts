import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../../features/auth/auth-service";
import { catchError, throwError } from "rxjs";
import { ErrorMapperService } from "@core/services/error-mapper-service";
import { GlobalErrorService } from "@core/services/global-error-service";
import { LoggerService } from "@core/services/logger-service";

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const errorMapperService = inject(ErrorMapperService);
    const globalErrorService = inject(GlobalErrorService);
    const logger = inject(LoggerService);

    return next(req).pipe(
        catchError((err: HttpErrorResponse) => {

            const appError = errorMapperService.mapError(err);

            logger.error(`[API Error] ${req.method} ${req.url}:`, appError);

            if (err.status === 401 && req.headers.get('Authorization')) {
                authService.logoutByExpiry();
                return throwError(() => err);
            }

            globalErrorService.show(errorMapperService.toMessage(appError, err.status));
            return throwError(() => err);
        })
    );
}