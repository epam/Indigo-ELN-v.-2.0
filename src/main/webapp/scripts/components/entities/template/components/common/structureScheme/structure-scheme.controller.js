angular.module('indigoeln')
    .controller('StructureSchemeController', function ($scope, $attrs, $http, $uibModal, $rootScope) {
        // structure types: molecule, reaction
        var type = $attrs.myStructureType;
        $scope.structureType = type;
        $scope.myTitle = $attrs.myTitle;

        if (!$scope.model) {
            return;
        }
        $scope.model[type] = $scope.model[type] || {structureScheme: {}};

        var onStructureIdChange = function () {
            if ($scope.model[type].structureId) {
                $scope.share[type] = $scope.model[type].structureMolfile;
            }
        };
        $scope.$watch('model.' + type + '.structureId', onStructureIdChange);

        if (type === 'molecule') {
            $scope.$on('batch-summary-row-selected', function (event, row) {
                if (row && row.structure && row.structure.structureType === type) {
                    $scope.model[type].image = $scope.share.selectedRow.structure.image;
                    $scope.model[type].structureMolfile = $scope.share.selectedRow.structure.molfile;
                    $scope.model[type].structureId = $scope.share.selectedRow.structure.structureId;
                    $http({
                        url: 'api/renderer/' + type + '/image',
                        method: 'POST',
                        data: $scope.model[type].structureMolfile
                    }).success(function (result) {
                        $scope.model[type].image = result.image;
                    });
                } else if (!_.isUndefined(row)) {
                    $scope.model[type].image = $scope.model[type].structureMolfile = $scope.model[type].structureId = null;
                }
            });
        }

        // HTTP POST to save new structure into Bingo DB and get its id
        var saveNewStructure = function (structure, type) {
            $http({
                url: 'api/bingodb/' + type + '/',
                method: 'POST',
                data: structure
            }).success(function (structureId) {
                $http({
                    url: 'api/renderer/' + type + '/image',
                    method: 'POST',
                    data: structure
                }).success(function (result) {
                    $scope.model[type].image = result.image;
                    $scope.share = $scope.share || {};
                    if ($scope.share.selectedRow) {
                        $scope.share.selectedRow.structure = $scope.share.selectedRow.structure || {};
                        $scope.share.selectedRow.structure.image = result.image;
                        $scope.share.selectedRow.structure.structureType = type;
                        $scope.share.selectedRow.structure.molfile = structure;
                        $scope.share.selectedRow.structure.structureId = structureId;

                        if (type === 'molecule' && result.image) {
                            $rootScope.$broadcast('product-batch-structure-changed', $scope.share.selectedRow);
                        }
                    } else {
                        $scope.model[type].image = result.image;
                        // case of search by molecule
                        if ($scope.model.restrictions) {
                            $scope.model.restrictions.structure = $scope.model.restrictions.structure || {};
                            $scope.model.restrictions.structure.image = result.image;
                            $scope.model.restrictions.structure.molfile = structure;
                        }
                    }
                    $scope.model[type].structureId = result;
                    // set the renewed value if it's fine with bingo
                    $scope.model[type].structureMolfile = structure;
                });
            }).error(function () {
                console.info('Cannot save the structure.');
            });
        };

        $scope.openEditor = function () {
            if ($scope.myReadonly) {
                return;
            }
            // open editor with pre-defined structure (prestructure)
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/entities/template/components/common/structureScheme/structure-editor-modal.html',
                controller: 'StructureEditorModalController',
                windowClass: 'structure-editor-modal',
                resolve: {
                    prestructure: function () {
                        return $scope.model[type].structureMolfile;
                    },
                    editor: function () {
                        // TODO: get editor name from user's settings; ketcher by default
                        return 'KETCHER';
                    }
                }
            });

            // process structure if changed
            modalInstance.result.then(function (structure) {
                if (structure) {
                    saveNewStructure(structure, type);
                }
            });
        };

        $scope.importStructure = function () {
            if ($scope.myReadonly) {
                return;
            }
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/entities/template/components/common/structureScheme/structure-import-modal.html',
                controller: 'StructureImportModalController',
                windowClass: 'structure-import-modal'
            });

            // set structure if picked
            modalInstance.result.then(function (structure) {
                saveNewStructure(structure, type);
            });
        };

        $scope.exportStructure = function () {
            if ($scope.myReadonly) {
                return;
            }
            $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/entities/template/components/common/structureScheme/structure-export-modal.html',
                controller: 'StructureExportModalController',
                windowClass: 'structure-export-modal',
                resolve: {
                    structureToSave: function () {
                        return $scope.model[type].structureMolfile;
                    },
                    structureType: function () {
                        return type;
                    }
                }
            });
        };
    });