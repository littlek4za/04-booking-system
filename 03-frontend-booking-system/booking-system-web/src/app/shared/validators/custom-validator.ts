import { AbstractControl, FormArray, ValidationErrors, ValidatorFn } from "@angular/forms";
import { EventTypeModel } from "@features/events/dtos/event-type-model";
import { InvitationResponseDto } from "@features/invitations/dtos/invitation-response-dto";
import { SlotResponseDto } from "@features/slots/dtos/slot-response-dto";
import { TimeRange } from "@shared/model/time-range";
import moment from "moment";

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
        return { endBeforeStart: true };
    }

    if (interval != null) {
        const diffMinutes = (combineEndTime.getTime() - combineStartTime.getTime()) / (1000 * 60);
        if (diffMinutes < interval) {
            return { rangeTooShortForInterval: true };
        }
    }

    return null;
}

function combineDateAndTime(startDate: Date, startTime: string) {
    const [hours, minutes] = startTime.split(":").map(Number);

    const result = new Date(startDate);
    result.setHours(hours, minutes, 0, 0);

    return result;
}

export function divisibleBy5Validator(abstractControl: AbstractControl) {
    const value = abstractControl.value;
    if (value == null) {
        return null;
    }
    return value % 5 === 0 ? null : { notDivisibleBy5: true };
}

export function timeRangeValidatorForFlexible (getSlotIntervalMinutes?: () => number | null) {
    return (abstractControl: AbstractControl): ValidationErrors | null => {

        const startDate = abstractControl.get('startDate')?.value;
        const startTime = abstractControl.get('startTime')?.value;
        const endDate = abstractControl.get('endDate')?.value;
        const endTime = abstractControl.get('endTime')?.value;

        if (!startDate || !startTime || !endDate || !endTime) {
            return null;
        }

        const combineStartTime = combineDateAndTime(startDate, startTime);
        const combineEndTime = combineDateAndTime(endDate, endTime);
        const startMinutes = timeToMinutes(combineStartTime.toISOString());
        const endMinutes = timeToMinutes(combineEndTime.toISOString());

        if (startMinutes >= endMinutes) {
            return { endBeforeStart: true };
        }

        if (getSlotIntervalMinutes != null) {
            const minInterval = Number(getSlotIntervalMinutes?.());
            if (minInterval > 0 && endMinutes - startMinutes < minInterval) {
                return { rangeTooShortForInterval: true };
            }
        }
        return null;
    }
}

export function timeRangeValidatorForBusiness (getSlotIntervalMinutes?: () => number | null) {
    return (abstractControl: AbstractControl): ValidationErrors | null => {

        const open = abstractControl.get('open')?.value;
        const close = abstractControl.get('close')?.value;

        if (!open  || !close) {
            return null;
        }

        const startMinutes = timeToMinutes(open);
        const endMinutes = timeToMinutes(close);

        if (startMinutes >= endMinutes) {
            return { endBeforeStart: true };
        }

        if (getSlotIntervalMinutes != null) {
            const minInterval = Number(getSlotIntervalMinutes?.());
            if (minInterval > 0 && endMinutes - startMinutes < minInterval) {
                return { rangeTooShortForInterval: true };
            }
        }
        return null;
    }
}

export function timeOverlapValidator(abstractControl: AbstractControl): ValidationErrors | null {
    const intervalsArray = abstractControl.get('intervals') as FormArray;

    if (!intervalsArray || intervalsArray.length < 2) {
        return null;
    }

    const hasTimeRangeInvalid = intervalsArray.controls.some(control =>
        control.hasError('timeRangeInvalid')
    );

    if (hasTimeRangeInvalid) {
        return null;
    }

    const ranges = intervalsArray.controls
        .map(ctrl => {
            const startDate = ctrl.get('startDate')?.value;
            const startTime = ctrl.get('startTime')?.value;
            const endDate = ctrl.get('endDate')?.value;
            const endTime = ctrl.get('endTime')?.value;

            if (!startDate || !startTime || !endDate || !endTime) return null;

            const combineStartTime = combineDateAndTime(startDate, startTime);
            const combineEndTime = combineDateAndTime(endDate, endTime);
            const startMinutes = timeToMinutes(combineStartTime.toISOString());
            const endMinutes = timeToMinutes(combineEndTime.toISOString());

            return {
                start: startMinutes,
                end: endMinutes
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

function timeToMinutes(value: string) {

    if (value.includes('T')) {
        const date = new Date(value);
        return Math.floor(date.getTime() / 60000); // gettime return millisecond, 1 sec = 1000 milliseconds 
    }

    const [hours, minutes] = value.split(":").map(Number);
    return hours * 60 + minutes;
}

export function maxUsagePerIdentityExceedMaxUsage(abstractControl: AbstractControl): ValidationErrors | null {
    const noMaxUsage = abstractControl.get('noMaxUsage')?.value;
    const noMaxUsagePerIdentity = abstractControl.get('noMaxUsagePerIdentity')?.value;
    const maxUsagePerIdentity = abstractControl.get('maxUsagePerIdentity')?.value;
    const maxUsage = abstractControl.get('maxUsage')?.value;
    if (!noMaxUsage && !noMaxUsagePerIdentity) {
        if (maxUsage != null &&
            maxUsagePerIdentity != null &&
            maxUsagePerIdentity > maxUsage) {
            return { maxUsagePerIdentityMaxError: true }
        }
    }
    return null;
}

export function validateStartAndEndTimeBaseOnEvent(slot: SlotResponseDto, invitation: InvitationResponseDto): ValidatorFn | null {
    return (abstractControl: AbstractControl): ValidationErrors | null => {
        const requestedStart = abstractControl.get('choosenStartTime')?.value;
        const requestedEnd = abstractControl.get('choosenEndTime')?.value;

        if (!requestedStart || !requestedEnd) {
            return null;
        }

        const requestedStartTimeDate = new Date(requestedStart);
        const requestedEndTimeDate = new Date(requestedEnd);

        if (invitation.event.eventType == EventTypeModel.FLEXIBLE) {
            const instantRangeList: TimeRange[] = slot.flexibleDaysHours || [];

            const fitsInInstantRange = instantRangeList.some(range => {
                const openZdt = new Date(range.open);
                const closeZdt = new Date(range.close);
                return requestedStartTimeDate >= openZdt && requestedEndTimeDate <= closeZdt;
            });

            if (!fitsInInstantRange) {
                return { startOrEndTimeOutOfRange: true };
            }
        }

        if (invitation.event.eventType == EventTypeModel.BUSINESS && slot.businessTimeZone && slot.businessDaysHours && slot.businessAllowOt) {
            const businessTimeZone = slot.businessTimeZone;

            const requestedStartZdt = moment.tz(requestedStart, businessTimeZone);
            const requestedEndZdt = moment.tz(requestedEnd, businessTimeZone);

            const startDay = requestedStartZdt.day();

            const businessDaysHours = slot.businessDaysHours;
            const instantRangListStart = businessDaysHours[startDay] ?? [];

            const fitsInInstantRange = instantRangListStart.some(range => {
                const openZdt = moment.tz(
                    requestedStartZdt.format('YYYY-MM-DD') + 'T' + range.open,
                    businessTimeZone);
                const closeZdt = moment.tz(
                    requestedEndZdt.format('YYYY-MM-DD') + 'T' + range.close,
                    businessTimeZone);
                if (closeZdt.isBefore(openZdt)) {
                    closeZdt.add(1, 'day');
                }

                if (slot.businessAllowOt) {
                    return requestedStartZdt.isSameOrAfter(openZdt) && requestedStartZdt.isSameOrBefore(closeZdt);
                } else {
                    return requestedStartZdt.isSameOrAfter(openZdt) && requestedEndZdt.isSameOrBefore(closeZdt);
                }
            })

            if (!fitsInInstantRange) {
                return { startOrEndTimeOutOfRange: true };
            }
        }

        return null;
    }
}
