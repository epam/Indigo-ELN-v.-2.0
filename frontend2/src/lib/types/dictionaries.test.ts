import { describe, expect, it } from 'vitest';

import { sortByName } from '@/lib/types/dictionaries.ts';

describe('sortByName', () => {
  it('orders refs alphabetically without touching the input', () => {
    const items = [
      { id: '3', name: 'Very Toxic' },
      { id: '1', name: 'Carcinogen' },
      { id: '2', name: 'Irritant' },
    ];
    expect(sortByName(items).map((item) => item.name)).toEqual(['Carcinogen', 'Irritant', 'Very Toxic']);
    expect(items[0].name).toBe('Very Toxic');
  });
});
