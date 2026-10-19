import { SavingOverlay } from '@/components/common/saving-overlay';
import { Menu, MenuContent, MenuItem, MenuTrigger } from '@/components/ui/menu';
import type { StoichiometryMutations } from '@/lib/hooks/experiments/use-stoichiometry-mutations';
import { cellId } from '@/lib/hooks/experiments/use-stoichiometry-mutations';
import type { ModelMutation } from '@/lib/types/mutations.ts';
import type { ColumnUnit, ExperimentModel, MolUnit, VolumeUnit, WeightUnit } from '@/lib/types/reactions.ts';
import { columnUnitLabel, MOL_UNITS, unitLabel, VOLUME_UNITS, WEIGHT_UNITS } from '@/lib/types/reactions.ts';
import { cn } from '@/lib/utils';

/**
 * The quantities whose unit the user picks, and what picking one sends. Experiment-wide, like
 * significant figures: the backend converts the values typed so far and recalculates the rest,
 * so the patch comes back with every affected cell already in the new unit.
 */
const PICKABLE: Partial<
  Record<
    ColumnUnit,
    {
      options: readonly string[];
      current: (model: ExperimentModel) => string;
      mutation: (unit: string) => ModelMutation;
    }
  >
> = {
  weight: {
    options: WEIGHT_UNITS,
    current: (model) => model.weightUnit,
    mutation: (unit) => ({ type: 'SetExperimentWeightUnit', weightUnit: unit as WeightUnit }),
  },
  volume: {
    options: VOLUME_UNITS,
    current: (model) => model.volumeUnit,
    mutation: (unit) => ({ type: 'SetExperimentVolumeUnit', volumeUnit: unit as VolumeUnit }),
  },
  mol: {
    options: MOL_UNITS,
    current: (model) => model.molUnit,
    mutation: (unit) => ({ type: 'SetExperimentMolUnit', molUnit: unit as MolUnit }),
  },
};

/**
 * A column's header text, with the unit its values are in after it — `Weight, g`.
 *
 * Given `mutations`, a pickable unit is a menu: the unit is underlined the way an editable cell's
 * number is, and choosing another sends the experiment-wide mutation. Without them — the products
 * and batches tables, the batch sub-headers, or an experiment that cannot be edited — the unit is
 * plain text, since the stoichiometry table's top-level header is the one place it is changed.
 */
export function ColumnHeader({
  header,
  unit,
  model,
  mutations,
}: {
  header: string;
  unit?: ColumnUnit;
  model: ExperimentModel;
  /** Present only where the unit may be changed. */
  mutations?: StoichiometryMutations;
}) {
  if (unit === undefined) return header;

  const pickable = PICKABLE[unit];
  if (!mutations || !pickable) return `${header}, ${columnUnitLabel(model, unit)}`;

  const cell = cellId('experiment', `${unit}Unit`);
  const current = pickable.current(model);

  return (
    <>
      {`${header}, `}
      <SavingOverlay pending={mutations.savingCells.has(cell)} spinner="center" className="inline-block">
        <Menu>
          {/*
            Named by its unit alone, so the header reads `Weight, mg` to assistive tech exactly as
            it does on screen; the column header around it is what says which quantity it is.
          */}
          <MenuTrigger
            className={cn(
              'rounded-2 underline decoration-dashed decoration-[0.5px] underline-offset-4 outline-none',
              'hover:bg-neutral-200 focus-visible:ring-3 focus-visible:ring-ring/50',
            )}
          >
            {unitLabel(current)}
          </MenuTrigger>
          <MenuContent align="start" className="min-w-[80px]">
            {pickable.options.map((option) => (
              <MenuItem
                key={option}
                onClick={() => {
                  if (option !== current) mutations.save(cell, pickable.mutation(option));
                }}
              >
                {unitLabel(option)}
              </MenuItem>
            ))}
          </MenuContent>
        </Menu>
      </SavingOverlay>
    </>
  );
}
