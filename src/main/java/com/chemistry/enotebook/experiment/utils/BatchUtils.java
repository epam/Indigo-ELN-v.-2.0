package com.chemistry.enotebook.experiment.utils;

import com.chemistry.enotebook.domain.AmountModel;

public class BatchUtils {

    public static boolean isUnitOnlyChanged(AmountModel from, AmountModel to) {
        boolean result;
        result = (CeNNumberUtils.doubleEquals(from.GetValueInStdUnitsAsDouble(), to.GetValueInStdUnitsAsDouble()) && !(from
                .getUnit().equals(to.getUnit())));

        return result;
    }

    public static double calcMolesWithEquivalents(AmountModel reagentMoles, AmountModel equiv) {
        if (reagentMoles.GetValueInStdUnitsAsDouble() == 0.0) {
            return reagentMoles.GetValueInStdUnitsAsDouble();
        }
        return reagentMoles.GetValueInStdUnitsAsDouble() * equiv.GetValueInStdUnitsAsDouble();

    }

    public static double calcEquivalentsWithMoles(AmountModel moles, AmountModel reagentMoles) {
        if (reagentMoles.GetValueInStdUnitsAsDouble() == 0.0) {
            return moles.GetValueInStdUnitsAsDouble();
        }
        return moles.GetValueInStdUnitsAsDouble() / reagentMoles.GetValueInStdUnitsAsDouble();
    }

}
