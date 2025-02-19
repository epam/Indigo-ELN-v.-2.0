import { twsx } from './twsx';

export const getRandomStr = () => Math.random().toString(36).slice(-8);

export const withSelector = (prefix: string, ...str: string[]) =>
  twsx(str.map((s) => `${prefix}:${s}`).join(' '));
