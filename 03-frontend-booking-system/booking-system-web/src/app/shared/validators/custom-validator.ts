import { AbstractControl, FormArray, ValidationErrors } from "@angular/forms";

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

    if (!passwordControl || !confirmPasswordControl) {
        return null;
    }
    if (passwordControl.invalid) {
        return null;
    }
    return passwordControl.value === confirmPasswordControl.value
        ? null
        : { passwordUnmatched: true };

}

export function dateTimeRangeValidator(abstractControl: AbstractControl): ValidationErrors | null {
    const startDate = abstractControl.get('startDate')?.value;
    const endDate = abstractControl.get('endDate')?.value;
    const startTime = abstractControl.get('startTime')?.value;
    const endTime = abstractControl.get('endTime')?.value;
    const interval = abstractControl.get('slotIntervalMinutes')?.value;

    if (!startDate || !endDate || !startTime || !endTime) {
        return null;
    }
    const combineStartTime = combineDateAndTime(startDate, startTime);
    const combineEndTime = combineDateAndTime(endDate, endTime);

    if (combineEndTime <= combineStartTime) {
        return { dateTimeRangeInvalid: true };
    }

    if (interval != null) {
        const diffMinutes = (combineEndTime.getTime() - combineStartTime.getTime()) / (1000 * 60);
        if (diffMinutes < interval) {
            return { slotIntervalInvalid: true };
        }
    }

    return null;
}

function combineDateAndTime(startDate: Date, startTime: string) {
    const [hours, minutes] = startTime.split(":").map(Number);

    const result = new Date(startDate);
    result.setHours(hours, minutes, 0, 0);

    return result
}

export function divisibleBy5Validator(abstractControl: AbstractControl) {
    const value = abstractControl.value;
    if (value == null) {
        return null;
    }
    return value % 5 === 0 ? null : { notDivisibleBy5: true };
}

export function timeRangeValidator(abstractControl: AbstractControl): ValidationErrors | null {
    const openTime = abstractControl.get('open')?.value;
    const closeTime = abstractControl.get('close')?.value;

    if (!openTime || !closeTime) {
        null;
    }
    const openMinutes = timeToMinutes(openTime);
    const closeMinutes = timeToMinutes(closeTime);

    return openMinutes >= closeMinutes ? { timeRangeInvalid: true } : null
}

export function timeOverlapValidator(abstractControl: AbstractControl): ValidationErrors | null {
    const intervalsArray = abstractControl.get('intervals') as FormArray;

    if (!intervalsArray || intervalsArray.length < 2) {
        return null;
    }

    const hasTimeRangeInvalid = intervalsArray.controls.some(control => 
        control.hasError('timeRangeInvalid')
    );

    if(hasTimeRangeInvalid){
        return null;
    }

    const ranges = intervalsArray.controls
        .map(ctrl => {
            const open = ctrl.get('open')?.value;
            const close = ctrl.get('close')?.value;
            if (!open || !close) return null;

            return {
                start: timeToMinutes(open),
                end: timeToMinutes(close)
            };
        })
        .filter(Boolean) as { start: number; end: number }[];

    // Sort by start time
    ranges.sort((a, b) => a.start - b.start);

    for (let i = 0; i < ranges.length - 1; i++) {
        if (ranges[i].end > ranges[i + 1].start) {
            return { timeOverlapError: true };
        }
    }

    return null;
}
function timeToMinutes(time: string) {
    const [hours, minutes] = time.split(":").map(Number);
    return hours * 60 + minutes;
}
