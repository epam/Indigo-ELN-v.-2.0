/**
 * Created by Stepan_Litvinov on 2/8/2016.
 */
'use strict';
angular.module('indigoeln')
    .directive('reactionDetails', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/reactionDetails/reactionDetails.html',
            controller: function ($scope, Principal, Dictionary) {
                $scope.model.reactionDetails = $scope.model.reactionDetails || {};
                $scope.model.reactionDetails.experimentCreator = $scope.model.reactionDetails.experimentCreator ||
                    {name: Principal.getIdentity().fullName};

                Dictionary.get({id: 'users'}, function (dictionary) {
                    $scope.users = dictionary.words;
                    $scope.model.reactionDetails.coAuthors = $scope.model.reactionDetails.coAuthors ||
                        _.where($scope.users, {name: Principal.getIdentity().fullName});
                });
            }
        };
    });