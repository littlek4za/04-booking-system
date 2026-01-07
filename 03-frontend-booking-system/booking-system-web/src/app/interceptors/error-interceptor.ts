import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../services/auth-service";
import { catchError, EMPTY, throwError } from "rxjs";
import { GlobalErrorService } from "../services/global-error-service";

export const errorInterceptor:HttpInterceptorFn = (req,next) => {
    const authService = inject(AuthService);
    const globalErrorService = inject(GlobalErrorService);

    return next(req).pipe(
        catchError(error => {
            if (error.status === 401){
                authService.logoutByExpiry();
                // return EMPTY;
            }
            if (error.status === 0){
                alert('Network error. Please check connection.');
                return EMPTY;
            }

            return throwError(() => error);
        })
    );
}