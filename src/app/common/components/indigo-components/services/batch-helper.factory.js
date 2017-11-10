batchHelper.$inject = ['appUnits', 'calculationService', 'columnActions', 'scalarService', 'unitService',
    'selectService', 'inputService', '$q'];

function batchHelper(appUnits, calculationService, columnActions, scalarService, unitService, selectService,
                     inputService, $q) {
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
        calculationService.setEntered(data);
        calculationService.calculateProductBatch(data);
    }

    function onCloseSaltCode(data) {
        calculationService.setEntered(data);
        recalculateSalt(data.row);
        if (data.model.value === 0) {
            data.row.saltEq.value = 0;
        }
    }

    function onCloseSaltEq(data) {
        calculationService.setEntered(data);
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
        calculationService.recalculateSalt(reagent).then(function() {
            calculationService.recalculateStoich();
        });
    }

    var compounds = [
        {name: 'Intermediate'},
        {name: 'Test Compound'}
    ];

    return {
        close: close,
        hasCheckedRow: hasCheckedRow,
        getCheckedBatches: getCheckedBatches,
        compounds: compounds,
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
                unitItems: appUnits.grams,
                actions: unitService.getActions('Total Weight', appUnits.grams)
            },
            totalVolume: {
                id: 'totalVolume',
                name: 'Total Volume',
                type: 'unit',
                width: '150px',
                unitItems: appUnits.liters,
                actions: unitService.getActions('Total Volume', appUnits.liters)
            },
            mol: {
                id: 'mol',
                name: 'Total Moles',
                type: 'unit',
                width: '150px',
                unitItems: appUnits.moles,
                actions: unitService.getActions('Total Moles', appUnits.moles)
            },
            theoWeight: {
                id: 'theoWeight',
                name: 'Theo. Wgt.',
                type: 'unit',
                unitItems: appUnits.grams,
                width: '150px',
                hideSetValue: true,
                readonly: true,
                actions: unitService.getActions('Theo. Wgt.', appUnits.grams)
            },
            theoMoles: {
                id: 'theoMoles',
                name: 'Theo. Moles',
                width: '150px',
                type: 'unit',
                unitItems: appUnits.moles,
                hideSetValue: true,
                readonly: true,
                actions: unitService.getActions('Theo. Moles', appUnits.moles)
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
                values: null,
                actions: selectService.getActions('Compound State', null, 'Compound State')
            },
            saltCode: {
                id: 'saltCode',
                name: 'Salt Code',
                type: 'select',
                showDefault: true,
                values: appUnits.saltCodeValues,
                actions: selectService.getActions('Salt Code', appUnits.saltCodeValues)
            },
            compoundProtection: {
                id: 'compoundProtection',
                name: 'Compound Protection',
                type: 'select',
                values: appUnits.compoundProtectionValues,
                actions: selectService.getActions('Compound Protection', appUnits.compoundProtectionValues)
            },
            structureComments: {
                id: 'structureComments',
                name: 'Structure Comments',
                type: 'input',
                bulkAssignment: true,
                actions: inputService.getActions('Structure Comments')
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
                bulkAssignment: true,
                actions: inputService.getActions('Batch Comments')
            },
            $$batchType: {
                id: '$$batchType',
                name: 'Intermediate/Test Compound',
                type: 'select',
                values: compounds,
                actions: selectService.getActions('Intermediate/Test Compound', compounds)
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
                readonly: true,
                actions: inputService.getActions('Mol Formula')
            },
            conversationalBatchNumber: {
                id: 'conversationalBatchNumber',
                name: 'Conversational Batch #'
            },
            virtualCompoundId: {
                id: 'virtualCompoundId',
                name: 'Virtual Compound Id',
                type: 'input',
                actions: inputService.getActions('Virtual Compound Id')
            },
            stereoisomer: {
                id: 'stereoisomer',
                name: 'Stereoisomer Code',
                type: 'select',
                dictionary: 'Stereoisomer Code',
                hasCustomItemProp: true,
                values: null,
                width: '350px',
                actions: selectService.getActions('Stereoisomer Code', null, 'Stereoisomer Code')
            },
            saltEq: {
                id: 'saltEq',
                name: 'Salt EQ',
                type: 'scalar',
                bulkAssignment: true,
                checkEnabled: function(o) {
                    return o.saltCode && o.saltCode.value > 0;
                },
                actions: [
                    {
                        name: 'Set value for scalar',
                        title: 'scalar',
                        action: function(rows, column) {
                            scalarService.action(rows, 'scalar', column)
                                .then(function(promises) {
                                    return $q.all(promises).then(calculationService.recalculateStoich);
                                });
                        }
                    }
                ]
            },
            precursors: {
                id: 'precursors',
                name: 'Precursor/Reactant IDs',
                type: 'input',
                readonly: true,
                actions: inputService.getActions('Precursor/Reactant IDs')
            },
            select: {
                id: '$$select',
                name: 'Select',
                type: 'boolean',
                noDisableable: true,
                noDirty: true,
                actions: [
                    {
                        name: 'Select All',
                        action: function(rows) {
                            _.each(rows, function(row) {
                                row.$$select = true;
                            });
                        }
                    },
                    {
                        name: 'Deselect All',
                        action: function(rows) {
                            _.each(rows, function(row) {
                                row.$$select = false;
                            });
                        }
                    }
                ]
            },
            purity: {
                id: '$$purity',
                realId: 'purity',
                name: 'Purity',
                type: 'string',
                onClick: function(data) {
                    columnActions.editPurity(data.row);
                },
                actions: [{
                    name: 'Set value for Purity',
                    title: 'Purity',
                    action: function(rows) {
                        columnActions.editPurityForAllRows(rows);
                    }
                }]
            },
            $$meltingPoint: {
                id: '$$meltingPoint',
                realId: 'meltingPoint',
                name: 'Melting Point',
                type: 'string',
                onClick: function(data) {
                    columnActions.editMeltingPoint(data.row);
                },
                actions: [{
                    name: 'Set value for Melting Point',
                    title: 'Melting Point',
                    action: function(rows) {
                        columnActions.editMeltingPointForAllRows(rows);
                    }
                }]
            },
            source: {
                id: 'source',
                name: 'Source',
                type: 'select',
                dictionary: 'Source',
                hideSelectValue: true,
                actions: [{
                    name: 'Set value for Source',
                    title: 'Source',
                    action: function(rows) {
                        columnActions.openProductBatchSummaryModal(rows, this.title);
                    }
                }]
            },
            sourceDetail: {
                id: 'sourceDetail',
                name: 'Source Detail',
                type: 'select',
                dictionary: 'Source Details',
                hideSelectValue: true,
                actions: [{
                    name: 'Set value for Source Detail',
                    title: 'Source Detail',
                    action: function(rows) {
                        columnActions.openProductBatchSummaryModal(rows, this.title);
                    }
                }]
            },
            $$externalSupplier: {
                id: '$$externalSupplier',
                realId: 'externalSupplier',
                name: 'External Supplier',
                type: 'string',
                onClick: function(data) {
                    columnActions.editExternalSupplier(data.row);
                },
                actions: [{
                    name: 'Set value for External Supplier',
                    title: 'External Supplier',
                    action: columnActions.editExternalSupplierForAllRows
                }]
            },
            $$healthHazards: {
                id: '$$healthHazards',
                realId: 'healthHazards',
                name: 'Health Hazards',
                type: 'string',
                onClick: function(data) {
                    columnActions.editHealthHazards(data.row);
                },
                actions: [{
                    name: 'Set value for Health Hazards',
                    title: 'Health Hazards',
                    action: columnActions.editHealthHazardsForAllRows
                }]
            },
            $$residualSolvents: {
                id: '$$residualSolvents',
                realId: 'residualSolvents',
                name: 'Residual Solvents',
                type: 'string',
                onClick: function(data) {
                    columnActions.editResidualSolvents([data.row]);
                },
                actions: [{
                    name: 'Set value for Residual Solvents',
                    title: 'Residual Solvents',
                    action: function(rows) {
                        columnActions.editResidualSolvents(rows);
                    }
                }]
            },
            $$solubility: {
                id: '$$solubility',
                realId: 'solubility',
                name: 'Solubility in Solvents',
                type: 'string',
                onClick: function(data) {
                    columnActions.editSolubility([data.row]);
                },
                actions: [{
                    name: 'Set value for Solubility in Solvents',
                    title: 'Solubility in Solvents',
                    action: function(rows) {
                        columnActions.editSolubility(rows);
                    }
                }]
            },
            $$storageInstructions: {
                id: '$$storageInstructions',
                realId: 'storageInstructions',
                name: 'Storage Instructions',
                type: 'string',
                onClick: function(data) {
                    columnActions.editStorageInstructions([data.row]);
                },
                actions: [{
                    name: 'Set value for Storage Instructions',
                    title: 'Storage Instructions',
                    action: function(rows) {
                        columnActions.editStorageInstructions(rows);
                    }
                }]
            },
            $$handlingPrecautions: {
                id: '$$handlingPrecautions',
                realId: 'handlingPrecautions',
                name: 'Handling Precautions',
                type: 'string',
                onClick: function(data) {
                    columnActions.editHandlingPrecautions([data.row]);
                },
                actions: [{
                    name: 'Set value for Handling Precautions',
                    title: 'Handling Precautions',
                    action: function(rows) {
                        columnActions.editHandlingPrecautions(rows);
                    }
                }]
            }
        }
    };

    function hasCheckedRow(batches) {
        return _.some(batches, function(item) {
            return item.$$select;
        });
    }

    function getCheckedBatches(batches) {
        return _.filter(batches, function(batch) {
            return batch.$$select;
        });
    }
}

module.exports = batchHelper;
