angular
    .module('indigoeln')
    .controller('StructureSchemeController', StructureSchemeController);

/* @ngInject */
function StructureSchemeController($scope, $q, $http, $uibModal, $rootScope, Alert, EntitiesBrowser) {
    var vm = this;

    init();

    function init() {
        initModel();
        vm.openEditor = openEditor;
        vm.importStructure = importStructure;
        vm.exportStructure = exportStructure;

        bindEvents();
    }

    function initModel() {
        var newStructure = {
            structureScheme: {}
        };
        vm.model[vm.structureType] = vm.model[vm.structureType] || newStructure;
    }

    function setSelecterdRow() {
        vm.model[vm.structureType].image = vm.share.selectedRow.structure.image;
        vm.model[vm.structureType].structureMolfile = vm.share.selectedRow.structure.molfile;
        vm.model[vm.structureType].structureId = vm.share.selectedRow.structure.structureId;
    }

    function bindEvents() {
        if (vm.structureType === 'molecule') {
            $scope.$on('batch-summary-row-selected', function(event, data) {
                var row = data.row;
                if (row && row.structure && row.structure.structureType === vm.structureType) {
                    setSelecterdRow();
                    $http({
                        url: 'api/renderer/' + vm.structureType + '/image',
                        method: 'POST',
                        data: vm.model[vm.structureType].structureMolfile
                    }).success(function(result) {
                        vm.model[vm.structureType].image = result.image;
                    });
                } else if (!_.isUndefined(row)) {
                    clearStructure();
                }
            });
        }
        if (vm.structureType === 'reaction') {
            $scope.$on('new-reaction-scheme', function(event, data) {
                vm.model[vm.structureType].image = data.image;
                vm.model[vm.structureType].structureMolfile = data.molfile;
            });
        }

        $scope.$watch('vm.model.' + vm.structureType + '.structureId', function() {
            if (vm.share) {
                vm.share[vm.structureType] = vm.model[vm.structureType].structureMolfile;
            }
        });
    }

    function clearStructure() {
        vm.model[vm.structureType].image = null;
        vm.model[vm.structureType].structureMolfile = null;
        vm.model[vm.structureType].structureId = null;
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

    function isEmptyStructure(type, structure) {
        var deferred = $q.defer();
        $http({
            url: 'api/bingodb/' + type + '/empty',
            method: 'POST',
            data: structure
        }).success(function(result) {
            deferred.resolve(result.empty);
        });

        return deferred.promise;
    }

    function setRenderedStructure(type, data) {
        vm.model[type].image = data.image;
        vm.model[type].structureMolfile = data.structure;
        vm.model[type].structureId = data.structureId;
        if (vm.share && vm.share.selectedRow && type === 'molecule') {
            vm.share.selectedRow.structure = vm.share.selectedRow.structure || {};
            vm.share.selectedRow.structure.image = data.image;
            vm.share.selectedRow.structure.structureType = type;
            vm.share.selectedRow.structure.molfile = data.structure;
            vm.share.selectedRow.structure.structureId = data.structureId;
            $rootScope.$broadcast('product-batch-structure-changed', vm.share.selectedRow);
        }
        if (vm.model.restrictions) {
            vm.model.restrictions.structure = vm.model.restrictions.structure || {};
            vm.model.restrictions.structure.molfile = data.structure;
            vm.model.restrictions.structure.image = data.image;
        }

        vm.onChange({
            model: vm.model
        });
    }

    function setStructure(type, structure, structureId) {
        if (structure) {
            renderStructure(type, structure).then(function(result) {
                setRenderedStructure(type, {
                    structure: structure,
                    structureId: structureId,
                    image: result.image
                });
            });
        } else {
            setRenderedStructure(type, {});
        }
    }

    // HTTP POST to save new structure into Bingo DB and get its id
    function saveNewStructure(type, structure) {
        isEmptyStructure(type, structure).then(function(empty) {
            if (empty) {
                setStructure(type);
            } else {
                $http({
                    url: 'api/bingodb/' + type + '/',
                    method: 'POST',
                    data: structure
                }).success(function(structureId) {
                    setStructure(type, structure, structureId);
                }).error(function() {
                    Alert.error('Cannot save the structure!');
                });
            }
        });
    }

    function openEditor($event) {
        $event.stopPropagation();
        if (vm.readonly) {
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
                    return vm.model[vm.structureType].structureMolfile;
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
                saveNewStructure(vm.structureType, structure);
            }
            setStructure(vm.structureType, structure);
            EntitiesBrowser.setCurrentFormDirty();
        });
    }

    function importStructure($event) {
        $event.stopPropagation();
        if (vm.readonly) {
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
                setStructure(vm.structureType, structure);
                saveNewStructure(vm.structureType, structure);
            } else {
                setStructure(vm.structureType, structure);
            }
        });
    }

    function exportStructure($event) {
        $event.stopPropagation();
        if (vm.readonly) {
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
                    return vm.model[vm.structureType].structureMolfile;
                },
                structureType: function() {
                    return vm.structureType;
                }
            }
        });
    }
}
