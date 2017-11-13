stoichReactantsColumns.$inject =
    ['appUnits', 'stoichColumnActions', 'selectService', 'unitService', 'calculationService'];

function stoichReactantsColumns(appUnits, stoichColumnActions, selectService, unitService, calculationService) {
    return {
        compoundId: {
            id: 'compoundId',
            name: 'Compound ID',
            type: 'input',
            hasStructurePopover: true
        },
        casNumber: {
            id: 'casNumber',
            name: 'CAS Number',
            type: 'input'
        },
        chemicalName: {
            id: 'chemicalName',
            name: 'Chemical Name',
            type: 'input'
        },
        fullNbkBatch: {
            id: 'fullNbkBatch',
            name: 'Nbk Batch #',
            type: 'input',
            hasStructurePopover: true,
            hasPopup: true,
            popItemClick: function(row, item) {
                row.fullNbkBatch = item.details.fullNbkBatch;
                stoichColumnActions.populateFetchedBatch(row, item.details);
            },
            onChange: function(data) {
                var row = data.row;
                if (row) {
                    if (!row.$$fullNbkBatchOld && row.fullNbkImmutablePart) {
                        row.$$fullNbkBatchOld = data.oldVal;
                    }
                    var nbkBatch = data.model;
                    row.$$popItems = null;
                    row.$$populatedBatch = false;
                    stoichColumnActions.fetchBatchByNbkNumber(nbkBatch).then(function(result) {
                        if (result[0]) {
                            row.$$popItems = result.map(function(r) {
                                return {
                                    item: r,
                                    title: r.details.fullNbkBatch
                                };
                            });
                        } else {
                            stoichColumnActions.alertWrongFormat();
                        }
                    });
                }
            },
            onClose: function(data) {
                var row = data.row;
                var nbkBatch = data.model;
                if (!row.$$populatedBatch) {
                    if (row.fullNbkBatch) {
                        stoichColumnActions.fetchBatchByNbkNumber(nbkBatch).then(function(result) {
                            var pb = result[0];
                            if (pb && pb.details.fullNbkBatch === row.fullNbkBatch) {
                                stoichColumnActions.populateFetchedBatch(row, pb.details);
                            } else {
                                stoichColumnActions.alertWrongFormat();
                                row.fullNbkBatch = row.$$fullNbkBatchOld;
                            }
                        });
                    } else {
                        stoichColumnActions.alertWrongFormat();
                        row.fullNbkBatch = row.$$fullNbkBatchOld;
                    }
                }
            }
        },
        molWeight: {
            id: 'molWeight',
            name: 'Mol Weight',
            type: 'scalar'
        },
        weight: {
            id: 'weight',
            name: 'Weight',
            type: 'unit',
            unitItems: appUnits.grams,
            actions: unitService.getActions('Weight', appUnits.grams)
        },
        volume: {
            id: 'volume',
            name: 'Volume',
            type: 'unit',
            unitItems: appUnits.liters
        },
        mol: {
            id: 'mol',
            name: 'Mol',
            type: 'unit',
            unitItems: appUnits.moles,
            actions: unitService.getActions('Mol', appUnits.moles)
        },
        eq: {
            id: 'eq',
            name: 'EQ',
            type: 'scalar',
            sigDigits: 2
        },
        limiting: {
            id: 'limiting',
            name: 'Limiting',
            type: 'radio'
        },
        rxnRole: {
            id: 'rxnRole',
            name: 'Rxn Role',
            type: 'select',
            values: appUnits.rxnValues
        },
        density: {
            id: 'density',
            name: 'Density',
            type: 'unit',
            unitItems: appUnits.density,
            actions: unitService.getActions('Density', appUnits.density)
        },
        molarity: {
            id: 'molarity',
            name: 'Molarity',
            type: 'unit',
            unitItems: appUnits.molarity,
            actions: unitService.getActions('Molarity', appUnits.molarity)
        },
        stoicPurity: {
            id: 'stoicPurity',
            name: 'Purity',
            type: 'scalar'
        },
        formula: {
            id: 'formula',
            name: 'Mol Formula',
            type: 'input'
        },
        saltCode: {
            id: 'saltCode',
            name: 'Salt Code',
            type: 'select',
            values: appUnits.saltCodeValues,
            onClose: function(data) {
                calculationService.setEntered(data);
                recalculateSalt(data.row);
                if (data.row.saltCode.value === 0) {
                    data.row.saltEq.value = 0;
                }
            },
            showDefault: true,
            actions: selectService.getActions('Salt Code', appUnits.saltCodeValues)
        },
        saltEq: {
            id: 'saltEq',
            name: 'Salt EQ',
            type: 'scalar',
            checkEnabled: function(o) {
                return o.saltCode && o.saltCode.value > 0;
            },
            onClose: function(data) {
                calculationService.setEntered(data);
                recalculateSalt(data.row);
                if (data.row.saltCode.value === 0) {
                    data.row.saltEq.value = 0;
                }
            }
        },
        loadFactor: {
            id: 'loadFactor',
            name: 'Load Factor',
            type: 'unit',
            unitItems: appUnits.loadFactorUnits,
            actions: unitService.getActions('Load Factor', appUnits.loadFactorUnits)
        },
        hazardComments: {
            id: 'hazardComments',
            name: 'Hazard Comments',
            type: 'input'
        },
        comments: {
            id: 'comments',
            name: 'Comments',
            type: 'input'
        }
    };

    function recalculateSalt(reagent) {
        calculationService.recalculateSalt(reagent).then(function() {
            calculationService.recalculateAmounts({
                row: reagent
            });
        });
    }
}

module.exports = stoichReactantsColumns;
