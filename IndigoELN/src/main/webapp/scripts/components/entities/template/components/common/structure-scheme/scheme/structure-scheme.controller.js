angular
    .module('indigoeln')
    .controller('StructureSchemeController', StructureSchemeController);

/* @ngInject */
function StructureSchemeController($scope, $q, $attrs, $http, $uibModal, $rootScope, Alert, EntitiesBrowser) {
    var vm = this;
    var type = $attrs.myStructureType;
    var batchSummarySelected;
    var newReactionScheme;
    vm.structureType = type;
    vm.myTitle = $attrs.myTitle;
    vm.myAutosave = $attrs.myAutosave === 'true';
    vm.model = $scope.model;
    vm.model[type] = $scope.model[type] || {structureScheme: {}};
    vm.onChange = $scope.onChange;

    vm.openEditor = openEditor;
    vm.importStructure = importStructure;
    vm.exportStructure = exportStructure;

    init();

    function init() {
        if (type === 'molecule') {
            batchSummarySelected = $scope.$on('batch-summary-row-selected', function (event, data) {
                var row = data.row;
                if (row && row.structure && row.structure.structureType === type) {
                    vm.model[type].image = $scope.share.selectedRow.structure.image;
                    vm.model[type].structureMolfile = $scope.share.selectedRow.structure.molfile;
                    vm.model[type].structureId = $scope.share.selectedRow.structure.structureId;
                    $http({
                        url: 'api/renderer/' + type + '/image',
                        method: 'POST',
                        data: vm.model[type].structureMolfile
                    }).success(function (result) {
                        vm.model[type].image = result.image;
                    });
                } else if (!_.isUndefined(row)) {
                    vm.model[type].image = vm.model[type].structureMolfile = vm.model[type].structureId = null;
                }
            });
        }
        if (type === 'reaction') {
            newReactionScheme = $scope.$on('new-reaction-scheme', function (event, data) {
                vm.model[type].image = data.image;
                vm.model[type].structureMolfile = data.molfile;
            });
        }

        var unsubscribe = $scope.$watch('vm.model.' + type + '.structureId', function () {
            if ($scope.share) {
                $scope.share[type] = vm.model[type].structureMolfile;
            }
        });
        $scope.$on('$destroy', function () {
            unsubscribe();
            if (batchSummarySelected) {
                batchSummarySelected();
            }
            if (newReactionScheme) {
                newReactionScheme();
            }
        });
    }


    function renderStructure(type, structure) {
        var deferred = $q.defer();
        $http({
            url: 'api/renderer/' + type + '/image',
            method: 'POST',
            data: structure
        }).success(function (result) {
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
        }).success(function (result) {
            deferred.resolve(result.empty);
        });
        return deferred.promise;
    }

    function setRenderedStructure(type, data) {
        vm.model[type].image = data.image;
        vm.model[type].structureMolfile = data.structure;
        vm.model[type].structureId = data.structureId;
        if ($scope.share && $scope.share.selectedRow && type === 'molecule') {
            $scope.share.selectedRow.structure = $scope.share.selectedRow.structure || {};
            $scope.share.selectedRow.structure.image = data.image;
            $scope.share.selectedRow.structure.structureType = type;
            $scope.share.selectedRow.structure.molfile = data.structure;
            $scope.share.selectedRow.structure.structureId = data.structureId;
            $rootScope.$broadcast('product-batch-structure-changed', $scope.share.selectedRow);
        }
        if (vm.model.restrictions) {
            vm.model.restrictions.structure = vm.model.restrictions.structure || {};
            vm.model.restrictions.structure.molfile = data.structure;
            vm.model.restrictions.structure.image = data.image;
        }
        if(vm.onChange){
            vm.onChange({model: vm.model});
        }

    }

    function setStructure(type, structure, structureId) {
        if (structure) {
            renderStructure(type, structure).then(function (result) {
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
        isEmptyStructure(type, structure).then(function (empty) {
            if (empty) {
                setStructure(type);
            } else {
                $http({
                    url: 'api/bingodb/' + type + '/',
                    method: 'POST',
                    data: structure
                }).success(function (structureId) {
                    setStructure(type, structure, structureId);
                }).error(function () {
                    Alert.error('Cannot save the structure!');
                });
            }
        });
    }

    function openEditor($event) {
        $event.stopPropagation();
        if ($scope.myReadonly) {
            return;
        }
        // open editor with pre-defined structure (prestructure)
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/editor/structure-editor-modal.html',
            controller: 'StructureEditorModalController',
            windowClass: 'structure-editor-modal',
            resolve: {
                prestructure: function () {
                    return vm.model[type].structureMolfile;
                },
                editor: function () {
                    // TODO: get editor name from user's settings; ketcher by default
                    return 'KETCHER';
                }
            }
        });

        // process structure if changed
        modalInstance.result.then(function (structure) {
            if (vm.myAutosave && structure) {
                saveNewStructure(type, structure);
            }
            setStructure(type, structure);
            EntitiesBrowser.setCurrentFormDirty();

        });
    }

    function importStructure($event) {
        $event.stopPropagation();
        if ($scope.myReadonly) {
            return;
        }
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/import/structure-import-modal.html',
            controller: 'StructureImportModalController',
            windowClass: 'structure-import-modal'
        });

        // set structure if picked
        modalInstance.result.then(function (structure) {
            if ($scope.myAutosave) {
                setStructure(type, structure);
                saveNewStructure(type, structure);
            } else {
                setStructure(type, structure);
            }
        });
    }

    function exportStructure($event) {
        $event.stopPropagation();
        if ($scope.myReadonly) {
            return;
        }
        $uibModal.open({
            animation: true,
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/export/structure-export-modal.html',
            controller: 'StructureExportModalController',
            windowClass: 'structure-export-modal',
            resolve: {
                structureToSave: function () {
                    return vm.model[type].structureMolfile;
                },
                structureType: function () {
                    return type;
                }
            }
        });
    }
}
