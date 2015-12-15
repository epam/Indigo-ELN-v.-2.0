(function () {

    angular.module('indigoeln').controller('homeController', homeController);

    homeController.$inject = ['$rootScope', '$scope', 'authService', '$location'];

    function homeController($rootScope, $scope, authService, $location) {

        $rootScope.MODEL = {
            loggedUser: null
        };

        $scope.checkLoggedUser = function () {
            if ($rootScope.MODEL.loggedUser != null) {
                return true;
            }

            authService.getAuth().then(function(response) {
                if (response.data) {
                    $rootScope.MODEL.loggedUser = response.data;
                } else {
                    // no user found
                    $location.path("/login");
                }
            }, function() {
                // error callback
                $location.path("/login");
            });
            return false;
        };

        var init = function () {
            if (!$scope.checkLoggedUser()) {
                return;
            }
            // To do further initialization
        };

        init();

    }

})();