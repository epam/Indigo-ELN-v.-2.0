(function () {

    angular.module('indigoeln').controller('loginController', loginController);

    loginController.$inject = ['$rootScope', '$scope', 'authService', '$location'];

    function loginController($rootScope, $scope, authService, $location) {

        $scope.LoginModel = {
            login: "",
            password: "",
            validated: false,
            error: null
        };

        $scope.doLogin = function () {

            $scope.LoginModel.validated = true;

            if ($scope.LoginModel.frmLogin.$valid) {
                authService.login($scope.LoginModel.username, $scope.LoginModel.password).then(function (response) {
                    $scope.LoginModel.error = null;
                    $rootScope.MODEL.loggedUser = response.data;

                    $location.path("/");
                }, function () {
                    $scope.LoginModel.error = "Authorization failed. Invalid login or password.";
                });
            }
        };

    }

})();