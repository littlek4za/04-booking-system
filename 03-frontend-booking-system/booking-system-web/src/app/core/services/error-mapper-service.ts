import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ErrorCode } from '@core/model/error-code';
import { ErrorResponseDto } from '@core/model/error-response-dto';

@Injectable({
  providedIn: 'root',
})
export class ErrorMapperService {
  mapError(err: HttpErrorResponse): ErrorResponseDto {

    const body = err.error;

    if (!body || typeof body !== 'object') {
      return {
        status: err.status,
        message: err.message,
      };
    }

    return {
      status: err.status,
      error: body.error,
      message: body.message,
      errorCode: body.errorCode,
      timestamp: body.timestamp,
      path: body.path,
      fieldErrorList: body.fieldErrorList
    }
  }

  toMessage(appError: ErrorResponseDto | null, status: number): string {
    if (appError?.errorCode) {
      return this.mapCodeToMessage(appError.errorCode);
    }

    return this.mapStatusToMessage(status);
  }

  mapCodeToMessage(errorCode: string): string {

    switch (errorCode) {

      // Guest
      case ErrorCode.GUEST_TOKEN_INVALID:
        return "Guest token is invalid.";

      case ErrorCode.GUEST_TOKEN_EXPIRED:
        return "Guest session expired.";

      case ErrorCode.CAPTCHA_INVALID:
        return "Captcha validation failed.";

      case ErrorCode.CAPTCHA_REQUIRED:
        return "Captcha is required.";

      case ErrorCode.USERNAME_ALREADY_REGISTERED:
        return "Username is already registered.";

      case ErrorCode.EMAIL_ALREADY_REGISTERED:
        return "Email is already registered.";

      // Auth
      case ErrorCode.FORBIDDEN:
        return "You do not have permission to perform this action.";

      case ErrorCode.UNAUTHORIZED:
        return "Authentication required.";

      case ErrorCode.ACCESS_DENIED:
        return "Access denied.";

      case ErrorCode.TOKEN_INVALID:
        return "Session expired. Please login again.";

      case ErrorCode.USER_NOT_FOUND:
        return "User not found.";

      case ErrorCode.INVALID_CREDENTIALS:
        return "Invalid username or password.";

      case ErrorCode.TOKEN_TYPE_INVALID:
        return "Invalid token type.";

      // Validation
      case ErrorCode.FIELD_VALIDATION_FAILED:
        return "Please check the highlighted fields.";

      // System
      case ErrorCode.INVALID_STATE:
        return "Invalid system state.";

      case ErrorCode.INTERNAL_ERROR:
        return "Something went wrong. Please try again later.";

      case ErrorCode.INVALID_CAPTCHA:
        return "Invalid captcha.";

      // Invitation
      case ErrorCode.INVITATION_NOT_FOUND:
        return "Invitation not found.";

      case ErrorCode.INVITATION_EXPIRED:
        return "Invitation expired.";

      case ErrorCode.INVITATION_MAX_USAGE_REACHED:
        return "Invitation usage limit reached.";

      case ErrorCode.INVITATION_USER_USAGE_LIMIT_REACHED:
        return "User invitation limit reached.";

      // Event
      case ErrorCode.EVENT_NOT_FOUND:
        return "Event not found.";

      case ErrorCode.EVENT_TYPE_INVALID:
        return "Invalid event type.";

      case ErrorCode.EVENT_TYPE_CHANGE_NOT_ALLOWED:
        return "Event type change is not allowed.";

      case ErrorCode.EVENT_HAS_ACTIVE_BOOKINGS:
        return "Event has active bookings.";

      // Slot
      case ErrorCode.SLOT_NOT_FOUND:
        return "Slot not found.";

      case ErrorCode.SLOT_INCLUDE_MODE_INVALID:
        return "Invalid slot include mode.";

      case ErrorCode.SLOT_FULL:
        return "This slot is already full.";

      case ErrorCode.SLOT_TIME_ALREADY_BOOKED:
        return "This time slot is already booked.";
      
      case ErrorCode.SLOT_HAS_ACTIVE_BOOKINGS:
        return "Slot has active bookings.";

      // Booking
      case ErrorCode.BOOKING_NOT_FOUND:
        return "Booking not found.";

      case ErrorCode.BOOKING_REQUEST_INVALID:
        return "Invalid booking request.";

      case ErrorCode.EVENT_BOOKING_LIMIT_REACHED:
        return "Event booking limit reached.";

      case ErrorCode.SLOT_BOOKING_LIMIT_REACHED:
        return "Slot booking limit reached.";

      // Mismatch
      case ErrorCode.SLOT_EVENT_MISMATCH:
        return "Slot does not belong to this event.";

      case ErrorCode.SLOT_INVITATION_MISMATCH:
        return "Slot invitation mismatch.";

      case ErrorCode.SLOT_CONFIGURATION_INVALID:
        return "Invalid slot configuration.";

      case ErrorCode.SLOT_UPDATE_NOT_ALLOWED:
        return "Slot update is not allowed.";

      default:
        console.warn(`Unknown error code: ${errorCode}`)
        return "An unexpected error occurred.";
    }
  }

  private mapStatusToMessage(status?: number): string {
    switch (status) {
      case 0:
        return 'Network error. Please check your connection.';
      case 400:
        return 'Invalid request. Please check your input.';
      case 401:
        return 'Authentication required.';
      case 403:
        return 'You do not have permission to perform this action.';
      case 404:
        return 'The requested resource was not found.';
      default:
        if (status && status >= 500) {
          return 'Something went wrong. Please try again later.';
        }

        return 'An unexpected error occurred.';
    }
  }

}
