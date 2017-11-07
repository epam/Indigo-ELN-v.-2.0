stoichProductColumns.$inject = ['appUnits', 'calculationService'];

function stoichProductColumns(appUnits, calculationService) {
    return {
        chemicalName: {
            id: 'chemicalName',
            name: 'Chemical Name',
            type: 'input',
            hasStructurePopover: true
        },
        formula: {
            id: 'formula',
            name: 'Formula'
        },
        molWeight: {
            id: 'molWeight',
            name: 'Mol.Wt.',
            type: 'scalar',
            readonly: true
        },
        exactMass: {
            id: 'exactMass',
            name: 'Exact Mass',
            type: 'primitive'
        },
        weight: {
            id: 'weight',
            name: 'Theo. Wgt.',
            type: 'unit',
            unitItems: appUnits.grams,
            readonly: true
        },
        mol: {
            id: 'mol',
            name: 'Theo. Moles',
            type: 'unit',
            unitItems: appUnits.moles,
            isIntended: true,
            readonly: true
        },
        saltCode: {
            id: 'saltCode',
            name: 'Salt Code',
            type: 'select',
            values: appUnits.saltCodeValues,
            showDefault: true,
            onClose: function(data) {
                calculationService.setEntered(data);
                recalculateSalt(data.row);
                if (data.model.value === 0) {
                    data.row.saltEq.value = 0;
                }
            }

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
            }
        },
        hazardComments: {
            id: 'hazardComments',
            name: 'Hazard Comments'
        },
        eq: {
            id: 'eq',
            name: 'EQ',
            type: 'scalar'
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

module.exports = stoichProductColumns;
