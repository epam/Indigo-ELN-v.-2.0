import { HttpParams } from '@angular/common/http';
import { PagedRequest } from '../types/request/paged-request.i';

export function getRequestParams(
  filter: Record<string, unknown>,
  paging?: PagedRequest,
) {
  let params = new HttpParams();

  const filters = { ...filter, ...paging };

  Object.keys(filters).forEach(
    (key) =>
      (typeof filters[key] == 'number' || !!filters[key]) &&
      (params = params.append(key, filters[key])),
  );

  return params;
}
