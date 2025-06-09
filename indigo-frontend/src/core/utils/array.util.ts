export function ensureDistinct(array: unknown[], property: string) {
  const uniqueMap = new Map();
  array.forEach((obj) => {
    uniqueMap.set(obj[property], obj);
  });
  return Array.from(uniqueMap.values());
}
