import { AbstractControl, FormControl, ValidationErrors } from "@angular/forms";

export class AuthFormValidator {

    static passwordMatched(abstractControl: AbstractControl): ValidationErrors | null {
        const passwordControl = abstractControl.get('password');
        const confirmPasswordControl = abstractControl.get('confirmPassword');

        if (!passwordControl || !confirmPasswordControl) {
            return null;
        }
        if (passwordControl.invalid) {
            return null;
        }
        return passwordControl.value === confirmPasswordControl.value
            ? null
            : { passwordMatched: true };
    }
}
