(function() {
    angular
        .module('indigoeln')
        .controller('IndigoCompoundSummaryController', IndigoCompoundSummaryController);

    /* @ngInject */
    function IndigoCompoundSummaryController($scope, CalculationService, RegistrationUtil, $log, $rootScope, AlertModal,
                                             $stateParams, SdImportService, SdExportService, $window, $q, $http,
                                             Notebook, EntitiesCache) {
        var vm = this;
        var unbinds = [];
        var showStructureColumn;
        var getNotebook;

        init();

        function init() {
            vm.model = vm.model || {};
            vm.model.preferredCompoundSummary = vm.model.preferredCompoundSummary || {};
            vm.model.preferredCompoundSummary.compounds = vm.model.preferredCompoundSummary.compounds || [];
            vm.columns = [
                {
                    id: 'structure',
                    name: 'Structure',
                    type: 'image',
                    isVisible: false,
                    width: vm.structureSize
                },
                {
                    id: 'nbkBatch',
                    name: 'Nbk Batch #'
                },
                {
                    id: 'select',
                    name: 'Select',
                    type: 'boolean',
                    noDirty: true,
                    actions: [
                        {
                            name: 'Select All',
                            action: function() {
                                _.each(getCompounds(), function(row) {
                                    row.select = true;
                                });
                            }
                        },
                        {
                            name: 'Deselect All',
                            action: function() {
                                _.each(getCompounds(), function(row) {
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
                    id: 'stereoisomer',
                    name: 'Stereoisomer',
                    type: 'select',
                    dictionary: 'Stereoisomer Code',
                    values: function() {
                        return null;
                    },
                    width: '350px'
                },
                {
                    id: 'structureComments',
                    name: 'Structure Comments',
                    type: 'input',
                    bulkAssignment: true
                }
            ];

            vm.onRowSelected = onRowSelected;
            vm.setCompounds = setCompounds;
            vm.deleteCompounds = deleteCompounds;
            vm.addNewCompound = addNewCompound;
            vm.registerVC = registerVC;
            vm.importSDFile = importSDFile;
            vm.exportSDFile = exportSDFile;
            vm.isHasCheckedRows = isHasCheckedRows;
            vm.vnv = vnv;

            showStructureColumn = getShowedStructuresColumns();
            bindEvents();
        }

        function getShowedStructuresColumns() {
            return _.find(vm.columns, function(item) {
                return item.id === 'structure';
            });
        }

        function getCompounds() {
            return vm.model.preferredCompoundSummary.compounds;
        }

        function setCompounds(compounds) {
            vm.model.preferredCompoundSummary.compounds = compounds;
        }

        function addCompound(compound) {
            vm.model.preferredCompoundSummary.compounds.push(compound);
        }

        vm.share.selectedRow = _.findWhere(getCompounds(), {
            $$selected: true
        });

        function getLatestNbkBatch() {
            var compound = _.last(getCompounds());

            return (compound && compound.nbkBatch) || 0;
        }

        function requestNbkBatchNumberAndAddToTable(duplicatedCompound) {
            var latest = getLatestNbkBatch();

            return $http.get('api/projects/' + $stateParams.projectId + '/notebooks/' + $stateParams.notebookId +
                '/experiments/' + $stateParams.experimentId + '/batch_number?latest=' + latest)
                .then(function(result) {
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
                        });
                    }
                    getNotebook.promise.then(function(notebook) {
                        var fullNbkBatch = notebook.name + '-' + vm.experimentName + '-' + batchNumber;
                        var fullNbkImmutablePart = notebook.name + '-' + vm.experimentName + '-';
                        _.each(getCompounds(), function(row) {
                            row.$$selected = false;
                        });
                        var compound = duplicatedCompound || {};
                        compound.nbkBatch = batchNumber;
                        compound.fullNbkBatch = fullNbkBatch;
                        compound.fullNbkImmutablePart = fullNbkImmutablePart;
                        compound.molWeight = {
                            value: 0, entered: false
                        };
                        compound.$$selected = true;
                        addCompound(compound);
                        vm.onRowSelected(compound);
                    });
                });
        }

        function onRowSelected(row) {
            vm.share.selectedRow = row || null;
            if (row) {
                var data = {
                    row: row
                };
                $rootScope.$broadcast('batch-summary-row-selected', data);
            } else {
                $rootScope.$broadcast('batch-summary-row-deselected');
            }
            $log.debug(row);
        }

        function registerVC() {

        }

        function addNewCompound() {
            requestNbkBatchNumberAndAddToTable();
        }

        function getSelectedNonEditableCompounds() {
            return _
                .chain(getCompounds())
                .filter(function(item) {
                    return item.select;
                })
                .filter(function(item) {
                    return RegistrationUtil.isRegistered(item);
                })
                .map(function(item) {
                    return item.fullNbkBatch;
                })
                .value();
        }

        function deleteCompounds() {
            var nonEditableBatches = getSelectedNonEditableCompounds();
            if (nonEditableBatches && nonEditableBatches.length > 0) {
                AlertModal.error('Following compounds were registered or sent to registration and cannot be deleted: ' + _.uniq(nonEditableBatches)
                        .join(', '));

                return;
            }
            setCompounds(_.filter(getCompounds(), function(item) {
                return !item.select;
            }));
            $rootScope.$broadcast('batch-summary-row-deselected');
        }

        function importSDFile() {
            SdImportService.importFile(requestNbkBatchNumberAndAddToTable);
        }

        function exportSDFile() {
            var selectedBatches = _.filter(getCompounds(), function(item) {
                return item.select;
            });
            SdExportService.exportItems(selectedBatches).then(function(data) {
                $window.open('api/sd/download?fileName=' + data.fileName);
            });
        }

        function isHasCheckedRows() {
            return !!_.find(getCompounds(), function(item) {
                return item.select;
            });
        }

        function vnv() {
            $log.debug('VnV');
        }

        function bindEvents() {
            $scope.$watch('vm.structureSize', function(newVal) {
                var column = _.find(vm.columns, function(item) {
                    return item.id === 'structure';
                });
                column.width = (500 * newVal) + 'px';
            });

            $scope.$watch(function() {
                return showStructureColumn.isVisible;
            }, function(val) {
                vm.onShowStructure({
                    isVisible: val
                });
            });

            $scope.$on('$destroy', function() {
                _.each(unbinds, function(unbind) {
                    unbind();
                });
            });

            $scope.$on('product-batch-structure-changed', function(event, row) {
                var resetMolInfo = function() {
                    row.formula = null;
                    row.molWeight = null;
                };
                var getInfoCallback = function(molInfo) {
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
        }
    }
})();
