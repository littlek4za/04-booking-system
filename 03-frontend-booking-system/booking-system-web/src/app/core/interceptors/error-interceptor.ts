import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { AuthService } from "../../features/auth/auth-service";
import { catchError, EMPTY, throwError } from "rxjs";
import { GlobalErrorService } from "../services/global-error-service";
import { extractFieldErrorMessage } from "@shared/utils/error-utils";

export const errorInterceptor:HttpInterceptorFn = (req,next) => {
    const authService = inject(AuthService);
    const globalErrorService = inject(GlobalErrorService);

    return next(req).pipe(
        catchError((err:HttpErrorResponse) => {
            console.error(`[API Error] ${req.method} ${req.url}:`, err);
            if (err.status === 401){
                authService.logoutByExpiry();
                alert(extractFieldErrorMessage(err));
                return throwError(() => err);
            }

            if (err.status === 0){
                alert('Network error. Please check connection.');
                return throwError(() => err);
            }

            alert(extractFieldErrorMessage(err));

            return throwError(() => err);
        })
    );
}