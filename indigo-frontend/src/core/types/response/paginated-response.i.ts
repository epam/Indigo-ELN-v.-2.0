export interface PaginatedResponse<T> {
  pageNo: number;
  pageSize: number;
  totalItems: number;
  items: T[];
  firstIndex: number;
  lastIndex: number;
  totalPages: number;
}
