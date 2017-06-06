angular.module('indigoeln')
    .controller('TemplateDetailController', function ($scope, $rootScope, $stateParams, pageInfo, Template) {
        //$scope.template = pageInfo.entity;
        $scope.load = function (id) {
            Template.get({id: id}, function (result) {
                $scope.template = result;
                console.warn($scope.template)
            });
        };
        if ($stateParams.id) {
        	$scope.load($stateParams.id);
        }
    });
