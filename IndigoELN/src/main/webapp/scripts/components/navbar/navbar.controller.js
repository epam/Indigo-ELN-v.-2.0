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

        $scope.onSearchKeyup = function (e) {
            if (e.keyCode == 13) {
                $scope.search()
                e.preventDefault()
            }
        }
        $scope.search = function() {
            $scope.query = $scope.query.trim().toLowerCase();
            $state.go('entities.search-panel').then(function() {
                $rootScope.$broadcast('toggle-search', {query: $scope.query});
            });
        }
        var unsubscribe = $scope.$watch(function () {
            return EntitiesBrowser.getActiveTab();
        }, function (value) {
            $scope.activeTab = value;
        });

        $scope.$on('$destroy', function () {
            unsubscribe();
        });

    });