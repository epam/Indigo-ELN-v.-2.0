'use strict';

angular.module('indigoeln')
    .controller('StructureImportModalController', function ($scope, $uibModalInstance) {

        $scope.import = function() {
            $uibModalInstance.close($scope.content);
        };

        $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

});