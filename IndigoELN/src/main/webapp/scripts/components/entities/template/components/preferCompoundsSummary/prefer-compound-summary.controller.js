angular.module('indigoeln')
    .controller('PreferCompoundSummaryController',
        function ($scope) {

            $scope.model = $scope.model || {};
            $scope.structureSize = 0.3;

            $scope.showStructure = function (value) {
                $scope.isStructure = value;
            }
        });