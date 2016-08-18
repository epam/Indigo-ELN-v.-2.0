angular.module('indigoeln')
    .controller('SetSelectValueController', function ($scope, name, values, dictionary, $uibModalInstance) {
        $scope.name = name;
        $scope.values = values;
        $scope.dictionary = dictionary;
        $scope.wrapper = {
            value: {}
        };
        $scope.save = function () {
            $uibModalInstance.close($scope.wrapper.value);
        };
        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };

    });
