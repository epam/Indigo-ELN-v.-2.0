stoichProductColumns.$inject = ['appUnits', 'calculationService'];

function stoichProductColumns(appUnits) {
    return {
        chemicalName: {
            id: 'chemicalName',
            name: 'Chemical Name',
            type: 'input',
            hasStructurePopover: true
        },
        formula: {
            id: 'formula',
            name: 'Formula',
            type: 'formula',
            readonly: true
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
        theoWeight: {
            id: 'theoWeight',
            name: 'Theo. Wgt.',
            type: 'unit',
            unitItems: appUnits.grams,
            readonly: true
        },
        theoMoles: {
            id: 'theoMoles',
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
            showDefault: true
        },
        saltEq: {
            id: 'saltEq',
            name: 'Salt EQ',
            type: 'scalar',
            checkEnabled: function(o) {
                return o.saltCode && o.saltCode.value > 0;
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
}

module.exports = stoichProductColumns;
