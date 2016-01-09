'use strict';

angular.module('indigoeln').controller('homeController', homeController);

homeController.$inject = ['$q', '$rootScope', '$scope', 'authService', 'websocketService', '$location'];

function homeController($q, $rootScope, $scope, authService, websocketService, $location) {

    $rootScope.MODEL = {
        loggedUser: null
    };

    $scope.checkLoggedUser = function () {
        var deferred = $q.defer();
        if ($rootScope.MODEL.loggedUser !== null) {
            deferred.resolve($rootScope.MODEL.loggedUser);
        } else {
            authService.getAuth().then(function (response) {
                if (response.data) {
                    $rootScope.MODEL.loggedUser = response.data;
                    deferred.resolve($rootScope.MODEL.loggedUser);
                } else {
                    // no user found
                    $location.path('/login');
                    deferred.reject();
                }
            }, function () {
                // error callback
                $location.path('/login');
                deferred.reject();
            });
        }
        return deferred.promise;
    };

    var init = function () {
        $scope.checkLoggedUser().then(function (user) {
            websocketService.connect();
            // To do further initialization
        });
    };
    init();

}