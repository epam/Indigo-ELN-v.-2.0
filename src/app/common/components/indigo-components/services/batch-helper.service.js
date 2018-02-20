var BatchRow = require('../directives/indigo-components/domain/batch-row/calculation-row/batch-row');

/* @ngInject */
function batchHelper(appUnits, appValuesService, columnActions, batchesCalculation,
                     calculationHelper, scalarService, unitService, selectService, setInputService, $q) {
    function onBatchChanged(change) {
        var batchesData = {
            changedRow: change.row,
            changedField: change.column
        };

        calculateRow(batchesData);
    }

    function calculateAllRows(batchesData) {
        var preparedBatchesData = prepareBatchesForCalculation(batchesData);
        var calculatedRows = batchesCalculation.calculateAllRows(preparedBatchesData);

        calculationHelper.updateViewRows(calculatedRows, batchesData.rows);
    }

    function calculateRow(batchesData) {
        var preparedBatchData = prepareBatchForCalculation(batchesData);
        var calculatedRow = batchesCalculation.calculateRow(preparedBatchData);

        calculationHelper.updateViewRow(calculatedRow, batchesData.changedRow);
    }

    function calculateSaltEq(rows) {
        _.forEach(rows, function(row) {
            if (canEditSaltEq(row)) {
                calculateRow({
                    changedRow: row,
                    changedField: 'saltEq'
                });
            }
        });
    }

    function calculateValuesDependingOnTheoMoles(changedRow, limitingRow) {
        var batchRow = new BatchRow(changedRow);
        var calculatedRow = batchesCalculation.calculateValuesDependingOnTheoMoles(batchRow, limitingRow);

        calculationHelper.updateViewRow(calculatedRow, changedRow);
    }

    function prepareBatchForCalculation(change) {
        var row = new BatchRow(change.changedRow);

        return {
            changedRow: row,
            changedField: change.changedField
        };
    }

    function prepareBatchesForCalculation(change) {
        var rows = _.map(change.rows, function(viewRow) {
            return new BatchRow(viewRow);
        });

        return {
            rows: rows,
            limitingRow: change.limitingRow
        };
    }

    function canEditSaltEq(batch) {
        return batch && batch.saltCode && batch.saltCode.value !== '0' && hasMolfile(batch);
    }

    var compounds = [
        {name: 'Intermediate'},
        {name: 'Test Compound'}
    ];

    function hasMolfile(o) {
        return !!_.get(o, 'structure.molfile');
    }

    function canEditSaltCode(row) {
        return hasMolfile(row);
    }

    return {
        onBatchChanged: onBatchChanged,
        calculateAllRows: calculateAllRows,
        calculateRow: calculateRow,
        calculateValuesDependingOnTheoMoles: calculateValuesDependingOnTheoMoles,
        hasCheckedRow: hasCheckedRow,
        getCheckedBatches: getCheckedBatches,
        canEditSaltEq: canEditSaltEq,
        canEditSaltCode: canEditSaltCode,
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
                min: 0,
                type: 'unit',
                width: '150px',
                unitItems: appUnits.grams,
                actions: unitService.getActions('Total Weight', appUnits.grams)
            },
            totalVolume: {
                id: 'totalVolume',
                name: 'Total Volume',
                min: 0,
                type: 'unit',
                width: '150px',
                unitItems: appUnits.liters,
                actions: unitService.getActions('Total Volume', appUnits.liters)
            },
            totalMoles: {
                id: 'totalMoles',
                name: 'Total Moles',
                min: 0,
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
                values: null,
                actions: selectService.getActions('Compound State', null, 'Compound State')
            },
            saltCode: {
                id: 'saltCode',
                name: 'Salt Code',
                type: 'select',
                showDefault: true,
                values: appValuesService.getSaltCodeValues(),
                actions: selectService.getActions('Salt Code', appValuesService.getSaltCodeValues()),
                checkEnabled: canEditSaltCode,
                disableTitle: 'Batch hasn\'t structure'
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
                actions: setInputService.getActions('Structure Comments')
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
                actions: setInputService.getActions('Batch Comments')
            },
            $$batchType: {
                id: 'batchType',
                name: 'Intermediate/Test Compound',
                type: 'select',
                values: compounds,
                actions: selectService.getActions('Intermediate/Test Compound', compounds)
            },
            molWeight: {
                id: 'molWeight',
                name: 'Mol Wgt',
                min: 0,
                type: 'scalar',
                readonly: true
            },
            formula: {
                id: 'formula',
                name: 'Mol Formula',
                type: 'formula'
            },
            conversationalBatchNumber: {
                id: 'conversationalBatchNumber',
                name: 'Conversational Batch #'
            },
            virtualCompoundId: {
                id: 'virtualCompoundId',
                name: 'Virtual Compound Id',
                type: 'input',
                actions: setInputService.getActions('Virtual Compound Id')
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
                min: 0,
                type: 'scalar',
                bulkAssignment: true,
                checkEnabled: canEditSaltEq,
                actions: [
                    {
                        name: 'Set value for Salt EQ',
                        title: 'Salt EQ',
                        action: function(rows, column) {
                            var changeRows = _.filter(rows, canEditSaltEq);
                            scalarService.action(changeRows, 'Salt EQ', column)
                                .then(function(promises) {
                                    return $q.all(promises).then(calculateSaltEq);
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
                actions: setInputService.getActions('Precursor/Reactant IDs')
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
