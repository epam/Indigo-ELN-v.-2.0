var structureEditorModalTemplate = require('../structure-editor-modal/structure-editor-modal.html');
var structureImportModalTemplate = require('../import/structure-import-modal.html');
var structureExportModalTemplate = require('../export/structure-export-modal.html');

StructureSchemeController.$inject = ['$scope', 'apiUrl', '$http', '$uibModal', 'notifyService', 'calculationService'];

function StructureSchemeController($scope, apiUrl, $http, $uibModal, notifyService, calculationService) {
    var vm = this;

    var dlg;

    init();

    function init() {
        vm.structureModel = buildStructure();
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

    function buildStructure() {
        var structure = vm.model || {};

        return {
            image: structure.image || null,
            molfile: structure.molfile || null,
            structureId: structure.structureId || null
        };
    }

    function updateModel() {
        vm.structureModel = buildStructure();
        updateMolfileImage(vm.structureModel);
    }

    function updateMolfileImage(structure) {
        if (!structure || !structure.molfile || structure.image) {
            return;
        }
        calculationService
            .getImageForStructure(structure.molfile, vm.structureType)
            .then(function(image) {
                structure.image = image;
                onChangeStructure();
            });
    }

    function bindEvents() {
        $scope.$watch('vm.modelTrigger', updateModel);

        $scope.$on('$destroy', function() {
            closeDialog();
        });
    }

    function onChangeStructure() {
        vm.onChanged({structure: _.clone(vm.structureModel)});
    }

    function isEmptyStructure(structure) {
        return $http({
            url: apiUrl + 'bingodb/' + vm.structureType + '/empty',
            method: 'POST',
            data: structure
        }).then(function(result) {
            return result.data;
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

    function clearStructure() {
        setRenderedStructure({
            molfile: null,
            structureId: null,
            image: null
        });
    }

    function setStructure(structure, structureId) {
        isEmptyStructure(structure)
            .then(function(result) {
                if (!result.empty) {
                    calculationService.getImageForStructure(structure, vm.structureType).then(function(image) {
                        setRenderedStructure({
                            molfile: structure,
                            structureId: structureId,
                            image: image
                        });
                    });
                } else {
                    clearStructure();
                }
            });
    }

    // HTTP POST to save new structure into Bingo DB and get its id
    function saveNewStructure(structure) {
        isEmptyStructure(structure)
            .then(function(result) {
                if (result.empty) {
                    clearStructure();
                } else {
                    $http({
                        url: apiUrl + 'bingodb/' + vm.structureType + '/',
                        method: 'POST',
                        data: structure
                    }).then(function(response) {
                        setStructure(structure, response.data);
                    }).catch(function() {
                        notifyService.error('Cannot save the structure!');
                    });
                }
            });
    }

    function openEditor() {
        // open editor with pre-defined structure (prestructure)
        closeDialog();
        dlg = $uibModal.open({
            animation: true,
            template: structureEditorModalTemplate,
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
        dlg.result.then(successEditStructure);
    }

    function successEditStructure(structure) {
        if (vm.autosave && structure) {
            saveNewStructure(structure);
        } else {
            setStructure(structure);
        }
    }

    function importStructure() {
        closeDialog();
        dlg = $uibModal.open({
            animation: true,
            template: structureImportModalTemplate,
            controller: 'StructureImportModalController',
            controllerAs: 'vm',
            windowClass: 'structure-import-modal'
        });

        // set structure if picked
        dlg.result.then(successEditStructure);
    }

    function exportStructure() {
        closeDialog();
        dlg = $uibModal.open({
            animation: true,
            template: structureExportModalTemplate,
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

    function closeDialog() {
        if (dlg) {
            dlg.dismiss();
            dlg = null;
        }
    }
}

module.exports = StructureSchemeController;
