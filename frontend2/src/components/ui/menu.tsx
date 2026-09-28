import { Menu as MenuPrimitive } from '@base-ui/react/menu';
import { Check } from 'lucide-react';

import { cn } from '@/lib/utils';

const MenuRoot = MenuPrimitive.Root;
const MenuTrigger = MenuPrimitive.Trigger;

/**
 * The portalled popup and its positioner. Styling matches the sort dropdown in
 * `src/components/common/action-bar.tsx`, which builds the same thing inline.
 */
function MenuContent({
  className,
  children,
  sideOffset = 4,
  align = 'end',
  ...props
}: MenuPrimitive.Popup.Props & Pick<MenuPrimitive.Positioner.Props, 'sideOffset' | 'align'>) {
  return (
    <MenuPrimitive.Portal>
      <MenuPrimitive.Positioner sideOffset={sideOffset} align={align} className="z-50">
        <MenuPrimitive.Popup
          data-slot="menu-content"
          className={cn(
            'min-w-40 rounded-md border border-neutral-300 bg-popover p-1 shadow-card outline-none',
            className,
          )}
          {...props}
        >
          {children}
        </MenuPrimitive.Popup>
      </MenuPrimitive.Positioner>
    </MenuPrimitive.Portal>
  );
}

function MenuItem({ className, ...props }: MenuPrimitive.Item.Props) {
  return (
    <MenuPrimitive.Item
      data-slot="menu-item"
      className={cn(
        'flex cursor-default items-center gap-2 rounded-2 px-3 py-2 text-[14px]/6 text-neutral-1000 outline-none data-highlighted:bg-blue-10',
        className,
      )}
      {...props}
    />
  );
}

/**
 * A menu item that toggles. Base UI leaves `closeOnClick` false here, which is what a
 * multi-select filter wants: the popup stays open while several are ticked.
 *
 * The indicator keeps its box when unticked (`invisible`, not unmounted), so labels line up
 * down the column whatever is selected.
 */
function MenuCheckboxItem({ className, children, ...props }: MenuPrimitive.CheckboxItem.Props) {
  return (
    <MenuPrimitive.CheckboxItem
      data-slot="menu-checkbox-item"
      className={cn(
        'flex cursor-default items-center gap-2 rounded-2 px-3 py-2 text-[14px]/6 text-neutral-1000 outline-none data-highlighted:bg-blue-10',
        className,
      )}
      {...props}
    >
      <MenuPrimitive.CheckboxItemIndicator
        keepMounted
        className="flex size-4 shrink-0 items-center justify-center data-unchecked:invisible"
      >
        <Check className="size-4 text-blue-400" />
      </MenuPrimitive.CheckboxItemIndicator>
      {children}
    </MenuPrimitive.CheckboxItem>
  );
}

export { MenuRoot as Menu, MenuTrigger, MenuContent, MenuItem, MenuCheckboxItem };
