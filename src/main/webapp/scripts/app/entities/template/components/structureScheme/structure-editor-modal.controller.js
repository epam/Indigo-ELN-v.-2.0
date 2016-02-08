'use strict';

angular.module('indigoeln')
    .controller('StructureEditorModalController', function ($scope, $uibModalInstance, prestructure, editor) {

        // set attributes
        $scope.structure = {molfile: prestructure};
        $scope.editor = editor;

        $scope.ok = function() {
            $uibModalInstance.close($scope.structure.molfile);
        };

        $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

    });