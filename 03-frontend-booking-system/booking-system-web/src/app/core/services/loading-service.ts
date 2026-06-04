import { computed, Injectable, signal } from '@angular/core';
import { finalize, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LoadingService {

  private readonly loadingCount = signal(0);
  private readonly slowLoading = signal(false);

  readonly isLoading = computed(() => this.loadingCount() > 0);
  readonly isSlowLoading = computed(() => this.slowLoading());

  private slowTimer?: ReturnType<typeof setTimeout>;

  show() {
    this.loadingCount.update(count => count + 1);

    if (this.loadingCount() === 1) {
      this.slowLoading.set(false);

      this.slowTimer = setTimeout(() => {
        if (this.isLoading()) {
          this.slowLoading.set(true);
        }
      }, 10_000);
    }
  }

  hide() {
    this.loadingCount.update(count => Math.max(0, count - 1));

    if (this.loadingCount() === 0) {
      this.slowLoading.set(false);

      if (this.slowTimer) {
        clearTimeout(this.slowTimer);
        this.slowTimer = undefined;
      }
    }
  }

  track<T>(source$: Observable<T>): Observable<T> {
    this.show();

    return source$.pipe(
      finalize(() => this.hide())
    );
  }
  
}
