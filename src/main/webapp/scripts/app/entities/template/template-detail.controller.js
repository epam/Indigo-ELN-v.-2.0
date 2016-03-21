'use strict';

angular.module('indigoeln')
    .controller('TemplateDetailController', function ($scope, $rootScope, $stateParams, pageInfo, Template) {
        $scope.template = pageInfo.entity;
        $scope.load = function (id) {
            Template.get({id: id}, function (result) {
                $scope.template = result;
            });
        };
    });
