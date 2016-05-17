angular.module('indigoeln')
    .factory('StoichCalculator', function (NumberUtil) {
        var isEditable = true;
        var autoCalcOn = true;

        var recalculateStoich = function (data) {
        };

        function isSolventReactionRole(reagent) {
            return reagent.rxnRole && reagent.rxnRole.name === 'SOLVENT';
        }

        var recalculateStoichBasedOnBatch = function (data, calcMolesOnly) {
            var reagent = data.row;
            if (reagent) {
                // see if this batch(List) is qualified as LimitReag
                if (reagent.limiting === true && !canBeLimiting(reagent)) {
                    reagent.limiting = false;
                }
                // find limiting reagent
                var limitingReag = findLimitingReagent(data);
                if (!limitingReag && autoCalcOn) {
                    limitingReag = setLimitingReagent(data);
                }
                if (isSolventReactionRole(reagent)) {
                    recalculateSolventAmounts(reagent, limitingReag, data);
                    // to recalculate Molarity or Volume
                }
                // vb 5/8 limitingBatch is null if the user hasn't entered stoic info and is in
                if (limitingReag) {
                    if (NumberUtil.floatEquals(limitingReag.eq.value, 0.0, 4)) {
                        resetRxnEquivs(limitingReag); // Puts back to 1.0
                    }
                    // Catalyst to get calcs going in case limiting reag isn't filled in.
                    // Make sure the limiting reagent is set and has a value.
                    if (!limitingReag.mol) {
                        recalculateAmountsForBatch(reagent, limitingReag, calcMolesOnly);
                    }

                    // Update Molarity for all solvents, if moles of limiting reagent is modified
                    recalculateMolarAmountForSolvent(limitingReag.mol);
                    var reactants = getReagentBatches(data);
                    findAndRecalculateVolumeForSolvents(reactants, limitingReag.mol, null);

                    // unset any other limiting reagent flag
                    if (reagent.limiting === true) {
                        clearLimitingReagentFlags(reagent);
                    }
                    // re calculate the moles of all the other batches in stoic based on limit reag moles
                    recalculateMolesBasedOnLimitingReagent(reagent);

                    recalculateProductsBasedOnStoich();
                }
                updateMonomerAndProductListLevelFlags();
            }
        };

        var recalculateProductsBasedOnStoich = function () {

        };

        var isReactionEquivMeaningless = function (reagent) {
            // if only Volume is set for it then don't display eq value
            return !reagent.volume && reagent.mol && reagent.eq && reagent.density && reagent.molarity;
        };
        var canBeLimiting = function (reagent) {
            return !(isSolventReactionRole(reagent) || isReactionEquivMeaningless(reagent));
        };
        var findLimitingReagent = function (data) {
            // Find currently already marked limiting reagent in the list.
            var result;
            _.each(data.stoichTable.reactants, function (reagent) {
                if (reagent.limiting === true && canBeLimiting(reagent)) {
                    result = reagent;
                }
            });
            return result;
        };
        var setLimitingReagent = function (data) {
            // Find current limiting reagent.Identify new limiting reagent if there is none currently.
            // lowest mole value batch(List) is selected as Limiting Reagent and set equivs to 1.0 if current value is zero
            var result;
            if (isEditablePage()) {
                result = findLimitingReagent();
                if (!result) {
                    _.each(data.stoichTable.reactants, function (test) {
                        test.limiting = false;
                        if (canBeLimiting(test) && !result || (test.mol && result.mol && test.mol.value < result.mol.value)) {
                            result = test;
                        }
                    });
                    if (result) {
                        result.limiting = true;
                    }

                }
            }
            return result;

        };
        var recalculateSolventAmounts = function (reagent, limitingReag, data) {
            if (!limitingReag) {
                return;
            }
            var molarAmount = reagent.molarity;
            var volumeAmount = reagent.volume;
            var volumeAmountList = getVolumeAmountsForAllSolvents(data);
            if (molarAmount === 0.0 && volumeAmount === 0.0) {
                return;
            }
            var reagents = getReagentBatches(data);
            _.each(reagents, function (reag) {
                // if solvent go ahead
                if (isSolventReactionRole(reag)) {

                }// end of if (b.getType().equals(BatchType.SOLVENT))
            });


        };
        var recalculateAmountsForBatch = function () {

        };
        var resetRxnEquivs = function () {

        };
        var clearLimitingReagentFlags = function () {

        };
        var recalculateMolesBasedOnLimitingReagent = function () {

        };
        var recalculateMolarAmountForSolvent = function (data) {

        };
        var findAndRecalculateVolumeForSolvents = function () {

        };
        var isEditablePage = function () {
            return isEditable;
        };
        var updateActualProductTheoAmounts = function () {

        };
        var recalculateProductAmounts = function () {

        };
        var getVolumeAmountsForAllSolvents = function (data) {
            var reagents = getReagentBatches(data);
            var list = [];
            _.each(reagents, function (reagent) {
                if (isSolventReactionRole(reagent)) {
                    list.push(reagent.volume);
                }
            });
            return list;
        };
        var updateMolarAmountsForSolvent = function () {

        };
        var areAllVolumesUserEnteredForSovents = function () {

        };
        var recalculateVolumeForSolvent = function () {

        };
        var applySigDigitsToAllSolventMolarites = function () {

        };
        var getMolarAmountsForAllSolvents = function () {

        };
        var recalculateMoleAmountForBatch = function () {

        };
        var recalculateRxnEquivsForBatch = function () {

        };
        var insertBatchIntoAdditionOrder = function () {

        };
        var updateAdditionOrder = function () {

        };
        var removeBatchFromStep = function () {

        };
        var addProductIfNeeded = function () {

        };
        var createIntendedProduct = function () {

        };
        var getPreCursorsForReaction = function () {

        };
        var updateIntendedProductPrecursors = function () {

        };
        var recalculateRxnEquivsBasedOnLimitingReagentMoles = function () {

        };
        var updateMonomerAndProductListLevelFlags = function () {

        };
        var getReagentBatches = function (data) {
            return data.stoichTable.reactants;
        };

        return {
            recalculateStoich: recalculateStoich,
            recalculateStoichBasedOnBatch: recalculateStoichBasedOnBatch,
            recalculateProductsBasedOnStoich: recalculateProductsBasedOnStoich,
            canBeLimiting: canBeLimiting,
            findLimitingReagent: findLimitingReagent,
            recalculateMolarAmountForSolvent: recalculateMolarAmountForSolvent,
            recalculateProductAmounts: recalculateProductAmounts,
            createIntendedProduct: createIntendedProduct,
            getPreCursorsForReaction: getPreCursorsForReaction
        };
    });
