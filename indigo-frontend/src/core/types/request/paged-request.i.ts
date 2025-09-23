export interface PagedRequest {
  pageNo: number;
  pageSize: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface SortOption {
  label: string;
  value: string;
  defaultOrder?: 'asc' | 'desc';
}
