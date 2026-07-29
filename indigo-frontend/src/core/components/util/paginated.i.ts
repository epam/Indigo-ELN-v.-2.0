import { FilterOption, SortOption } from '@/core/types/request/paged-request.i';

export interface PaginatedConfig {
  loadUrl: string;
  enableQueryParams?: boolean;
  enableScrollRestoration?: boolean;
  sortOptions?: SortOption[];
  filterOptions?: FilterOption[];
  defaultSort?: {
    sortBy: string;
    sort: 'EARLIEST' | 'LATEST';
  };
}
