'use strict';

angular.module('indigoeln')
    .controller('TemplateDetailController', function ($scope, $rootScope, $stateParams, entity, Template, $builder) {
        $scope.template = entity;
        $builder.forms['default'].length = 0;
        _.each($scope.template.templateContent, function (item) {
            if (item) {
                $builder.addFormObject("default", item);
            }
        });
        $scope.load = function (id) {
            Template.get({id: id}, function (result) {
                $scope.template = result;
            });
        };
    });
