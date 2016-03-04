'use strict';

angular.module('indigoeln')
    .constant('StructureSchemeConsts', {
        // TODO: implement recieving that info from server
        BINGO_URL: 'http://' + window.location.hostname + ':12345/'
    })
    .controller('StructureSchemeController', function ($scope, $attrs, $http, $uibModal, StructureSchemeConsts) {
        if (!$scope.model) {
            return;
        }
        $scope.model[$attrs.myStructureType] = $scope.model[$attrs.myStructureType] || {structureScheme: {}};
        $scope.myModel = $scope.model[$attrs.myStructureType];

        $scope.structureType = $attrs.myStructureType;
        $scope.myTitle = $attrs.myTitle;

        // watch structure's id and update structure and its image if changed
        $scope.$watch('myModel.structureId', function () {
            if ($scope.myModel.structureId) {
                $http({
                    url: getRendererUrl($scope.structureType),
                    method: 'POST',
                    data: $scope.myModel.structureMolfile
                }).success(function (result) {
                    $scope.image = result.image;
                    if ($scope.share.selectedRow) {
                        $scope.share.selectedRow.structure = $scope.share.selectedRow.structure || {};
                        $scope.share.selectedRow.structure.image = result.image;
                        $scope.share.selectedRow.structure.structureType = $scope.structureType;
                        $scope.share.selectedRow.structure.molfile = $scope.myModel.structureMolfile;
                        $scope.share.selectedRow.structure.structureId = $scope.myModel.structureId;
                    }
                }).error(function () {
                    $scope.image = null;
                    console.info('Cannot render the structure.');
                });
            }
        }, true);

        $scope.$watch('share.selectedRow', function (row) {
            if (row && row.structure && row.structure.structureType === $scope.structureType) {
                $scope.image = $scope.share.selectedRow.structure.image;
                $scope.myModel.structureMolfile = $scope.share.selectedRow.structure.molfile;
                $scope.myModel.structureId = $scope.share.selectedRow.structure.structureId;
            } else {
                $scope.image = $scope.myModel.structureMolfile = $scope.myModel.structureId = null;
            }
        });

        $scope.openEditor = function () {
            // open editor with pre-defined structure (prestructure)
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/entities/template/components/common/structureScheme/structure-editor-modal.html',
                controller: 'StructureEditorModalController',
                windowClass: 'structure-editor-modal',
                resolve: {
                    prestructure: function () {
                        return $scope.myModel.structureMolfile;
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
                    saveNewStructure(structure, $scope.structureType);
                }
            });
        };

        $scope.importStructure = function () {

            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/entities/template/components/common/structureScheme/structure-import-modal.html',
                controller: 'StructureImportModalController',
                windowClass: 'structure-import-modal'
            });

            // set structure if picked
            modalInstance.result.then(function (structure) {
                saveNewStructure(structure, $scope.structureType);
            });
        };

        $scope.exportStructure = function () {

            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/entities/template/components/common/structureScheme/structure-export-modal.html',
                controller: 'StructureExportModalController',
                windowClass: 'structure-export-modal',
                resolve: {
                    structureToSave: function () {
                        return $scope.myModel.structureMolfile;
                    },
                    structureType: function () {
                        return $scope.structureType;
                    }
                }
            });
        };

        // URL to get image
        var getRendererUrl = function (type) {
            return 'api/renderer/' + type + '/image';
        };

        // HTTP POST to save new structure into Bingo DB and get its id
        var saveNewStructure = function (structure, type) {
            $http({
                url: StructureSchemeConsts.BINGO_URL + type + '/',
                method: 'POST',
                data: structure
            }).success(function (result) {
                $scope.myModel.structureId = result.id;
                // set the renewed value if it's fine with bingo
                $scope.myModel.structureMolfile = structure;
            }).error(function () {
                console.info('Cannot save the structure.');
            });
        };
    });