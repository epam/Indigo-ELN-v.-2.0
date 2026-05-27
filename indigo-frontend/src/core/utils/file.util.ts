import { Observable } from 'rxjs';

export function openFileDialog(accept: string): Observable<File> {
  return new Observable((observer) => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = accept;

    const onChange = () => {
      const file = input.files?.[0];
      if (file) {
        observer.next(file);
      }
      observer.complete();
    };

    const onCancel = () => {
      // make sure cancel didn't fire before onChange
      setTimeout(() => observer.complete(), 1000);
    };

    input.addEventListener('change', onChange);
    window.addEventListener('focus', onCancel, { once: true });

    input.click();

    return () => {
      input.removeEventListener('change', onChange);
      window.removeEventListener('focus', onCancel);
    };
  });
}
