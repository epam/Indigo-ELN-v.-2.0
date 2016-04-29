angular.module('indigoeln')
    .controller('SetSelectValueController', function ($scope, name, values, $uibModalInstance) {
        $scope.name = name;
        $scope.values = values;
        $scope.save = function () {
            $uibModalInstance.close($scope.value);
        };
        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };

    });
