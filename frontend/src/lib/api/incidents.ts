import { useMutation } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import { INCIDENTS_PATH } from '@/lib/report-error';

export function useSubmitIncident() {
  return useMutation({
    mutationFn: (formData: FormData) => apiFetch<void>(INCIDENTS_PATH, { method: 'POST', formData }),
  });
}
