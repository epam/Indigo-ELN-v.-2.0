type JSONNode =
  | null
  | boolean
  | number
  | string
  | JSONNode[]
  | { [key: string]: JSONNode };

// eslint-disable-next-line
type JSONObject = { [key: string]: JSONNode };

// Counterpart of backend's JSONPatcher.java, see its usage and tests for ddetails
export class JSONPatcher {

  updatedNodes = new Map<unknown, unknown>();

  setPaths: Record<string, string>;
  listPaths: Record<string, string>;

  constructor(setPaths: Record<string, string>, listPaths: Record<string, string>) {
    this.setPaths = setPaths;
    this.listPaths = listPaths;
  }

  apply(base: unknown, patch: unknown): [unknown, Map<unknown, unknown>] {
    this.updatedNodes = new Map();
    const updated = this.doApply(structuredClone(base) as JSONNode, patch as JSONNode, "");
    return [updated, this.updatedNodes];
  }

  private doApply(base: JSONNode, patch: JSONNode, path: string): JSONNode {
    if (patch == null) { // unchanged
      return base;
    }
    if (typeof patch !== 'object' || Array.isArray(patch)) {
      throw Error('patch expected to be an object');
    }
    let result: JSONNode;
    if ('$old' in patch || '$new' in patch) {
      result = '$new' in patch ? patch['$new'] : null;
    } else if (path in this.setPaths) {
      result = this.doRestoreSet(base as JSONObject[], patch, path);
    } else if (path in this.listPaths) {
      result = this.doRestoreList(base as JSONObject[], patch, path);
    } else {
      result = this.doRestoreObject(base as JSONObject, patch, path);
    }
    if (result != null && typeof result === 'object' && !Array.isArray(result)) {
      this.updatedNodes.set(result, base as JSONObject);
    }
    return result;
  }

  private doRestoreObject(base: JSONObject | null, patch: JSONNode, path: string): JSONNode {
    const target = base != null ? { ...base } : {};
    for (const [key, value] of Object.entries(patch)) {
      const oldValue = base != null && key in base ? base[key] : null;
      const newValue = this.doApply(oldValue, value, path + '/' + key);
      if (newValue == null) {
        delete target[key];
      } else {
        target[key] = newValue;
      }
    }
    return target;
  }

  private doRestoreSet(base: JSONObject[] | null, patch: JSONObject, path: string): JSONObject[] | null {
    const target: JSONObject[] = base != null ? [...base] : [];
    const keyProperty = this.setPaths[path];

    const keyIndices = new Map<JSONNode, number>();
    for (let i = 0; i < target.length; i++) {
      keyIndices.set(target[i][keyProperty], i);
    }

    for (const [key, itemPatch] of Object.entries(patch)) {
      const index = keyIndices.get(key);
      const newValue = this.doApply(index !== undefined ? target[index] : null, itemPatch, path + '/#') as JSONObject;
      if (index !== undefined) {
        target[index] = newValue;
      } else {
        target.push(newValue);
      }
    }

    return target.filter(item => item !== null);
  }

  private doRestoreList(base: JSONObject[] | null, patch: JSONObject, path: string): JSONObject[] {
    const source: JSONObject[] = base != null ? base : [];
    const target: JSONObject[] = [...source];

    const referenceCount: number[] = new Array(source.length + Object.keys(patch).length).fill(0);
    for (let i = 0; i < source.length; i++) {
      referenceCount[i]++;
    }

    for (const [key, itemPatch] of Object.entries(patch)) {
      const [oldIndex, newIndex] = this.parseListKey(key);
      if (oldIndex === -1) { // new item
        if (newIndex === -1) throw new Error('Invalid patch key');
        target[newIndex] = this.doApply(null, itemPatch, path + '/#') as JSONObject;
        referenceCount[newIndex]++;
      } else if (newIndex === -1) { // deleted item
        referenceCount[oldIndex]--;
      } else { // updated and/or repositioned item
        const oldValue = source[oldIndex];
        const newValue = itemPatch === '$unchanged' ? oldValue : this.doApply(oldValue, itemPatch, path + '/#') as JSONObject;
        target[newIndex] = newValue;
        referenceCount[oldIndex]--;
        referenceCount[newIndex]++;
      }
    }

    let lastReferencedIndex = -1;
    for (let i = referenceCount.length - 1; i >= 0; i--) {
      if (referenceCount[i] > 0) {
        lastReferencedIndex = i;
        break;
      }
    }

    return target.slice(0, lastReferencedIndex + 1);
  }

  private parseListKey(str: string): [number, number] {
    const p = str.indexOf('>');
    if (p === -1) {
      const v = parseInt(str, 10);
      return [v, v];
    }
    const oldIndex = p > 0 ? parseInt(str.substring(0, p), 10) : -1;
    const newIndex = p < str.length - 1 ? parseInt(str.substring(p + 1), 10) : -1;
    return [oldIndex, newIndex];
  }
}

export const JSON_PATCHER = new JSONPatcher(
  {
    '/attachments': 'id',
    '/acl': 'username',
  },
  {
    '/model/reactions': 'anchor',
    '/model/reactions/#/inputs': 'anchor',
    '/model/reactions/#/inputs/#/samples': 'anchor',
    '/model/reactions/#/outputs': 'anchor',
    '/model/reactions/#/outputs/#/samples': 'anchor',
  },
);
