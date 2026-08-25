import { useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';

export const imageKeys = {
  image: (path: string) => ['apiImage', path] as const,
};

/**
 * An image served by the ELN API, as its source text.
 *
 * Every image endpoint here produces `image/svg+xml`, and `apiFetch` already hands that
 * back as a string: `parseBody` reads the body, fails to parse it as JSON, and returns the
 * text. So there is no blob and no object URL to own — indigo-frontend's ApiImage created
 * one per image and never revoked it. A binary endpoint would need a separate fetcher.
 */
export function fetchApiImage(path: string, signal?: AbortSignal): Promise<string> {
  return apiFetch<string>(path, { signal });
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
