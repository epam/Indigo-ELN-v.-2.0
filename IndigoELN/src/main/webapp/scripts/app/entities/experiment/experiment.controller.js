angular.module('indigoeln')
    .controller('ExperimentController', function ($rootScope, $scope, $state, Dashboard, CONFIG) {
        $scope.experiments = [];
        $scope.predicate = 'id';
        $scope.reverse = true;
        $scope.page = 1;
        $scope.dView = 'open';
        $scope.loadAll = function () {
            Dashboard.get({}, function (result) {
                $scope.experiments = result;
            });
        };
        $scope.loadPage = function (page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.signatureServiceUrl = CONFIG['indigoeln.client.signatureservice.url'];

        $scope.refresh = function () {
            $scope.loadAll();
            $scope.clear();
        };

        $scope.clear = function () {
            $scope.experiment = {
                name: null,
                experimentNumber: null,
                templateId: null,
                id: null
            };
        };
    });
