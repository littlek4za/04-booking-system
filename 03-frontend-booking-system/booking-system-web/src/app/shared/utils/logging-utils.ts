import { AbstractControl, FormArray, FormControl, FormGroup } from "@angular/forms";
import { LoggerService } from "@core/services/logger-service";

export function logFormErrors(
  form: AbstractControl,
  logger: LoggerService,
  parentKey: string = '',
  depth: number = 0
) {
  if (!form) return;

  const indent = '  '.repeat(depth); // nicer visual nesting

  if (form instanceof FormGroup) {
    Object.keys(form.controls).forEach(key => {
      const control = form.get(key);
      logFormErrors(control!, logger, parentKey ? `${parentKey}.${key}` : key, depth + 1);
    });

    // log FormGroup-level errors if any
    if (form.errors) {
      logger.debug(`${indent}${parentKey} FormGroup errors:`, form.errors);
    }

  } else if (form instanceof FormArray) {
    form.controls.forEach((control, index) => {
      logFormErrors(control, logger, `${parentKey}[${index}]`, depth + 1);
    });

    // log FormArray-level errors if any
    if (form.errors) {
      logger.debug(`${indent}${parentKey} FormArray errors:`, form.errors);
    }

  } else if (form instanceof FormControl) {
    if (form.errors) {
      logger.debug(`${indent}${parentKey} FormControl errors:`, form.errors, 'value:', form.value);
    } else {
      // optional: uncomment if you want to see valid controls too
      // console.log(`${indent}${parentKey} is valid, value:`, form.value);
    }
  }
}

export function logControls(control: AbstractControl, logger: LoggerService, path = ''): void {
  if (control instanceof FormGroup) {
    Object.entries(control.controls).forEach(([key, child]) => {
      logControls(child, logger, path ? `${path}.${key}` : key);
    });
    return;
  }

  if (control instanceof FormArray) {
    control.controls.forEach((child, index) => {
      logControls(child, logger, `${path}[${index}]`);
    });
    return;
  }

  logger.debug(
    path,
    '=> value:',
    control.value,
    '| valid:',
    control.valid,
    '| touched:',
    control.touched
  );
}
