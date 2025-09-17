import { Injectable } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { Observable } from 'rxjs';
import {
  DictionaryFull,
  DictionaryList,
} from '@/core/types/entities/dictionary.i';

@Injectable({
  providedIn: 'root',
})
export class DictionaryService {
  constructor(private api: ApiService<unknown>) {}

  getDictionaries(): Observable<DictionaryList> {
    return this.api.request<DictionaryList>('get', 'dictionaries');
  }

  getDictionaryDetails(id: string): Observable<DictionaryFull> {
    return this.api.request<DictionaryFull>('get', `dictionaries/${id}/full`);
  }

  deleteDictionaryItem(
    dictionaryId: string,
    itemId: string,
  ): Observable<DictionaryFull> {
    return this.api.request<DictionaryFull>(
      'delete',
      `dictionaries/${dictionaryId}/${itemId}`,
    );
  }
}
