import { FieldError } from "./field-error";

export class ErrorResponseDto {
    status?: number;
    error?: string;
    message?: string;
    errorCode?: string;
    timestamp?: string;
    path?: string;
    fieldErrorList?: FieldError[];
}
