import { inject, Injectable } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import {
  DictionaryItemRef,
  BuiltInDictionary,
} from '@/core/types/entities/dictionary.i';
import { tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class BuiltInDictionaryService {
  private service = inject(ApiService);

  // Private cache to track loaded dictionaries
  private cache = new Map<BuiltInDictionary, DictionaryItemRef[]>();
  private loading = new Set<BuiltInDictionary>();

  /**
   * Load one or more dictionaries into cache.
   * Prevents duplicate requests and respects existing cache.
   */
  load(dictionaries: BuiltInDictionary[], forceReload = false) {
    dictionaries.forEach((dict) => this.loadSingle(dict, forceReload));
  }

  /**
   * Get dictionary items from cache.
   * Returns empty array if not loaded.
   */
  getDictionaryItem(dictionary: BuiltInDictionary): DictionaryItemRef[] {
    return this.cache.get(dictionary) ?? [];
  }

  private loadSingle(dictionary: BuiltInDictionary, forceReload = false) {
    // Return early if already cached and not forcing reload
    if (!forceReload && this.cache.has(dictionary)) {
      return;
    }

    // Prevent duplicate requests
    if (this.loading.has(dictionary)) {
      return;
    }

    this.loading.add(dictionary);

    this.service
      .request<DictionaryItemRef[]>('get', `dictionaries/${dictionary}`)
      .pipe(
        tap({
          error: () => {
            this.cache.set(dictionary, []);
            this.loading.delete(dictionary);
          },
        }),
      )
      .subscribe((items) => {
        this.cache.set(dictionary, items);
        this.loading.delete(dictionary);
      });
  }
}
