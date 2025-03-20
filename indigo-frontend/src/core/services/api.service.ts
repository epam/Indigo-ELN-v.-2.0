import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PagedRequest } from '../types/request/paged-request.i';
import { PaginatedResponse } from '../types/response/paginated-response.i';
import { getRequestParams } from '../utils/http.util';

interface UrlMapType {
  getPagedUrl?: string;
  getSingleUrl?: string;
  createUrl?: string;
  updateUrl?: string;
  deleteUrl?: string;
}

@Injectable({
  providedIn: 'root',
})
export class ApiService<T> {
  // In order to support some urls like /notebooks/{notebookId}/experiments
  protected urlMap: UrlMapType = {
    getPagedUrl: '',
    getSingleUrl: '',
    createUrl: '',
    updateUrl: '',
    deleteUrl: '',
  };

  controller = 'unknown';
  protected httpClient = inject(HttpClient);

  public setup(controller: string, urlOverrides?: UrlMapType) {
    this.controller = controller;
    if (urlOverrides) {
      Object.keys(urlOverrides).forEach(
        (key) => (this.urlMap[key] = urlOverrides[key]),
      );
    }
  }

  public getPaged(
    pager: PagedRequest,
    filter?,
  ): Observable<PaginatedResponse<T>> {
    return this.httpClient.get<PaginatedResponse<T>>(
      this.buildUrl(this.urlMap.getPagedUrl),
      {
        params: getRequestParams(filter, pager),
      },
    );
  }

  public create(body: unknown): Observable<T> {
    return this.httpClient.post<T>(this.buildUrl(this.urlMap.createUrl), body);
  }

  private buildUrl = (str?: string) =>
    `/api/eln/${this.controller}/${str || ''}`
      .replace(/\/\//g, '/')
      .replace(/\/+$/, '');
}
