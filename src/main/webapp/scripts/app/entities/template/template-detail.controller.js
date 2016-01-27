'use strict';

angular.module('indigoeln')
    .controller('TemplateDetailController', function ($scope, $rootScope, $stateParams, entity, Template) {
        $scope.template = entity;
        $scope.load = function (id) {
            Template.get({id: id}, function (result) {
                $scope.template = result;
            });
        };
        //var unsubscribe = $rootScope.$on('indigoeln:templateUpdate', function (event, result) {
        //    $scope.template = result;
        //});
        //$scope.$on('$destroy', unsubscribe);

    });
