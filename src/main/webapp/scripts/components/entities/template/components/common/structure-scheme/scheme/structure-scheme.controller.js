angular
    .module('indigoeln')
    .controller('StructureSchemeController', StructureSchemeController);

/* @ngInject */
function StructureSchemeController($scope, $q, $http, $uibModal, Alert) {
    var vm = this;

    init();

    function init() {
        vm.structureModel = getInitModel();
        if (!_.some(vm.structureModel, isEqualStructures)) {
            onChangeStructure();
        }

        vm.openEditor = openEditor;
        vm.importStructure = importStructure;
        vm.exportStructure = exportStructure;

        bindEvents();
    }

    function isEqualStructures(value, key) {
        return !(key === 'entered' || key === 'structureScheme') && (vm.model && vm.model[key] === value);
    }

    function buildStructure(fromStructure) {
        var structure = fromStructure || {};

        return {
            image: structure.image || null,
            molfile: structure.molfile || null,
            structureId: structure.structureId || null
        };
    }

    function getInitModel() {
        return buildStructure(vm.model);
    }

    function updateModel() {
        vm.structureModel = getInitModel();
        updateMolfileImage(vm.structureModel);
    }

    function updateMolfileImage(structure) {
        if (!structure || !structure.molfile || structure.image) {
            return;
        }

        $http({
            url: 'api/renderer/' + vm.structureType + '/image',
            method: 'POST',
            data: structure.molfile
        }).success(function(result) {
            vm.structureModel.image = result.image;
            onChangeStructure();
        });
    }

    function bindEvents() {
        $scope.$watch('vm.modelTrigger', updateModel);
    }

    function onChangeStructure() {
        vm.onChanged({structure: vm.structureModel});
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

    function updateModelRestriction(data) {
        vm.model.restrictions.structure = vm.model.restrictions.structure || {};
        vm.model.restrictions.structure.molfile = data.structure || data.molfile;
        vm.model.restrictions.structure.image = data.image;
    }

    function setRenderedStructure(data) {
        _.assign(vm.structureModel, data);

        if (vm.model && vm.model.restrictions) {
            updateModelRestriction(data);
        }

        onChangeStructure();
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
            setRenderedStructure({
                molfile: null,
                structureId: null,
                image: null
            });
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
        if (vm.isReadonly) {
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
        modalInstance.result.then(successEditStructure);
    }

    function successEditStructure(structure) {
        if (vm.autosave && structure) {
            saveNewStructure(structure);
        } else {
            setStructure(structure);
        }
    }

    function importStructure($event) {
        $event.stopPropagation();
        if (vm.isReadonly) {
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
        modalInstance.result.then(successEditStructure);
    }

    function exportStructure($event) {
        $event.stopPropagation();
        if (vm.isReadonly) {
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
                    return vm.structureModel.molfile;
                },
                structureType: function() {
                    return vm.structureType;
                }
            }
        });
    }
}
