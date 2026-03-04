import { AbstractControl, FormArray, FormControl, FormGroup } from "@angular/forms";

export function logFormErrors(form: AbstractControl, parentKey: string = '') {
    if (!form) return;
    if (form instanceof FormGroup) {
        Object.keys(form.controls).forEach(key => {
            const control = form.get(key);
            logFormErrors(control!, parentKey ? `${parentKey}.${key}` : key);
        });
    } else if (form instanceof FormArray) {
        form.controls.forEach((control, index) => {
            logFormErrors(control, `${parentKey}[${index}]`);
        });
    } else if (form instanceof FormControl) {
        if (form.errors) {
            console.log(`${parentKey} errors:`, form.errors);
        }
    }
}

export function logControls(control: AbstractControl, path = ''): void {
  if (control instanceof FormGroup) {
    Object.entries(control.controls).forEach(([key, child]) => {
      logControls(child, path ? `${path}.${key}` : key);
    });
    return;
  }

  if (control instanceof FormArray) {
    control.controls.forEach((child, index) => {
      logControls(child, `${path}[${index}]`);
    });
    return;
  }

  console.log(
    path,
    '=> value:', control.value,
    '| valid:', control.valid,
    '| touched:', control.touched
  );
}
