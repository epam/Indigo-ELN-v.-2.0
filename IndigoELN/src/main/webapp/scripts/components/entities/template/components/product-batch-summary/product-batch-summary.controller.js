/**
 * Created by Stepan_Litvinov on 3/2/2016.
 */
angular.module('indigoeln')
    .controller('ProductBatchSummaryController',
    function($scope) {
        $scope.model = $scope.model || {};
        $scope.structureSize = 0.3;

        $scope.showStructure = function(value) {
            $scope.isStructure = value;
        };
    }
    );
