'use strict';

angular
    .module('indigoeln')
    .controller('StructureSchemeController', function ($scope, $http, $uibModal) {

        var BINGO_URL = 'http://localhost:12345/';
        var PIC_WIDTH = 100;
        var PIC_HEIGHT = 50;

        // TODO: get experiment from server and set structureId and strutureType values
        $scope.structureId = 3;
        $scope.strutureType = "molecule"; // "reaction"


        $http({
            url: BINGO_URL + $scope.strutureType + '/' + $scope.structureId + '?callback=JSON_CALLBACK',
            method: "JSONP",
            params: {
                width: PIC_WIDTH,
                height: PIC_HEIGHT
            }
        }).success(function(result){
                $scope.structure = result.structure;
                $scope.image = result.picture;
            })
            .error(function(data){
                console.info('The experiment does not contain a structure.');
            });


        $scope.openEditor = function () {

            // open editor with pre-defined structure (prestructure)
            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/template/components/structureScheme/structure-editor-modal.html',
                controller: 'StructureEditorModalController',
                windowClass: 'structure-editor-modal',
                resolve: {
                    prestructure: function () {
                        return $scope.structure;
                    },
                    editor: function() {
                        // TODO get editor name from user's settings; ketcher by default
                        return "KETCHER";
                    }
                }
            });

            // process structure if changed
            modalInstance.result.then(function (structure) {

                if (structure && structure !== $scope.structure) {

                    // save structure in Bingo db and get its id
                    var structureTypeTemp = structure.startsWith('$RXN') ? "reaction" : "molecule";
                    $http({
                        url: BINGO_URL + structureTypeTemp,
                        method: "POST",
                        data: {structure: structure}
                    }).success(function(result){
                            $scope.structureId = result.structure;
                            //$scope.image = result.picture;
                    }).error(function(data){
                            console.info('The experiment contains no saved structure.');
                        });
                }

            });
        };

        //$scope.copyReaction = function() {
        //};

        $scope.pasteReaction = function() {

        };

        $scope.cutReaction = function() {

        };


    });