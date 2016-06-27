/**
 * Created by Selector on 21.02.2016.
 */
angular
    .module('indigoeln')
    .controller('AppPageController', function ($rootScope, $scope, $cookieStore, $window, experimentStatusSubscriber, batchStatusSubscriber, Config) {
        /**
         * Sidebar Toggle & Cookie Control
         */
        var mobileView = 992;

        $scope.getWidth = function () {
            return $window.innerWidth;
        };

        $scope.$watch($scope.getWidth, function (newValue) {
            if (newValue >= mobileView) {
                if (angular.isDefined($cookieStore.get('toggle'))) {
                    $scope.toggle = !$cookieStore.get('toggle') ? false : true;
                } else {
                    $scope.toggle = true;
                }
            } else {
                $scope.toggle = false;
            }

        });

        Config.load({}, function(config) {
            $rootScope.$broadcast('config-loaded', config);
        });

        $scope.toggleSidebar = function () {
            $scope.toggle = !$scope.toggle;
            $cookieStore.put('toggle', $scope.toggle);
        };

        $window.onresize = function () {
            $scope.$apply();
        };

        $scope.$on('$destroy', experimentStatusSubscriber.unSubscribe);
        $scope.$on('$destroy', batchStatusSubscriber.unSubscribe);

        //todo: refactoring
        experimentStatusSubscriber.onServerEvent(function (statuses) {
            $rootScope.$broadcast('experiment-status-changed', statuses);
        });
        batchStatusSubscriber.onServerEvent(function (statuses) {
            $rootScope.$broadcast('batch-registration-status-changed', statuses);
        });


        $scope.onMouseWheel = function ($event) {
            var prevent = function () {
                $event.stopPropagation();
                $event.preventDefault();
                $event.returnValue = false;
                return false;
            };
            return prevent();
        };
    });