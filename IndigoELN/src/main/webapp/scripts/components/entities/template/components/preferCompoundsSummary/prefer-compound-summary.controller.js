angular.module('indigoeln')
    .controller('PreferCompoundSummaryController',
    function ($scope, $stateParams, Notebook, EntitiesCache, $http, SdService, SdImportService, $window, RegistrationUtil, AlertModal, $rootScope, $log, CalculationService, Dictionary) {

            $scope.model = $scope.model || {};
            $scope.model.preferredCompoundSummary = $scope.model.preferredCompoundSummary || {};
            $scope.model.preferredCompoundSummary.compounds = $scope.model.preferredCompoundSummary.compounds || [];

            $scope.columns = [
                {
                    id: 'structure',
                    name: 'Structure',
                    type: 'image',
                    isVisible: false,
                    width: '300px'
                },
                {
                    id: 'nbkBatch',
                    name: 'Nbk Batch #'
                },
                {
                    id: 'select',
                    name: 'Select',
                    type: 'boolean',
                    actions: [
                        {
                            name: 'Select All',
                            action: function () {
                                _.each(getCompounds(), function (row) {
                                    row.select = true;
                                });
                            }
                        },
                        {
                            name: 'Deselect All',
                            action: function () {
                                _.each(getCompounds(), function (row) {
                                    row.select = false;
                                });
                            }
                        }
                    ]
                },
                {
                    id: 'virtualCompoundId', name: 'Virtual Compound ID',  type: 'input'
                },
                {
                    id: 'molWeight', name: 'Mol Wgt', type: 'scalar'
                },
                {
                    id: 'formula', name: 'Mol Formula', type: 'input', readonly: true
                },
                {
                    id: 'stereoisomer', name: 'Stereoisomer',
                    type: 'select',
                    dictionary: 'Stereoisomer Code',
                    values: function () {
                        return null;
                    },
                    width: '350px'
                },
                {
                    id: 'structureComments', name: 'Structure Comments',
                    type: 'input', bulkAssignment: true
                }
            ];

            var getCompounds = function () {
                return $scope.model.preferredCompoundSummary.compounds;
            };

            var setCompounds = function (compounds) {
                $scope.model.preferredCompoundSummary.compounds = compounds;
            };

            var addCompound = function (compound) {
                $scope.model.preferredCompoundSummary.compounds.push(compound);
            };

            $scope.share.selectedRow = _.findWhere(getCompounds(), {$$selected: true});


            function getLatestNbkBatch() {
                var compounds = getCompounds();
                return compounds && compounds.length > 0 && compounds[compounds.length - 1].nbkBatch ? compounds[compounds.length - 1].nbkBatch : 0;
            }


        function requestNbkBatchNumberAndAddToTable(duplicatedCompound) {
                var latest = getLatestNbkBatch();
                return $http.get('api/projects/' + $stateParams.projectId + '/notebooks/' + $stateParams.notebookId +
                        '/experiments/' + $stateParams.experimentId + '/batch_number?latest=' + latest)
                    .then(function (result) {
                        var batchNumber = result.data.batchNumber;
                        if(!EntitiesCache.get($stateParams)){
                            EntitiesCache.put($stateParams,  Notebook.get({ projectId: $stateParams.projectId, notebookId: $stateParams.notebookId}).$promise);
                        }
                         EntitiesCache.get($stateParams).then(function (notebook) {
                            var fullNbkBatch = notebook.name + '-' + $scope.experiment.name + '-' + batchNumber;
                            var fullNbkImmutablePart = notebook.name + '-' + $scope.experiment.name + '-';
                            _.each(getCompounds(), function (row) {
                                row.$$selected = false;
                            });
                             var compound = duplicatedCompound || {};
                             compound.nbkBatch = batchNumber;
                             compound.fullNbkBatch = fullNbkBatch;
                             compound.fullNbkImmutablePart = fullNbkImmutablePart;
                             compound.molWeight = {value: 0, entered:false};
                             compound.$$selected = true;
                             addCompound(compound);
                             $scope.onRowSelected(compound);
                        });
                    });
            }

            $scope.onRowSelected = function (row) {
                console.log(row);
                $scope.share.selectedRow = row || null;
                if (row) {
                    var data = {};
                    data.row = row;
                    $rootScope.$broadcast('batch-summary-row-selected', data);
                } else {
                    $rootScope.$broadcast('batch-summary-row-deselected');
                }
                $log.debug(row);
            };


            $scope.registerVC = function(){

            };

            $scope.addNewCompound = function () {
                requestNbkBatchNumberAndAddToTable();
            };

            var getSelectedNonEditableCompounds = function () {
                return _.chain(getCompounds()).filter(function (item) {
                    return item.select;
                }).filter(function (item) {
                    return RegistrationUtil.isRegistered(item);
                }).map(function (item) {
                    return item.fullNbkBatch;
                }).value();
            };

            $scope.deleteCompounds = function () {
                var nonEditableBatches = getSelectedNonEditableCompounds();
                if (nonEditableBatches && nonEditableBatches.length > 0) {
                    AlertModal.error('Following compounds were registered or sent to registration and cannot be deleted: ' + _.uniq(nonEditableBatches).join(', '));
                    return;
                }
                setCompounds(_.filter(getCompounds(), function (item) {
                    return !item.select;
                }));
                $rootScope.$broadcast('batch-summary-row-deselected');
            };

        $scope.importSDFile = function () {
            SdImportService.importFile(requestNbkBatchNumberAndAddToTable);
        };

            $scope.exportSDFile = function () {
                var selectedBatchNumbers = _.chain(getCompounds()).filter(function (item) {
                    return item.select;
                }).map(function (batch) {
                    return batch.fullNbkBatch;
                }).value();
                SdService.export({component: 'compound'}, selectedBatchNumbers, function (data) {
                    $window.open('api/sd/download?fileName=' + data.fileName);
                });
            };

            $scope.isHasCheckedRows = function () {
                return !!_.find(getCompounds(), function (item) {
                    return item.select;
                });
            };


            $scope.showStructuresColumn = _.find($scope.columns, function (item) {
                return item.id === 'structure';
            });
            $scope.toggleShowStructures = function () {
                $scope.showStructuresColumn.isVisible = !$scope.showStructuresColumn.isVisible;
            };

            var onCompoundStructureChanged = $scope.$on('product-batch-structure-changed', function (event, row) {
                var resetMolInfo = function () {
                    row.formula = null;
                    row.molWeight = null;
                };
                var getInfoCallback = function (molInfo) {
                    row.formula = molInfo.data.molecularFormula;
                    row.molWeight = row.molWeight || {};
                    row.molWeight.value = molInfo.data.molecularWeight;
                };
                if (row.structure && row.structure.molfile) {
                    CalculationService.getMoleculeInfo(row, getInfoCallback, resetMolInfo);
                } else {
                    resetMolInfo();
                }
            });

            $scope.$on('$destroy', function () {
                onCompoundStructureChanged();
            });

        });