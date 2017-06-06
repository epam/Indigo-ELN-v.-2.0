angular.module('indigoeln')
    .directive('indigoCompoundSummary', function () {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/common/compound-summary/compound-summary.html',
            scope: {
                model: '=',
                share: '=',
                experimentName: '=',
                structureSize: '=',
                isHideColumnSettings: '=',
                onShowStructure: '&'
            },
            controller: function ($scope, CalculationService, RegistrationUtil,
                                  $log, $rootScope, AlertModal, $stateParams, SdImportService, SdExportService, $window,
                                  $q, $http, Notebook, EntitiesCache) {

                $scope.model = $scope.model || {};
                $scope.model.preferredCompoundSummary = $scope.model.preferredCompoundSummary || {};
                $scope.model.preferredCompoundSummary.compounds = $scope.model.preferredCompoundSummary.compounds || [];

                var unbinds = [];

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
                    {
                        id: 'select',
                        name: 'Select',
                        type: 'boolean',
                        noDirty : true,
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
                        id: 'virtualCompoundId', name: 'Virtual Compound ID', type: 'input'
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

                $scope.showStructuresColumn = _.find($scope.columns, function (item) {
                    return item.id === 'structure';
                });

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

                var getNotebook;
                function requestNbkBatchNumberAndAddToTable(duplicatedCompound) {
                    var latest = getLatestNbkBatch();
                    return $http.get('api/projects/' + $stateParams.projectId + '/notebooks/' + $stateParams.notebookId +
                            '/experiments/' + $stateParams.experimentId + '/batch_number?latest=' + latest)
                        .then(function (result) {
                            var batchNumber = result.data.batchNumber;
                            if (!EntitiesCache.get($stateParams)) {
                                EntitiesCache.put($stateParams, Notebook.get({
                                    projectId: $stateParams.projectId,
                                    notebookId: $stateParams.notebookId
                                }).$promise);
                            }
                            if (!getNotebook) {
                                getNotebook = $q.defer();
                                Notebook.get({
                                    projectId: $stateParams.projectId,
                                    notebookId: $stateParams.notebookId
                                }).$promise.then(function(notebook) {
                                    getNotebook.resolve(notebook);
                                })
                            }
                            getNotebook.promise.then(function(notebook) {
                                var fullNbkBatch = notebook.name + '-' + $scope.experimentName + '-' + batchNumber;
                                var fullNbkImmutablePart = notebook.name + '-' + $scope.experimentName + '-';
                                _.each(getCompounds(), function (row) {
                                    row.$$selected = false;
                                });
                                var compound = duplicatedCompound || {};
                                compound.nbkBatch = batchNumber;
                                compound.fullNbkBatch = fullNbkBatch;
                                compound.fullNbkImmutablePart = fullNbkImmutablePart;
                                compound.molWeight = {value: 0, entered: false};
                                compound.$$selected = true;
                                addCompound(compound);
                                $scope.onRowSelected(compound);
                            })
                            
                        });
                }

                $scope.onRowSelected = function (row) {
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


                $scope.registerVC = function () {

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
                    var selectedBatches = _.filter(getCompounds(), function (item) {
                        return item.select;
                    });
                    SdExportService.exportItems(selectedBatches).then(function (data) {
                        $window.open('api/sd/download?fileName=' + data.fileName);
                    });
                };

                $scope.isHasCheckedRows = function () {
                    return !!_.find(getCompounds(), function (item) {
                        return item.select;
                    });
                };


                unbinds.push($scope.$watch('structureSize', function (newVal) {
                    var column = _.find($scope.columns, function (item) {
                        return item.id === 'structure';
                    });
                    column.width = 500 * newVal + 'px';
                }));


                unbinds.push($scope.$on('product-batch-structure-changed', function (event, row) {
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
                }));



                unbinds.push( $scope.$watch(function(){
                    return $scope.showStructuresColumn.isVisible;
                }, function (val) {
                    $scope.onShowStructure({isVisible: val});
                }));



                $scope.$on('$destroy', function () {
                    _.each(unbinds, function (unbind) {
                        unbind();
                    });
                });
            }
        };
    });

