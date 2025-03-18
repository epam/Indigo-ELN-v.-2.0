import { ensureDistinct } from '@/core/utils/array.util';
import { BehaviorSubject, Observable, switchMap } from 'rxjs';
import { PaginatedComponent } from './paginated.component';

export abstract class InfiniteScrollComponent<T> extends PaginatedComponent<T> {
  dataBh = new BehaviorSubject<T[]>([]);
  data$: Observable<T[]>;
  search = '';

  refresh() {
    this.pager.pageNo = 1;
    this.dataBh.next([]);
    this.dataSubject$.next(null);
  }

  protected override initialize(): void {
    super.initialize();

    this.data$ = this.dataList$.pipe(
      switchMap((data) => {
        const currValue = this.dataBh.value;
        const result = [...currValue, ...data.items];

        this.dataBh.next(ensureDistinct(result, 'id'));

        return this.dataBh.asObservable();
      }),
    );
  }

  infiniteLoad() {
    const newPageSize = Number(this.pager.pageNo) + Number(1);
    if (newPageSize <= Math.ceil(this.total / this.pager.pageSize)) {
      this.pager.pageNo = newPageSize;
      this.fetchDataAndUpdateQueryParams(true);
    }
  }
}
