angular.module('indigoeln')
    .controller('StructureSchemeController', function ($scope, $q, $attrs, $http, $uibModal, $rootScope) {
        // structure types: molecule, reaction
        var type = $attrs.myStructureType;
        $scope.structureType = type;
        $scope.myTitle = $attrs.myTitle;
        $scope.myAutosave = $attrs.myAutosave === 'true';
        var batchSummarySelected;
        var newReactionScheme;

        if (!$scope.model) {
            return;
        }
        $scope.model[type] = $scope.model[type] || {structureScheme: {}};

        var unsubscribe = $scope.$watch('model.' + type + '.structureId', function () {
            if($scope.share){
                $scope.share[type] = $scope.model[type].structureMolfile;
            }
        });
        $scope.$on('$destroy', function () {
            unsubscribe();
            if(batchSummarySelected){
                batchSummarySelected();
            }
            if(newReactionScheme){
                newReactionScheme();
            }
        });
        if (type === 'molecule') {
            batchSummarySelected = $scope.$on('batch-summary-row-selected', function (event, data) {
                var row = data.row;
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
        if (type === 'reaction') {
            newReactionScheme = $scope.$on('new-reaction-scheme', function (event, data) {
                $scope.model[type].image = data.image;
                $scope.model[type].structureMolfile = data.molfile;
            });
        }

        var renderStructure = function (type, structure) {
            var deferred = $q.defer();
            $http({
                url: 'api/renderer/' + type + '/image',
                method: 'POST',
                data: structure
            }).success(function (result) {
                deferred.resolve(result);
            });
            return deferred.promise;
        };

        var isEmptyStructure = function (type, structure) {
            var deferred = $q.defer();
            $http({
                url: 'api/bingodb/' + type + '/empty',
                method: 'POST',
                data: structure
            }).success(function (result) {
                deferred.resolve(result.empty);
            });
            return deferred.promise;
        };

        var setRenderedStructure = function (type, data) {
            $scope.model[type].image = data.image;
            $scope.model[type].structureMolfile = data.structure;
            $scope.model[type].structureId = data.structureId;
            if ($scope.share && $scope.share.selectedRow && type === 'molecule') {
                $scope.share.selectedRow.structure = $scope.share.selectedRow.structure || {};
                $scope.share.selectedRow.structure.image = data.image;
                $scope.share.selectedRow.structure.structureType = type;
                $scope.share.selectedRow.structure.molfile = data.structure;
                $scope.share.selectedRow.structure.structureId = data.structureId;
                $rootScope.$broadcast('product-batch-structure-changed', $scope.share.selectedRow);
            }
            if ($scope.model.restrictions) {
                $scope.model.restrictions.structure = $scope.model.restrictions.structure || {};
                $scope.model.restrictions.structure.molfile = data.structure;
                $scope.model.restrictions.structure.image = data.image;
            }
        };

        var setStructure = function (type, structure, structureId) {
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
        };

        // HTTP POST to save new structure into Bingo DB and get its id
        var saveNewStructure = function (type, structure) {
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
                        console.info('Cannot save the structure.');
                    });
                }
            });
        };

        $scope.openEditor = function ($event) {
            $event.stopPropagation();
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
                if ($scope.myAutosave) {
                    if (structure) {
                        saveNewStructure(type, structure);
                    }
                } else {
                    setStructure(type, structure);
                }
            });
        };

        $scope.importStructure = function ($event) {
            $event.stopPropagation();
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
                if ($scope.myAutosave) {
                    saveNewStructure(type, structure);
                } else {
                    setStructure(type, structure);
                }
            });
        };

        $scope.exportStructure = function ($event) {
            $event.stopPropagation();
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