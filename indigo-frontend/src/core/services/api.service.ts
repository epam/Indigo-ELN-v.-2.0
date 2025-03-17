import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PagedRequest } from '../types/request/paged-request.i';
import { PaginatedResponse } from '../types/response/paginated-response.i';
import { getRequestParams } from '../utils/http.util';

@Injectable({
  providedIn: 'root',
})
export class ApiService<T> {
  controller = 'unknown';
  protected httpClient = inject(HttpClient);

  setup(controller: string) {
    this.controller = controller;
  }

  getPaged(pager: PagedRequest, filter?): Observable<PaginatedResponse<T>> {
    return this.httpClient.get<PaginatedResponse<T>>(this.buildUrl(), {
      params: getRequestParams(filter, pager),
    });
  }

  private buildUrl = (str?: string) =>
    `/api/eln/${this.controller}/${str || ''}`
      .replace(/\/\//g, '/')
      .replace(/\/+$/, '');
}
