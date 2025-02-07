import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  constructor(private readonly http: HttpClient) {}

  public get<T>(url: string): Observable<T> {
    return this.http.get<T>(url);
  }

  // TODO replace any with generic type
  public post(url: string, body: any, options?: any): Observable<any> {
    return this.http.post(url, body, options);
  }
}
