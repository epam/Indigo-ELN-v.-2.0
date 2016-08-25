angular.module('indigoeln')
    .controller('SetScalarValueController', function ($scope, name, $uibModalInstance) {
        $scope.name = name;
        $scope.save = function () {
            $uibModalInstance.close($scope.value);
        };
        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };

    });
