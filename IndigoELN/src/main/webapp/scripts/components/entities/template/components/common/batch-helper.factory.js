(function() {
    angular
        .module('indigoeln')
        .factory('batchHelper', batchHelper);

    batchHelper.$inject = ['appUnits', 'CalculationService'];

    function batchHelper(appUnits, CalculationService) {
        var columnCloseFunction = {
            totalWeight: onClose1,
            totalVolume: onClose1,
            mol: onClose1,
            saltCode: onCloseSaltCode,
            onCloseSaltEq: onCloseSaltEq,
            $$batchType: onCloseBatchType
        };

        function close(column, data) {
            var fn = columnCloseFunction[column.id];

            if (fn) {
                fn(data);
            }
        }

        function onClose1(data) {
            CalculationService.setEntered(data);
            CalculationService.calculateProductBatch(data);
        }

        function onCloseSaltCode(data) {
            CalculationService.setEntered(data);
            recalculateSalt(data.row);
            if (data.model.value === 0) {
                data.row.saltEq.value = 0;
            }
        }

        function onCloseSaltEq(data) {
            CalculationService.setEntered(data);
            recalculateSalt(data.row);
        }

        function onCloseBatchType(data) {
            var r = data.row;
            if (!r.$$batchType) {
                return;
            }
            r.batchType = r.$$batchType.name;
        }

        function recalculateSalt(reagent) {
            CalculationService.recalculateSalt(reagent).then(function() {
                CalculationService.recalculateStoich();
            });
        }

        return {
            close: close,
            columns: {
                structure: {
                    id: 'structure',
                    name: 'Structure',
                    type: 'image',
                    isVisible: false
                },
                nbkBatch: {
                    id: 'nbkBatch',
                    name: 'Nbk Batch #'
                },
                registrationStatus: {
                    id: 'registrationStatus',
                    name: 'Registration Status',
                    type: 'registrationStatus'
                },
                totalWeight: {
                    id: 'totalWeight',
                    name: 'Total Weight',
                    type: 'unit',
                    width: '150px',
                    unitItems: appUnits.grams
                },
                totalVolume: {
                    id: 'totalVolume',
                    name: 'Total Volume',
                    type: 'unit',
                    width: '150px',
                    unitItems: appUnits.liters
                },
                mol: {
                    id: 'mol',
                    name: 'Total Moles',
                    type: 'unit',
                    width: '150px',
                    unitItems: appUnits.moles
                },
                theoWeight: {
                    id: 'theoWeight',
                    name: 'Theo. Wgt.',
                    type: 'unit',
                    unitItems: appUnits.grams,
                    width: '150px',
                    hideSetValue: true,
                    readonly: true
                },
                theoMoles: {
                    id: 'theoMoles',
                    name: 'Theo. Moles',
                    width: '150px',
                    type: 'unit',
                    unitItems: appUnits.moles,
                    hideSetValue: true,
                    readonly: true
                },
                yield: {
                    id: 'yield',
                    name: '%Yield',
                    readonly: true
                },
                compoundState: {
                    id: 'compoundState',
                    name: 'Compound State',
                    type: 'select',
                    dictionary: 'Compound State',
                    values: null
                },
                saltCode: {
                    id: 'saltCode',
                    name: 'Salt Code',
                    type: 'select',
                    showDefault: true,
                    values: appUnits.saltCodeValues
                },
                compoundProtection: {
                    id: 'compoundProtection',
                    name: 'Compound Protection',
                    type: 'select',
                    values: appUnits.compoundProtectionValues
                },
                structureComments: {
                    id: 'structureComments',
                    name: 'Structure Comments',
                    type: 'input',
                    bulkAssignment: true
                },
                registrationDate: {
                    id: 'registrationDate',
                    name: 'Registration Date',
                    type: 'date'
                },
                comments: {
                    id: 'comments',
                    name: 'Batch Comments',
                    type: 'input',
                    bulkAssignment: true
                },
                $$batchType: {
                    id: '$$batchType',
                    name: 'Intermediate/Test Compound',
                    type: 'select',
                    values: [
                        {name: 'Intermediate'},
                        {name: 'Test Compound'}
                    ]
                },
                molWeight: {
                    id: 'molWeight',
                    name: 'Mol Wgt',
                    type: 'scalar'
                },
                formula: {
                    id: 'formula',
                    name: 'Mol Formula',
                    type: 'input',
                    readonly: true
                },
                conversationalBatchNumber: {
                    id: 'conversationalBatchNumber',
                    name: 'Conversational Batch #'
                },
                virtualCompoundId: {
                    id: 'virtualCompoundId',
                    name: 'Virtual Compound Id',
                    type: 'input'
                },
                stereoisomer: {
                    id: 'stereoisomer',
                    name: 'Stereoisomer Code',
                    type: 'select',
                    dictionary: 'Stereoisomer Code',
                    hasCustomItemProp: true,
                    values: null,
                    width: '350px'
                },
                saltEq: {
                    id: 'saltEq',
                    name: 'Salt EQ',
                    type: 'scalar',
                    bulkAssignment: true,
                    checkEnabled: function(o) {
                        return o.saltCode && o.saltCode.value > 0;
                    }
                },
                precursors: {
                    id: 'precursors',
                    name: 'Precursor/Reactant IDs',
                    type: 'input',
                    readonly: true
                }
            }
        };
    }
})();
