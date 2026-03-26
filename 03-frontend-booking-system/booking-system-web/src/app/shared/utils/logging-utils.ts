import { AbstractControl, FormArray, FormControl, FormGroup } from "@angular/forms";

export function logFormErrors(
  form: AbstractControl,
  parentKey: string = '',
  depth: number = 0
) {
  if (!form) return;

  const indent = '  '.repeat(depth); // nicer visual nesting

  if (form instanceof FormGroup) {
    Object.keys(form.controls).forEach(key => {
      const control = form.get(key);
      logFormErrors(control!, parentKey ? `${parentKey}.${key}` : key, depth + 1);
    });

    // log FormGroup-level errors if any
    if (form.errors) {
      console.log(`${indent}${parentKey} FormGroup errors:`, form.errors);
    }

  } else if (form instanceof FormArray) {
    form.controls.forEach((control, index) => {
      logFormErrors(control, `${parentKey}[${index}]`, depth + 1);
    });

    // log FormArray-level errors if any
    if (form.errors) {
      console.log(`${indent}${parentKey} FormArray errors:`, form.errors);
    }

  } else if (form instanceof FormControl) {
    if (form.errors) {
      console.log(`${indent}${parentKey} FormControl errors:`, form.errors, 'value:', form.value);
    } else {
      // optional: uncomment if you want to see valid controls too
      // console.log(`${indent}${parentKey} is valid, value:`, form.value);
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
