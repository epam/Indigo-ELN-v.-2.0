'use strict';

angular.module('indigoeln')
    .controller('StructureSchemeController', function ($scope, $http, $uibModal) {

        // TODO: implement recieving that info from server
        var BINGO_URL = 'http://ecse0010026a.epam.com:12345/',
            MONGO_URL = 'http://ecse0010026a.epam.com:8080/indigoeln/';
        //var BINGO_URL =   'http://localhost:12345/',
        //    MONGO_URL = 'http://localhost:3000/';


        var PIC_WIDTH = 10,
            PIC_HEIGHT = 5;

        // TODO: find out where take the info from
        $scope.structureType = 'molecule'; // molecule (default value) or reaction
        $scope.model.structureScheme = $scope.model.structureScheme || {};
        $scope.structure = null;

        // watch structure's id and update structure and its image if changed
        $scope.$watch('model.structureScheme.structureId', function() {
            if ($scope.model.structureScheme.structureId) {
                $http({
                    url: getRendererUrl($scope.structureType, $scope.model.structureScheme.structureId),
                    method: "GET",
                    params: {
                        width: PIC_WIDTH,
                        height: PIC_HEIGHT
                    }
                }).success(function(result){
                        $scope.structure = result.structure;
                        $scope.image = result.image;
                }).error(function(){
                        $scope.image = null;
                        console.info('Cannot render the structure.');
                });
            }
        }, true);

        $scope.openEditor = function () {

            // open editor with pre-defined structure (prestructure)
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/entities/template/components/structureScheme/structure-editor-modal.html',
                controller: 'StructureEditorModalController',
                windowClass: 'structure-editor-modal',
                resolve: {
                    prestructure: function () {
                        return $scope.structure;
                    },
                    editor: function() {
                        // TODO: get editor name from user's settings; ketcher by default
                        return "KETCHER";
                    }
                }
            });

            // process structure if changed
            modalInstance.result.then(function (structure) {
                if (structure) {
                    // save structure in Bingo db and get its id
                    saveNewStructure(structure, $scope.structureType);
                }
            });
        };

        $scope.importStructure = function () {

            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/components/entities/template/components/structureScheme/structure-import-modal.html',
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
                templateUrl: 'scripts/components/entities/template/components/structureScheme/structure-export-modal.html',
                controller: 'StructureExportModalController',
                windowClass: 'structure-export-modal',
                resolve: {
                    structureToSave: function () {
                        return $scope.structure;
                    },
                    structureType: function () {
                        return $scope.structureType;
                    }
                }
            });
        };

        // URL to get image
        var getRendererUrl = function(type, id) {
            return MONGO_URL + 'api/renderer/' + type + '/' + id  + '/image';
        }

        // HTTP POST to save new structure into Bingo DB and get its id
        var saveNewStructure = function(structure, type) {
            $http({
                url: BINGO_URL + type + '/',
                method: "POST",
                data: structure
            }).success(function(result){
                $scope.model.structureScheme.structureId = result.id;
            }).error(function(){
                console.info('Cannot save the structure.');
            });
        }
    });