import { Injectable } from "@angular/core";

@Injectable({
    providedIn: 'root',
})

export class GlobalErrorService {
    show(message: string) {
        alert(message);
    }
}
