angular
    .module('indigoeln')
    .controller('StructureSchemeController', StructureSchemeController);

/* @ngInject */
function StructureSchemeController($scope, $q, $http, $uibModal, $rootScope, Alert, EntitiesBrowser) {
    var vm = this;

    init();

    function init() {
        vm.structureModel = getInitModel();
        onChange();
        vm.openEditor = openEditor;
        vm.importStructure = importStructure;
        vm.exportStructure = exportStructure;

        bindEvents();
    }

    function getInitModel() {
        if (vm.model && vm.model[vm.structureType]) {
            return vm.model[vm.structureType];
        }

        return {
            structureScheme: {},
            image: null,
            molfile: null,
            structureId: null
        };
    }

    function setSelecterdRow() {
        vm.structureModel = _.get(vm.share.selectedRow, 'structure');
        onChange();
    }

    function getMolfileImage(row) {
        $http({
            url: 'api/renderer/' + vm.structureType + '/image',
            method: 'POST',
            data: row.structure.molfile
        }).success(function(result) {
            vm.structureModel.image = result.image;
            onChange();
        });
    }

    function bindEvents() {
        if (vm.structureType === 'molecule') {
            $scope.$on('batch-summary-row-selected', function(event, data) {
                var row = data.row;
                setSelecterdRow();
                if (row && row.structure && row.structure.structureType === vm.structureType) {
                    if (row.structure.molfile) {
                        getMolfileImage(row);
                    }
                } else if (!_.isUndefined(row)) {
                    clearStructure();
                }
            });
        }
        if (vm.structureType === 'reaction') {
            $scope.$on('new-reaction-scheme', function(event, data) {
                vm.structureModel.image = data.image;
                vm.structureModel.molfile = data.molfile;
                onChange();
            });
        }

        $scope.$watch('vm.structureModel.structureId', function() {
            if (vm.share && vm.structureModel) {
                vm.share[vm.structureType] = getStructureMolfile();
            }
        });

        $scope.$watch('vm.model', getInitModel);
        $scope.$watch('vm.structureType', getInitModel);
    }

    function onChange() {
        vm.onChanged({structure: vm.structureModel});
    }

    function clearStructure() {
        vm.structureModel.image = null;
        vm.structureModel.molfile = null;
        vm.structureModel.structureId = null;
        onChange();
    }

    function renderStructure(type, structure) {
        var deferred = $q.defer();
        $http({
            url: 'api/renderer/' + type + '/image',
            method: 'POST',
            data: structure
        }).success(function(result) {
            deferred.resolve(result);
        });

        return deferred.promise;
    }

    function isEmptyStructure(structure) {
        var deferred = $q.defer();
        $http({
            url: 'api/bingodb/' + vm.structureType + '/empty',
            method: 'POST',
            data: structure
        }).success(function(result) {
            deferred.resolve(result.empty);
        });

        return deferred.promise;
    }

    function updateShareSelectedRow(data) {
        vm.share.selectedRow.structure = vm.share.selectedRow.structure || {};
        vm.share.selectedRow.structure.image = data.image;
        vm.share.selectedRow.structure.structureType = vm.structureType;
        vm.share.selectedRow.structure.molfile = data.structure || data.molfile;
        vm.share.selectedRow.structure.structureId = data.structureId;
        $rootScope.$broadcast('product-batch-structure-changed', vm.share.selectedRow);
    }

    function updateModelRestriction(data) {
        vm.model.restrictions.structure = vm.model.restrictions.structure || {};
        vm.model.restrictions.structure.molfile = data.structure || data.molfile;
        vm.model.restrictions.structure.image = data.image;
    }

    function setRenderedStructure(data) {
        _.assign(vm.structureModel, data);

        if (vm.share && vm.share.selectedRow && vm.structureType === 'molecule') {
            updateShareSelectedRow(data);
        }

        if (vm.model.restrictions) {
            updateModelRestriction(data);
        }

        onChange();
    }

    function setStructure(structure, structureId) {
        if (structure) {
            renderStructure(vm.structureType, structure).then(function(result) {
                setRenderedStructure({
                    molfile: structure,
                    structureId: structureId,
                    image: result.image
                });
            });
        } else {
            setRenderedStructure({});
        }
    }

    // HTTP POST to save new structure into Bingo DB and get its id
    function saveNewStructure(structure) {
        isEmptyStructure(structure).then(function(empty) {
            if (empty) {
                setStructure();
            } else {
                $http({
                    url: 'api/bingodb/' + vm.structureType + '/',
                    method: 'POST',
                    data: structure
                }).success(function(structureId) {
                    setStructure(structure, structureId);
                }).error(function() {
                    Alert.error('Cannot save the structure!');
                });
            }
        });
    }

    function openEditor($event) {
        $event.stopPropagation();
        if (vm.indigoReadonly) {
            return;
        }
        // open editor with pre-defined structure (prestructure)
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/editor/structure-editor-modal.html',
            controller: 'StructureEditorModalController',
            controllerAs: 'vm',
            windowClass: 'structure-editor-modal',
            resolve: {
                prestructure: function() {
                    return vm.structureModel.molfile;
                },
                editor: function() {
                    // TODO: get editor name from user's settings; ketcher by default
                    return 'KETCHER';
                }
            }
        });

        // process structure if changed
        modalInstance.result.then(function(structure) {
            if (vm.autosave && structure) {
                saveNewStructure(structure);
            }
            setStructure(structure);
            EntitiesBrowser.setCurrentFormDirty();
        });
    }

    function importStructure($event) {
        $event.stopPropagation();
        if (vm.indigoReadonly) {
            return;
        }
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/import/structure-import-modal.html',
            controller: 'StructureImportModalController',
            controllerAs: 'vm',
            windowClass: 'structure-import-modal'
        });

        // set structure if picked
        modalInstance.result.then(function(structure) {
            if (vm.autosave) {
                setStructure(structure);
                saveNewStructure(structure);
            } else {
                setStructure(structure);
            }
        });
    }

    function getStructureMolfile() {
        return _.get(vm.model, vm.structureType + '.molfile');
    }

    function exportStructure($event) {
        $event.stopPropagation();
        if (vm.indigoReadonly) {
            return;
        }
        $uibModal.open({
            animation: true,
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/export/structure-export-modal.html',
            controller: 'structureExportModalController',
            controllerAs: 'vm',
            windowClass: 'structure-export-modal',
            resolve: {
                structureToSave: function() {
                    return getStructureMolfile();
                },
                structureType: function() {
                    return vm.structureType;
                }
            }
        });
    }
}
