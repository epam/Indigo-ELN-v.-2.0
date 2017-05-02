angular.module('indigoeln')
    .controller('NavbarController', function ($scope, $state, $rootScope, $q, Principal, Auth, EntitiesBrowser, EntitiesCache) {
        
        Principal.identity().then(function(user) {
            $scope.user =  user;
            console.warn('user', user)
        })

        $scope.logout = function () {
            var userId = Principal.getIdentity().id;
            Auth.logout();
            EntitiesCache.clearAll();
            $rootScope.$broadcast('user-logout', {id: userId});
            $state.go('login');
        };


        var unsubscribe = $scope.$watch(function () {
            return EntitiesBrowser.activeTab;
        }, function (value) {
            $scope.activeTab = value;
        });

        $scope.$on('$destroy', function () {
            unsubscribe();
        });

    });