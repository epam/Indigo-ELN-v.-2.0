angular
    .module('indigoeln')
    .controller('StructureSchemeController', structureSchemeController);

/* @ngInject */
function structureSchemeController($scope, $q, $http, $uibModal, $rootScope, Alert, EntitiesBrowser) {
    var vm = this;

    function init() {
        if (!vm.model) {
            return;
        }

        vm.model[vm.structureType] = vm.model[vm.structureType] || {structureScheme: {}};
        bindEvents();
    }

    vm.openEditor = function($event) {
        $event.stopPropagation();
        if (vm.readonly) {
            return;
        }
        // open editor with pre-defined structure (prestructure)
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/editor/structure-editor-modal.html',
            controller: 'StructureEditorModalController',
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
            updateSructure(structure);

            // TODO: Probably this code is redundant
            var currentForm = EntitiesBrowser.getCurrentForm();

            if (currentForm) {
                currentForm.$setDirty(true);
            }
        });
    };

    vm.importStructure = function($event) {
        $event.stopPropagation();
        if (vm.readonly) {
            return;
        }
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/import/structure-import-modal.html',
            controller: 'StructureImportModalController',
            windowClass: 'structure-import-modal'
        });

        // set structure if picked
        modalInstance.result.then(function(structure) {
            updateSructure(structure);
        });
    };

    vm.exportStructure = function($event) {
        $event.stopPropagation();
        if (vm.readonly) {
            return;
        }
        $uibModal.open({
            animation: true,
            templateUrl: 'scripts/components/entities/template/components/common/structure-scheme/export/structure-export-modal.html',
            controller: 'StructureExportModalController',
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
    };

    function bindEvents() {
        $scope.$watch('vm.model.' + vm.structureType + '.structureId', function() {
            if ($scope.share) {
                $scope.share[vm.structureType] = vm.model[vm.structureType].structureMolfile;
            }
        });

        if (vm.structureType === 'molecule') {
            $scope.$on('batch-summary-row-selected', function(event, data) {
                var row = data.row;
                if (row && row.structure && row.structure.structureType === vm.structureType) {
                    vm.model[vm.structureType].image = $scope.share.selectedRow.structure.image;
                    vm.model[vm.structureType].structureMolfile = $scope.share.selectedRow.structure.molfile;
                    vm.model[vm.structureType].structureId = $scope.share.selectedRow.structure.structureId;
                    $http({
                        url: 'api/renderer/' + vm.structureType + '/image',
                        method: 'POST',
                        data: vm.model[vm.structureType].structureMolfile
                    }).success(function(result) {
                        vm.model[vm.structureType].image = result.image;
                    });
                } else if (!_.isUndefined(row)) {
                    vm.model[vm.structureType].image = vm.model[vm.structureType].structureMolfile = vm.model[vm.structureType].structureId = null;
                }
            });
        }

        if (vm.structureType === 'reaction') {
            $scope.$on('new-reaction-scheme', function(event, data) {
                vm.model[vm.structureType].image = data.image;
                vm.model[vm.structureType].structureMolfile = data.molfile;
            });
        }
    }

    function renderStructure(type, structure) {
        var deferred = $q.defer();
        // TODO: extract it to service
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
        // TODO: extract it to service
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

    function updateSructure(newStructure) {
        if (vm.autosave && newStructure) {
            saveNewStructure(vm.structureType, newStructure);
        } else {
            setStructure(vm.structureType, newStructure);
        }
    }

    init();
}