angular.module('indigoeln').controller('EditMeltingPointController',
    function ($scope, $rootScope, $uibModalInstance, data) {
        $scope.meltingPoint = data || {};

        var resultToString = function () {
            if ($scope.meltingPoint.lower && $scope.meltingPoint.upper && $scope.meltingPoint.comments) {
                return 'Range ' + $scope.meltingPoint.lower + ' ~ ' + $scope.meltingPoint.upper +
                    '\xB0C, ' + $scope.meltingPoint.comments;
            } else if ($scope.meltingPoint.lower && $scope.meltingPoint.upper) {
                return 'Range ' + $scope.meltingPoint.lower + ' ~ ' + $scope.meltingPoint.upper + '\xB0C';
            }
        };

        $scope.save = function () {
            $scope.meltingPoint.asString = resultToString();
            $uibModalInstance.close($scope.meltingPoint);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });
