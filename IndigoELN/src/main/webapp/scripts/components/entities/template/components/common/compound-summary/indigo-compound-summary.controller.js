(function() {
    angular
        .module('indigoeln')
        .controller('IndigoCompoundSummaryController', IndigoCompoundSummaryController);

    /* @ngInject */
    function IndigoCompoundSummaryController($scope, RegistrationUtil, $log, AlertModal, $stateParams, sdImportService,
                                             sdExportService, $window, $http, Notebook, EntitiesCache) {
        var vm = this;
        var unbinds = [];
        var showStructureColumn;

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
            vm.onAddedBatch({batch: compound});
        }

        function getLatestNbkBatch() {
            var compound = _.last(getCompounds());

            return (compound && compound.nbkBatch) || 0;
        }

        function createCompound(notebook, batchNumber, duplicatedCompound) {
            var fullNbkBatch = notebook.name + '-' + vm.experimentName + '-' + batchNumber;
            var fullNbkImmutablePart = notebook.name + '-' + vm.experimentName + '-';
            var compound = duplicatedCompound || {};
            compound.nbkBatch = batchNumber;
            compound.fullNbkBatch = fullNbkBatch;
            compound.fullNbkImmutablePart = fullNbkImmutablePart;
            compound.molWeight = {
                value: 0, entered: false
            };

            return compound;
        }

        // TODO: use product-batch-summary, it doesn't work as must.
        function requestNbkBatchNumberAndAddToTable(sdUnit) {
            var latest = getLatestNbkBatch();
            var request = 'api/projects/' + $stateParams.projectId + '/notebooks/' + $stateParams.notebookId +
                '/experiments/' + $stateParams.experimentId + '/batch_number?latest=' + latest;
            var notebookParams = {
                projectId: $stateParams.projectId,
                notebookId: $stateParams.notebookId
            };

            return $http.get(request)
                .then(function(result) {
                    return result.data.batchNumber;
                })
                .then(function(batchNumber) {
                    var notebookPromise = EntitiesCache.get($stateParams);
                    if (!notebookPromise) {
                        notebookPromise = Notebook.get(notebookParams).$promise;
                        EntitiesCache.put($stateParams, notebookPromise);
                    }

                    return notebookPromise.then(function(notebook) {
                        var compound = createCompound(notebook, batchNumber, sdUnit);
                        addCompound(compound);
                        vm.onRowSelected(compound);
                    });
                });
        }

        function onRowSelected(row) {
            vm.onSelectBatch({batch: _.isEqual(row, vm.selectedBatch) ? null : row});
        }

        function registerVC() {

        }

        function addNewCompound() {
            requestNbkBatchNumberAndAddToTable();
        }

        function getSelectedNonEditableCompounds() {
            var selectedCompounds = [];

            _.forEach(getCompounds(), function(compound) {
                if (compound.select && RegistrationUtil.isRegistered(compound)) {
                    selectedCompounds.push(compound.fullNbkBatch);
                }
            });

            return selectedCompounds;
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
            vm.onSelectBatch({batch: null});
        }

        function importSDFile() {
            sdImportService.importFile().then(function(sdUnits) {
                _.forEach(sdUnits, function(sdUnit) {
                    requestNbkBatchNumberAndAddToTable(sdUnit);
                });
            });
        }

        function exportSDFile() {
            var selectedBatches = _.filter(getCompounds(), function(item) {
                return item.select;
            });
            sdExportService.exportItems(selectedBatches).then(function(data) {
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

            $scope.$watch('vm.batchesTrigger', function() {
                setCompounds(vm.batches);
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
        }
    }
})();
