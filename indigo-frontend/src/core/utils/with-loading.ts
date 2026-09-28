import { finalize, Observable } from 'rxjs';

export function withLoading<T>(setLoading: (loading: boolean) => void) {
  return (source: Observable<T>): Observable<T> => {
    setLoading(true);
    return source.pipe(finalize(() => setLoading(false)));
  };
}
