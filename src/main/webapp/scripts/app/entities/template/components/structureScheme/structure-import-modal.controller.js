'use strict';

angular.module('indigoeln')
    .controller('StructureImportControllerModal', function ($scope, $uibModalInstance) {

        $scope.import = function() {
            $uibModalInstance.close($scope.content);
        };

        $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

});