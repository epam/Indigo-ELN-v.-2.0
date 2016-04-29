/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
'use strict';
angular.module('indigoeln')
    .directive('conceptDetails', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/conceptDetails/conceptDetails.html',
            controller: function ($scope, Principal) {
                $scope.model.conceptDetails = $scope.model.conceptDetails || {};
                $scope.model.conceptDetails.experimentCreator = $scope.model.conceptDetails.experimentCreator ||
                    {name: Principal.getIdentity().fullName};
            }
        };
    });