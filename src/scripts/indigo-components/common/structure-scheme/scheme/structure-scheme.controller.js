angular
    .module('indigoeln.Components')
    .controller('StructureSchemeController', StructureSchemeController);

/* @ngInject */
function StructureSchemeController($scope, apiUrl, $http, $uibModal, notifyService, CalculationService) {
    var vm = this;

    init();

    function init() {
        vm.structureModel = getInitModel();
        if (vm.model && !_.some(vm.structureModel, isEqualStructures)) {
            onChangeStructure();
        }

        vm.openEditor = openEditor;
        vm.importStructure = importStructure;
        vm.exportStructure = exportStructure;

        bindEvents();
    }

    function isEqualStructures(value, key) {
        return !(key === 'entered' || key === 'structureScheme') && (vm.model[key] === value);
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
            url: apiUrl + 'renderer/' + vm.structureType + '/image',
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

    function isEmptyStructure(structure) {
        return $http({
            url: apiUrl + 'bingodb/' + vm.structureType + '/empty',
            method: 'POST',
            data: structure
        }).then(function(result) {
            return result.empty;
        });
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
            CalculationService.getImageForStructure(structure, vm.structureType).then(function(image) {
                setRenderedStructure({
                    molfile: structure,
                    structureId: structureId,
                    image: image
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
                    url: apiUrl + 'bingodb/' + vm.structureType + '/',
                    method: 'POST',
                    data: structure
                }).success(function(structureId) {
                    setStructure(structure, structureId);
                }).error(function() {
                    notifyService.error('Cannot save the structure!');
                });
            }
        });
    }

    function openEditor() {
        // open editor with pre-defined structure (prestructure)
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'scripts/indigo-components/common/structure-scheme/editor/structure-editor-modal.html',
            controller: 'StructureEditorModalController',
            controllerAs: 'vm',
            windowClass: 'structure-editor-modal',
            resolve: {
                prestructure: function() {
                    return vm.structureModel.molfile;
                },
                editor: function() {
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

    function importStructure() {
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'scripts/indigo-components/common/structure-scheme/import/structure-import-modal.html',
            controller: 'StructureImportModalController',
            controllerAs: 'vm',
            windowClass: 'structure-import-modal'
        });

        // set structure if picked
        modalInstance.result.then(successEditStructure);
    }

    function exportStructure() {
        $uibModal.open({
            animation: true,
            templateUrl: 'scripts/indigo-components/common/structure-scheme/export/structure-export-modal.html',
            controller: 'StructureExportModalController',
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
