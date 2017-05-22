angular.module('indigoeln')
    .controller('LoginController', function ($rootScope, $scope, $state, $timeout, Auth, Principal) {
        $scope.user = {};
        $scope.errors = {};
        $scope.rememberMe = true;
        $timeout(function () {
            angular.element('[ng-model="username"]').focus();
        }, 0, false);
        $scope.login = function (event) {
            $scope.loading = true;
            event.preventDefault();
            Auth.login({
                username: $scope.username,
                password: $scope.password,
                rememberMe: $scope.rememberMe
            }).then(function () {
                $scope.authenticationError = false;
                console.log('$state', $state)
                //$rootScope.back();
                Principal.identity(true).then(function(user) {
                    $state.go('experiment');
                    $scope.loading = false;
                })
            }).catch(function () {
                $scope.authenticationError = true;
                $scope.shake = !$scope.shake;
                $scope.loading = false;
            });
        };
    });
