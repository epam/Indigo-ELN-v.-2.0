import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface BreadcrumbItem {
  label: string;
  url?: string;
  active: boolean;
}

@Injectable({ providedIn: 'root' })
export class BreadcrumbsStateService {
  private readonly itemsSubject = new BehaviorSubject<BreadcrumbItem[]>([]);
  readonly items$ = this.itemsSubject.asObservable();

  setItems(items: BreadcrumbItem[]): void {
    this.itemsSubject.next(items);
  }
}
