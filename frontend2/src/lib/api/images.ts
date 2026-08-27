import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';

export const imageKeys = {
  image: (path: string) => ['apiImage', path] as const,
};

/**
 * An image served by the ELN API, as its source text.
 *
 * There is no blob and no object URL to own — indigo-frontend's ApiImage created one per
 * image and never revoked it. A binary endpoint such as the PNG avatar would need its own
 * fetcher (`responseType: 'blob'`), since the consumer inlines the text as an SVG data URL.
 */
export function fetchApiImage(path: string, signal?: AbortSignal): Promise<string> {
  return apiFetch(path, { signal, responseType: 'text' });
}

/**
 * `enabled` is how the caller defers the request until the image is worth loading — see
 * `useInViewport`. Callers put any cache-buster in the path, so the path fully identifies
 * the bytes and a cached image can never go stale.
 */
export function useApiImage(path: string, enabled: boolean) {
  return useQuery({
    queryKey: imageKeys.image(path),
    queryFn: ({ signal }) => fetchApiImage(path, signal),
    staleTime: Infinity,
    enabled,
  });
}
