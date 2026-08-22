import { Menu as MenuPrimitive } from '@base-ui/react/menu';

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
            'min-w-[160px] rounded-md border border-neutral-300 bg-popover p-1 shadow-card outline-none',
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
        'flex cursor-default items-center gap-2 rounded-2 px-3 py-2 text-[14px]/6 text-neutral-1000 outline-none data-[highlighted]:bg-blue-10',
        className,
      )}
      {...props}
    />
  );
}

export { MenuRoot as Menu, MenuTrigger, MenuContent, MenuItem };
