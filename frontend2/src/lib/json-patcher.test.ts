import { describe, expect, it } from 'vitest';

import { JSON_PATCHER, JSONPatcher } from '@/lib/json-patcher';

/**
 * The cases are `JSONPatcherTest.java`'s, read in the apply direction: each one takes the
 * base the backend diffed *from* and the patch it produced, and expects the base it diffed
 * *to*. The two implementations only agree if this file and that one describe the same
 * format.
 */

/** Root is an object, so no path is a set or a list — the plain object/value case. */
const plain = new JSONPatcher({}, {});
/** Root itself is the keyed collection, matching the Java tests' `Map.of(List.of(), "anchor")`. */
const asSet = new JSONPatcher({ '': 'anchor' }, {});
const asList = new JSONPatcher({}, { '': 'anchor' });

function apply(patcher: JSONPatcher, base: unknown, patch: unknown): unknown {
  return patcher.apply(base, patch)[0];
}

describe('values', () => {
  it('leaves a node alone when its patch is null', () => {
    expect(apply(plain, { a: 'a' }, { a: null })).toEqual({ a: 'a' });
  });

  it('creates from $new', () => {
    expect(apply(plain, {}, { a: { $new: 'a' } })).toEqual({ a: 'a' });
  });

  it('deletes a key whose patch is $old only', () => {
    expect(apply(plain, { a: 'a', b: 'b' }, { a: { $old: 'a' } })).toEqual({ b: 'b' });
  });

  it('replaces on $old + $new', () => {
    expect(apply(plain, { a: 'a' }, { a: { $old: 'a', $new: 'b' } })).toEqual({ a: 'b' });
  });

  it('replaces a whole object rather than merging into it', () => {
    const base = { n: { f: 1, keep: 'x' } };
    expect(apply(plain, base, { n: { $old: { f: 1 }, $new: { f: 2 } } })).toEqual({ n: { f: 2 } });
  });

  it('merges key by key when the patch carries neither $old nor $new', () => {
    const base = { n: { f: 1, keep: 'x' } };
    expect(apply(plain, base, { n: { f: { $old: 1, $new: 2 } } })).toEqual({ n: { f: 2, keep: 'x' } });
  });

  it('does not mutate the base it was given', () => {
    const base = { n: { f: 1 } };
    apply(plain, base, { n: { f: { $old: 1, $new: 2 } } });
    expect(base).toEqual({ n: { f: 1 } });
  });
});

describe('sets — keyed, unordered', () => {
  const a1 = { anchor: 'A1', name: 'a' };
  const a2 = { anchor: 'A2', name: 'b' };

  it('appends an item the base does not have', () => {
    expect(apply(asSet, [a1], { A2: { $new: a2 } })).toEqual([a1, a2]);
  });

  it('drops an item whose patch is $old only', () => {
    expect(apply(asSet, [a1, a2], { A2: { $old: a2 } })).toEqual([a1]);
  });

  it('updates in place, by key rather than by position', () => {
    expect(apply(asSet, [a1, a2], { A2: { name: { $old: 'b', $new: 'c' } } })).toEqual([
      a1,
      { anchor: 'A2', name: 'c' },
    ]);
  });
});

describe('lists — keyed, ordered', () => {
  const a1 = { anchor: 'A1', name: 'a' };
  const a2 = { anchor: 'A2', name: 'b' };
  const a3 = { anchor: 'A3', name: 'c' };

  it('inserts at the index the key names', () => {
    expect(apply(asList, [a1], { '>1': { $new: a2 } })).toEqual([a1, a2]);
  });

  it('deletes the index the key names', () => {
    expect(apply(asList, [a1, a2], { '1>': { $old: a2 } })).toEqual([a1]);
  });

  it('updates in place when old and new index are the same', () => {
    expect(apply(asList, [a1], { '0': { name: { $old: 'a', $new: 'b' } } })).toEqual([{ anchor: 'A1', name: 'b' }]);
  });

  it('repositions on $unchanged, without touching the items', () => {
    expect(apply(asList, [a1, a2], { '1>0': '$unchanged', '0>1': '$unchanged' })).toEqual([a2, a1]);
  });

  it('repositions and updates in one key', () => {
    expect(apply(asList, [a1, a2], { '1>0': '$unchanged', '0>1': { name: { $old: 'a', $new: 'aa' } } })).toEqual([
      a2,
      { anchor: 'A1', name: 'aa' },
    ]);
  });

  it('truncates to the last still-referenced index, so a shrinking list leaves no tail', () => {
    // Three in, one deleted and one moved down into its place: length must fall to two.
    expect(apply(asList, [a1, a2, a3], { '1>': { $old: a2 }, '2>1': '$unchanged' })).toEqual([a1, a3]);
  });

  it('rejects a key that is neither an index nor a move', () => {
    expect(() => apply(asList, [], { '>': { $new: a1 } })).toThrow('Invalid patch key');
  });
});

describe('JSON_PATCHER — the configured experiment paths', () => {
  const base = {
    revision: 3,
    acl: [{ username: 'alice', level: 'ADMIN' }],
    attachments: [{ id: 'att-1', name: 'spectrum.pdf' }],
    model: {
      significantFigures: 5,
      reactions: [{ anchor: 'r-1', rxnfile: 'old', inputs: [{ anchor: 'i-1', chemicalName: 'toluene' }] }],
    },
  };

  it('walks into the nested reaction lists a scheme edit touches', () => {
    const [updated] = JSON_PATCHER.apply(base, {
      model: {
        reactions: {
          '0': {
            rxnfile: { $old: 'old', $new: 'new' },
            inputs: { '>1': { $new: { anchor: 'i-2', chemicalName: 'acetone' } } },
          },
        },
      },
    });

    expect(updated).toEqual({
      ...base,
      model: {
        significantFigures: 5,
        reactions: [
          {
            anchor: 'r-1',
            rxnfile: 'new',
            inputs: [
              { anchor: 'i-1', chemicalName: 'toluene' },
              { anchor: 'i-2', chemicalName: 'acetone' },
            ],
          },
        ],
      },
    });
  });

  it('keys /acl by username and /attachments by id', () => {
    const [updated] = JSON_PATCHER.apply(base, {
      acl: { alice: { level: { $old: 'ADMIN', $new: 'VIEW' } } },
      attachments: { 'att-1': { $old: { id: 'att-1', name: 'spectrum.pdf' } } },
    });

    expect(updated).toMatchObject({ acl: [{ username: 'alice', level: 'VIEW' }], attachments: [] });
  });

  it('reports every rebuilt object against the node it replaced', () => {
    const [updated, updatedNodes] = JSON_PATCHER.apply(base, {
      model: { reactions: { '0': { rxnfile: { $old: 'old', $new: 'new' } } } },
    });

    const reaction = (updated as typeof base).model.reactions[0];
    expect(updatedNodes.get(reaction)).toEqual(base.model.reactions[0]);
  });

  it('leaves revision alone — the backend never diffs it', () => {
    const [updated] = JSON_PATCHER.apply(base, { model: { significantFigures: { $old: 5, $new: 4 } } });
    expect((updated as typeof base).revision).toBe(3);
  });
});
