import { DropdownMenuItem } from '@/core/components/common/dropdown-menu/dropdown-menu.i';

export const SIGNIFICANT_FIGURES: ReadonlyArray<DropdownMenuItem> = [
  { label: '1', value: '1' },
  { label: '2', value: '2' },
  { label: '3', value: '3' },
  { label: '4', value: '4' },
  { label: '5', value: '5' },
] as const;
