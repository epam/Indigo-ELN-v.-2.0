import { ensureDistinct } from '@/core/utils/array.util';
import { DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { PaginatedBase } from './paginated.base';

export abstract class InfiniteScrollBase<T> extends PaginatedBase<T> {
  dataBh = new BehaviorSubject<T[]>([]);
  data$: Observable<T[]>;
  appendToTop = false;
  protected override isLoading = false;

  private isInfiniteLoaderVisible = false;
  private destroyRef = inject(DestroyRef);
  private dataListSub?: Subscription;

  protected override reinitialize(): void {
    this.dataListSub?.unsubscribe();
    this.dataBh.next([]);
    this.pager.pageNo = 0;

    super.reinitialize();
    this.config.enableScrollRestoration = true;

    this.dataListSub = this.dataList$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((data) => {
      const currValue = this.dataBh.value;
      const result = this.appendToTop ? [...data.items, ...currValue] : [...currValue, ...data.items];

      this.dataBh.next(ensureDistinct(result, 'id'));
      this.isLoading = false;

      if (this.isInfiniteLoaderVisible) {
        this.infiniteLoad();
      }
    });

    this.data$ = this.dataBh.asObservable();
  }

  onInfiniteLoaderLeft(): void {
    this.isInfiniteLoaderVisible = false;
  }

  onInfiniteLoaderEntered(): void {
    this.isInfiniteLoaderVisible = true;
  }

  infiniteLoad() {
    if (this.isLoading) return;

    const nextBackendPage = this.pager.pageNo + 1;
    const totalPages = Math.ceil(this.total / this.pager.pageSize);

    if (nextBackendPage < totalPages) {
      this.isLoading = true;
      this.pager.pageNo = nextBackendPage;
      this.fetchDataAndUpdateQueryParams(true);
    }
  }

  private resetListState() {
    this.isLoading = true;
    this.appendToTop = false;
    this.pager.pageNo = 0;
    this.dataBh.next([]);
  }

  override search(value: string) {
    this.resetListState();
    super.search(value);
  }

  override sort(sortBy: string, sort?: 'EARLIEST' | 'LATEST') {
    this.resetListState();
    super.sort(sortBy, sort);
  }

  override clearSort() {
    this.pager.pageNo = 0;
    this.dataBh.next([]);
    super.clearSort();
  }

  reload() {
    this.resetListState();
    this.firstLoad = true;
    this.fetchDataAndUpdateQueryParams();
  }
}
