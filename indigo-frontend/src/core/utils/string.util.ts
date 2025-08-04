import { capitalize } from 'lodash';

export const getRandomStr = () => Math.random().toString(36).slice(-8);

export function normalizeLabel(key: string): string {
    return capitalize(key.toLowerCase().replaceAll("_", " "));
}
