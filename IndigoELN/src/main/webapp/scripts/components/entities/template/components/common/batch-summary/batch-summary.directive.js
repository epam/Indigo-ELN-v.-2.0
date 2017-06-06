angular.module('indigoeln')
    .directive('indigoBatchSummary', function () {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/common/batch-summary/batch-summary.html',
            scope: {
                model: '=',
                share: '=',
                experimentName: '=',
                isHideColumnSettings: '=',
                structureSize: '=',
                onShowStructure: '&'
            },
            controller: function ($scope, CalculationService, AppValues, InfoEditor, RegistrationUtil, $uibModal,
                                  $log, $rootScope, AlertModal, $stateParams, SdImportService, SdExportService, $window, EntitiesBrowser,
                                  RegistrationService, Alert, $q, $http, Notebook, Experiment, ProductBatchSummaryOperations, $filter, ProductBatchSummaryCache, $timeout) {
                var stoichTable;
                var unbinds = [];

                $scope.model = $scope.model || {};
                $scope.model.productBatchSummary = $scope.model.productBatchSummary || {};
                $scope.model.productBatchSummary.batches = $scope.model.productBatchSummary.batches || [];


                var grams = AppValues.getGrams();
                var liters = AppValues.getLiters();
                var moles = AppValues.getMoles();
                var saltCodeValues = AppValues.getSaltCodeValues();
                var sourceValues = AppValues.getSourceValues();
                var sourceDetailExternal = AppValues.getSourceDetailExternal();
                var sourceDetailInternal = AppValues.getSourceDetailInternal();
                var compoundProtectionValues = AppValues.getCompoundProtectionValues();
                var compounds = [ { name : 'Intermediate'}, { name : 'Test Compound'} ]

                RegistrationService.info({}, function (info) {
                    $scope.isHasRegService = _.isArray(info) && info.length > 0;
                });


                var getProductBatches = function () {
                    return $scope.model.productBatchSummary.batches;
                };
                var setProductBatches = function (batches) {
                    $scope.model.productBatchSummary.batches = batches;
                };

                var addProductBatch = function (batch) {
                    $scope.model.productBatchSummary.batches.push(batch);
                };

                var recalculateSalt = function (reagent) {
                    CalculationService.recalculateSalt(reagent).then(function () {
                        CalculationService.recalculateStoich();
                    });
                };

               
               var setSelectSourceValueAction = {
                    action: function () {
                        var that = this;
                        $uibModal.open({
                            templateUrl: 'scripts/components/entities/template/components/productBatchSummary/product-batch-summary-set-source.html',
                            controller: 'ProductBatchSummarySetSourceController',
                            size: 'sm',
                            resolve: {
                                name: function () {
                                    return that.title;
                                },
                                sourceValues: function () {
                                    return sourceValues;
                                },
                                sourceDetailExternal: function () {
                                    return sourceDetailExternal;
                                },
                                sourceDetailInternal: function () {
                                    return sourceDetailInternal;
                                }
                            }
                        }).result.then(function (result) {
                            _.each(getProductBatches(), function (row) {
                                if (!RegistrationUtil.isRegistered(row)) {
                                    row.source = result.source;
                                    row.sourceDetail = result.sourceDetail;
                                }
                            });
                        }, function () {

                        });
                    }
                };


                $scope.share.selectedRow = _.findWhere(getProductBatches(), {$$selected: true});


                $scope.columns = [
                    {
                        id: 'structure',
                        name: 'Structure',
                        type: 'image',
                        isVisible: false,
                        width: $scope.structureSize
                    },
                    {
                        id: 'nbkBatch',
                        name: 'Nbk Batch #'
                    },
                    {id: 'registrationStatus', name: 'Registration Status'},
                    {
                        id: 'select',
                        name: 'Select',
                        type: 'boolean',
                        noDirty : true,
                        actions: [
                            {
                                name: 'Select All',
                                action: function () {
                                    _.each(getProductBatches(), function (row) {
                                        row.select = true;
                                    });
                                }
                            },
                            {
                                name: 'Deselect All',
                                action: function () {
                                    _.each(getProductBatches(), function (row) {
                                        row.select = false;
                                    });
                                }
                            }
                        ]
                    },
                    {
                        id: 'totalWeight',
                        name: 'Total Weight',
                        type: 'unit',
                        width: '150px',
                        unitItems: grams,
                        onClose: function (data) {
                            CalculationService.setEntered(data);
                            var tw  = data.row.totalWeight;
                            tw.value = Math.abs(parseInt(tw.value));
                            CalculationService.calculateProductBatch(data);
                        }
                    },
                    {
                        id: 'totalVolume',
                        name: 'Total Volume',
                        type: 'unit',
                        width: '150px',
                        unitItems: liters,
                        onClose: function (data) {
                            CalculationService.setEntered(data);
                            CalculationService.calculateProductBatch(data);
                        }
                    },
                    {
                        id: 'mol',
                        name: 'Total Moles',
                        type: 'unit',
                        width: '150px',
                        unitItems: moles,
                        onClose: function (data) {
                            CalculationService.setEntered(data);
                            CalculationService.calculateProductBatch(data);
                        }
                    },
                    {
                        id: 'theoWeight',
                        name: 'Theo. Wgt.',
                        type: 'unit',
                        unitItems: grams,
                        width: '150px',
                        hideSetValue: true,
                        readonly: true
                    },
                    {
                        id: 'theoMoles',
                        name: 'Theo. Moles',
                        width: '150px',
                        type: 'unit',
                        unitItems: moles,
                        hideSetValue: true,
                        readonly: true
                    },
                    {id: 'yield', name: '%Yield', type: 'primitive', sigDigits: 2, readonly: true},
                    {
                        id: 'compoundState',
                        name: 'Compound State',
                        type: 'select',
                        dictionary: 'Compound State',
                        values: function () {
                            return null;
                        }
                    },
                    {
                        id: 'saltCode',
                        name: 'Salt Code',
                        type: 'select',
                        showDefault : true,
                        values: function () {
                            return saltCodeValues;
                        },
                        onClose: function (data) {
                            CalculationService.setEntered(data);
                            recalculateSalt(data.row);
                            if (data.model.value == 0) {
                                data.row.saltEq.value = 0;
                            }
                        }
                    },
                    {
                        id: 'saltEq',
                        name: 'Salt EQ',
                        type: 'scalar',
                        bulkAssignment: true,
                        checkEnabled : function(o) { 
                            return (o.saltCode && o.saltCode.value > 0 );
                        },
                        onClose: function (data) {
                            CalculationService.setEntered(data);
                            recalculateSalt(data.row);
                        }
                    },
                    {
                        id: '$$purity',
                        realId: 'purity',
                        name: 'Purity',
                        type: 'string',
                        onClick: function (data) {
                            editPurity(data.row);
                        },
                        actions: [{
                            name: 'Set value for Purity',
                            title: 'Purity',
                            rows: getProductBatches(),
                            action: function () {
                                editPurityForAllRows(getProductBatches());
                            }
                        }]
                    },
                    {
                        id: '$$meltingPoint',
                        realId: 'meltingPoint',
                        name: 'Melting Point',
                        type: 'string',
                        onClick: function (data) {
                            editMeltingPoint(data.row);
                        },
                        actions: [{
                            name: 'Set value for Melting Point',
                            title: 'Melting Point',
                            rows: getProductBatches(),
                            action: function () {
                                editMeltingPointForAllRows(getProductBatches());
                            }
                        }]
                    },
                    {
                        id: 'molWeight', name: 'Mol Wgt', type: 'scalar'
                    },
                    {
                        id: 'formula', name: 'Mol Formula', type: 'input', readonly: true
                    },
                    {
                        id: 'conversationalBatchNumber', name: 'Conversational Batch #'
                    },
                    {
                        id: 'virtualCompoundId', name: 'Virtual Compound Id'
                    },
                    {
                        id: 'stereoisomer', name: 'Stereoisomer Code',
                        type: 'select',
                        dictionary: 'Stereoisomer Code',
                        values: function () {
                            return null;
                        },
                        width: '350px'
                    },
                    {
                        id: 'source', name: 'Source',
                        type: 'select',
                        values: function () {
                            return sourceValues;
                        },
                        onChange: function (row) {
                            row.sourceDetail = {};
                        },
                        hideSelectValue: true,
                        actions: [_.extend({}, setSelectSourceValueAction, {
                            name: 'Set value for Source',
                            title: 'Source'
                        })]
                    },
                    {
                        id: 'sourceDetail', name: 'Source Detail',
                        type: 'select',
                        values: function (row) {
                            if (row.source && row.source.name) {
                                if (row.source.name === 'Internal') {
                                    return sourceDetailInternal;
                                } else if (row.source.name === 'External') {
                                    return sourceDetailExternal;
                                }

                            }
                            return null;
                        },
                        hideSelectValue: true,
                        actions: [_.extend({}, setSelectSourceValueAction, {
                            name: 'Set value for Source Detail',
                            title: 'Source Detail'
                        })]
                    },
                    {
                        id: '$$externalSupplier',
                        realId: 'externalSupplier',
                        name: 'External Supplier',
                        type: 'string',
                        onClick: function (data) {
                            editExternalSupplier(data.row);
                        },
                        actions: [{
                            name: 'Set value for External Supplier',
                            title: 'External Supplier',
                            rows: getProductBatches(),
                            action: function () {
                                editExternalSupplierForAllRows(getProductBatches());
                            }
                        }]
                    },
                    {
                        id: 'precursors',
                        name: 'Precursor/Reactant IDs',
                        type: 'input'
                    },
                    {
                        id: '$$healthHazards',
                        realId: 'healthHazards',
                        name: 'Health Hazards',
                        type: 'string',
                        onClick: function (data) {
                            editHealthHazards(data.row);
                        },
                        actions: [{
                            name: 'Set value for Health Hazards',
                            title: 'Health Hazards',
                            rows: getProductBatches(),
                            action: function () {
                                editHealthHazardsForAllRows(getProductBatches());
                            }
                        }]

                    },
                    {
                        id: 'compoundProtection',
                        name: 'Compound Protection',
                        type: 'select',
                        values: function () {
                            return compoundProtectionValues;
                        }
                    },
                    {
                        id: 'structureComments', name: 'Structure Comments',
                        type: 'input', bulkAssignment: true
                    },
                    {
                        id: 'registrationDate', name: 'Registration Date', format: function (val) {
                        return val ? $filter('date')(val, 'MMM DD, YYYY HH:mm:ss z') : null;
                    }
                    }, {
                        id: '$$residualSolvents',
                        realId: 'residualSolvents',
                        name: 'Residual Solvents',
                        type: 'string',
                        onClick: function (data) {
                            editResidualSolvents([data.row]);
                        },
                        actions: [{
                            name: 'Set value for Residual Solvents',
                            title: 'Residual Solvents',
                            rows: getProductBatches(),
                            action: function () {
                                editResidualSolvents(getProductBatches());
                            }
                        }]
                    },{
                        id: '$$solubility',
                        realId: 'solubility',
                        name: 'Solubility in Solvents',
                        type: 'string',
                        onClick: function (data) {
                            editSolubility([data.row]);
                        },
                        actions: [{
                            name: 'Set value for Solubility in Solvents',
                            title: 'Solubility in Solvents',
                            rows: getProductBatches(),
                            action: function () {
                                editSolubility(getProductBatches());
                            }
                        }]
                    },{
                        id: '$$storageInstructions',
                        realId: 'storageInstructions',
                        name: 'Storage Instructions',
                        type: 'string',
                        onClick: function (data) {
                            editStorageInstructions([data.row]);
                        },
                        actions: [{
                            name: 'Set value for Storage Instructions',
                            title: 'Storage Instructions',
                            rows: getProductBatches(),
                            action: function () {
                                editStorageInstructions(getProductBatches());
                            }
                        }]
                    },{
                        id: '$$handlingPrecautions',
                        realId: 'handlingPrecautions',
                        name: 'Handling Precautions',
                        type: 'string',
                        onClick: function (data) {
                            editHandlingPrecautions([data.row]);
                        },
                        actions: [{
                            name: 'Set value for Handling Precautions',
                            title: 'Handling Precautions',
                            rows: getProductBatches(),
                            action: function () {
                                editHandlingPrecautions(getProductBatches());
                            }
                        }]
                    }, {
                        id: 'comments', 
                        name: 'Batch Comments',
                        type: 'input'
                    }, {
                        id: '$$batchType',
                        name: 'Intermediate/Test Compound',
                        type: 'select',
                        values: function (row) {
                            return compounds;
                        },
                        onClose: function (data) {
                            var r = data.row;
                            if (!r.$$batchType) return;
                            r.batchType = r.$$batchType.name;
                        }
                    },
  
                ];
        
               var editResidualSolvents = function (rows) {
                    var callback = function (result) {
                         _.each(rows, function (row) {
                            row.residualSolvents = result;
                        })
                    };
                    var data = rows.length == 1 ? rows[0].residualSolvents : {};
                    InfoEditor.editResidualSolvents(data || {}, callback);
                };
                
                var editSolubility = function (rows) {
                    var callback = function (result) {
                         _.each(rows, function (row) {
                            row.solubility = result;
                        })

                    };
                    var data = rows.length == 1 ? rows[0].solubility : {};
                    InfoEditor.editSolubility(data || {}, callback);
                };
                
                var editHandlingPrecautions = function (rows) {
                    var callback = function (result) {
                         _.each(rows, function (row) {
                            row.handlingPrecautions = result;
                        })
                    };
                    var data = rows.length == 1 ? rows[0].handlingPrecautions : {};
                    InfoEditor.editHandlingPrecautions(data || {}, callback);
                };

                var editStorageInstructions = function (rows) {
                    var callback = function (result) {
                         _.each(rows, function (row) {
                            row.storageInstructions = result;
                        })
                    };
                    var data = rows.length == 1 ? rows[0].storageInstructions : {};
                    InfoEditor.editStorageInstructions(data || {}, callback);
                };

                $scope.showStructuresColumn = _.find($scope.columns, function (item) {
                    return item.id === 'structure';
                });


                function editPurity(row) {
                    InfoEditor.editPurity(row.purity, function (result) {
                        row.purity = result;
                    });
                }

                function editPurityForAllRows(rows) {
                    InfoEditor.editPurity({}, function (result) {
                        _.each(rows, function (row) {
                            if (!RegistrationUtil.isRegistered(row)) {
                                row.purity = angular.copy(result);
                            }
                        });
                    });
                }


                function editMeltingPoint(row) {
                    InfoEditor.editMeltingPoint(row.meltingPoint, function (result) {
                        row.meltingPoint = result;
                    });
                }

                function editMeltingPointForAllRows(rows) {
                    InfoEditor.editMeltingPoint({}, function (result) {
                        _.each(rows, function (row) {
                            if (!RegistrationUtil.isRegistered(row)) {
                                row.meltingPoint = angular.copy(result);
                            }
                        });
                    });
                }


                function editExternalSupplier(row) {
                    InfoEditor.editExternalSupplier(row.externalSupplier, function (result) {
                        row.externalSupplier = result;
                    });
                }

                function editExternalSupplierForAllRows(rows) {
                    InfoEditor.editExternalSupplier({}, function (result) {
                        _.each(rows, function (row) {
                            if (!RegistrationUtil.isRegistered(row)) {
                                row.externalSupplier = angular.copy(result);
                            }
                        });
                    });
                }

                function editHealthHazards(row) {
                    InfoEditor.editHealthHazards(row.healthHazards, function (result) {
                        row.healthHazards = result;
                    });
                }

                function editHealthHazardsForAllRows(rows) {
                    InfoEditor.editHealthHazards({}, function (result) {
                        _.each(rows, function (row) {
                            if (!RegistrationUtil.isRegistered(row)) {
                                row.healthHazards = angular.copy(result);
                            }
                        });
                    });
                }

                $scope.syncWithIntendedProducts = function () {
                    ProductBatchSummaryOperations.syncWithIntendedProducts()
                }

                function updatePrecursor() {
                    if (!getStoicTable()) {
                        return;
                    }
                    _.findWhere($scope.columns, {id: 'precursors'}).readonly = true;
                    var precursors = $scope.share.stoichTable.reactants.filter(function(r) { 
                        return  (r.compoundId || r.fullNbkBatch) && r.rxnRole && r.rxnRole.name == 'REACTANT' 
                    })                    
                    .map(function(r) { return r.compoundId || r.fullNbkBatch }).join(', ')
                    _.each(getProductBatches(), function (batch) {
                        batch.precursors = precursors;
                    });
                }


                function getStoicTable() {
                    return stoichTable;
                }

                function setStoicTable(table) {
                    stoichTable = table;
                    ProductBatchSummaryOperations.setStoicTable(stoichTable)
                }

                $scope.isEditable = function (row, columnId) {
                    var rowResult = !(RegistrationUtil.isRegistered(row));
                    if (rowResult) {
                        if (columnId === 'precursors') {
                            if ($scope.share.stoichTable) {
                                return false;
                            }
                        }
                    }
                    return rowResult;
                };

                $scope.onRowSelected = function (row) {
                    $scope.share.selectedRow = row || null;
                    if (row) {
                        $rootScope.$broadcast('batch-summary-row-selected', {row: row});
                    } else {
                        $rootScope.$broadcast('batch-summary-row-deselected');
                    }
                    $log.debug(row);
                };

                $scope.isIntendedSynced = function () {
                    var intended = ProductBatchSummaryOperations.getIntendedNotInActual()
                    return intended ? !intended.length : true;
                };

                $scope.duplicateBatches = function (batchesQueueToAdd, i, isSyncWithIntended) {
                    ProductBatchSummaryOperations.duplicateBatches(batchesQueueToAdd, i, isSyncWithIntended)
                };

                $scope.duplicateBatch = function () {
                    ProductBatchSummaryOperations.duplicateBatch()
                };

                $scope.addNewBatch = function () {
                    ProductBatchSummaryOperations.addNewBatch().then(function(batch) {
                         $scope.onRowSelected(batch);
                    })
                };
                
                $scope.isHasCheckedRows = function () {
                    return !!_.find(getProductBatches(), function (item) {
                        return item.select;
                    });
                };

                $scope.deleteBatches = function () {
                    ProductBatchSummaryOperations.deleteBatches();
                    if ($scope.share.selectedRow && $scope.share.selectedRow.select)
                        $scope.onRowSelected(null)
                };

                $scope.importSDFile = function() {
                    $scope.importLoading = true;
                    ProductBatchSummaryOperations.importSDFile(function() {
                        $scope.importLoading = false;
                    });
                };

                $scope.exportSDFile = function () {
                    ProductBatchSummaryOperations.exportSDFile()
                };

                $scope.registerBatches = function () {
                    $scope.loading = ProductBatchSummaryOperations.registerBatches()
                };

                var initStructure = function(batches, type) {
                    var changed;
                    batches.forEach(function(row) {
                        if (row.structure) return;
                        changed = true;
                        type = type || 'molecule';
                        var model = $scope.model[type] ||  {};
                        model.image = null;
                        model.structureMolfile = null;
                        model.structureId = null;
                       
                        row.structure = row.structure || {};
                        row.structure.image = null;
                        row.structure.structureType = type;
                        row.structure.molfile = null;
                        row.structure.structureId = null;
                    })
                    if (changed) {
                        CalculationService.recalculateStoich();
                    }
                }

                unbinds.push($scope.$watch('share.stoichTable', function (table) {
                    setStoicTable(table);
                    updatePrecursor();
                }, true));

                unbinds.push($scope.$watch('model.productBatchSummary.batches', function (batches) {
                    _.each(batches, function (batch) {
                        batch.$$purity = batch.purity ? batch.purity.asString : null;
                        batch.$$externalSupplier = batch.externalSupplier ? batch.externalSupplier.asString : null;
                        batch.$$meltingPoint = batch.meltingPoint ? batch.meltingPoint.asString : null;
                        batch.$$healthHazards = batch.healthHazards ? batch.healthHazards.asString : null;
                        batch.$$batchType = batch.batchType ? (compounds[0].name == batch.batchType  ? compounds[0] : compounds[1]) : null;
                    });
                    $scope.share.actualProducts = batches;
                    ProductBatchSummaryCache.setProductBatchSummary(batches);
                    updatePrecursor();
                    initStructure(batches)
                }, true));

                unbinds.push($scope.$watch('isHasRegService', function (val) {
                    _.findWhere($scope.columns, {id: 'conversationalBatchNumber'}).isVisible = val;
                    _.findWhere($scope.columns, {id: 'registrationDate'}).isVisible = val;
                    _.findWhere($scope.columns, {id: 'registrationStatus'}).isVisible = val;
                }));



                unbinds.push( $scope.$watch(function(){
                    return $scope.showStructuresColumn.isVisible;
                }, function (val) {
                    $scope.onShowStructure({isVisible: val});
                }));

                unbinds.push($scope.$on('product-batch-structure-changed', function (event, row) {

                    var resetMolInfo = function () {
                        row.formula = null;
                        row.molWeight = null;
                        CalculationService.calculateProductBatch({row : row, column : 'totalWeight'});
                    };

                    var getInfoCallback = function (molInfo) {
                        row.formula = molInfo.data.molecularFormula;
                        row.molWeight = row.molWeight || {};
                        row.molWeight.value = molInfo.data.molecularWeight;
                        CalculationService.recalculateStoich();
                        CalculationService.calculateProductBatch({row : row, column : 'totalWeight'});
                    };
                    if (row.structure && row.structure.molfile) {
                        CalculationService.getMoleculeInfo(row, getInfoCallback, resetMolInfo);
                    } else {
                        resetMolInfo();
                    }
                }));

                unbinds.push($scope.$on('product-batch-details-command', function (event, data) {
                    console.log(data, $scope[data.command])
                    if ($scope[data.command]) $scope[data.command]()
                }));

                unbinds.push($scope.$on('product-batch-summary-recalculated', function (event, data) {
                    //$timeout(function() {}, 500);
                    if (data.length === getProductBatches().length) {
                        _.each(getProductBatches(), function (batch, i) {
                            _.extend(batch, data[i]);
                        });
                    }
                }));


                unbinds.push($scope.$watch('structureSize', function (newVal) {
                    var column = _.find($scope.columns, function (item) {
                        return item.id === 'structure';
                    });
                    column.width = 500 * newVal + 'px';
                }));


                $rootScope.$on('batch-registration-status-changed', function (event, statuses) {
                    _.each(statuses, function (status, fullNbkBatch) {
                        var batch = _.find(getProductBatches(), function (batch) {
                            return batch.fullNbkBatch === fullNbkBatch;
                        });
                        if (batch) {
                            batch.registrationStatus = status.status;
                            batch.registrationDate = status.date;
                            if (status.compoundNumbers) {
                                batch.compoundId = status.compoundNumbers[fullNbkBatch];
                            }
                            if (status.conversationalBatchNumbers) {
                                batch.conversationalBatchNumber = status.conversationalBatchNumbers[fullNbkBatch];
                            }
                        }
                    });
                });

                $scope.$on('$destroy', function () {
                    _.each(unbinds, function (unbind) {
                        unbind();
                    });
                });


            }
        };
    });

