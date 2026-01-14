import { AbstractControl, ValidationErrors } from "@angular/forms";

export function includePositionValidator(control: AbstractControl): ValidationErrors | null {
    const include = control.get('includePosition')!.value;
    const lat = control.get('latitude')?.value;
    const long = control.get('longitude')?.value;

    if (include) {
        if (lat == null || long == null) {
            return { positionRequired: true };
        }
    }
    return null;
}

export function passwordMatchedValidator(abstractControl: AbstractControl): ValidationErrors | null {
    const passwordControl = abstractControl.get('password');
    const confirmPasswordControl = abstractControl.get('confirmPassword');

    if(!passwordControl || !confirmPasswordControl){
        return null;
    }
    if (passwordControl.invalid) {
        return null;
    }
    return passwordControl.value === confirmPasswordControl.value
                                        ? null
                                        : { passwordUnmatched: true };

}
