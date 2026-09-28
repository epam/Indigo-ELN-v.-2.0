import { inject, Injectable } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { BuiltInDictionary, DictionaryItemRef } from '@/core/types/entities/dictionary.i';
import { Observable, ReplaySubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class BuiltInDictionaryService {
  private service = inject(ApiService);

  // Private cache to track loaded dictionaries
  private cache = new Map<BuiltInDictionary, ReplaySubject<DictionaryItemRef[]>>();

  /**
   * Get dictionary items from cache.
   * Returns empty array if not loaded.
   */
  getDictionaryItems(dictionary: BuiltInDictionary): Observable<DictionaryItemRef[]> {
    if (!this.cache.has(dictionary)) {
      const subject = new ReplaySubject<DictionaryItemRef[]>();
      this.cache.set(dictionary, subject);
      this.load(dictionary);
      return subject;
    }
    return this.cache.get(dictionary);
  }

  private load(dictionary: BuiltInDictionary) {
    this.service.request<DictionaryItemRef[]>('get', `dictionaries/${dictionary}`).subscribe({
      next: (items) => {
        this.cache.get(dictionary).next(items);
      },
    });
  }
}
