import { ensureDistinct } from '@/core/utils/array.util';
import { BehaviorSubject, Observable, switchMap } from 'rxjs';
import { PaginatedBase } from './paginated.base';

export abstract class InfiniteScrollBase<T> extends PaginatedBase<T> {
  dataBh = new BehaviorSubject<T[]>([]);
  data$: Observable<T[]>;
  search = '';
  appendToTop = false;

  protected override initialize(): void {
    super.initialize();
    this.config.enableScrollRestoration = true;
    this.data$ = this.dataList$.pipe(
      switchMap((data) => {
        const currValue = this.dataBh.value;
        const result = this.appendToTop
          ? [...data.items, ...currValue]
          : [...currValue, ...data.items];

        this.dataBh.next(ensureDistinct(result, 'id'));

        return this.dataBh.asObservable();
      }),
    );
  }

  infiniteLoad() {
    // Since backend uses zero-based indexing (pageNo=0 equals page 1),
    const currentBackendPage = this.pager.pageNo;
    const nextBackendPage = currentBackendPage + 1;

    const totalPages = Math.ceil(this.total / this.pager.pageSize);

    if (nextBackendPage < totalPages) {
      this.pager.pageNo = nextBackendPage;
      this.fetchDataAndUpdateQueryParams(true);
    }
  }

  reload() {
    this.firstLoad = true;
    this.appendToTop = true;
    this.dataSubject$.next(null);
  }
}
