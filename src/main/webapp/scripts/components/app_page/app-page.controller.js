/**
 * Created by Selector on 21.02.2016.
 */
angular
    .module('indigoeln')
.controller('AppPageController', function ($rootScope, $scope, $cookieStore, $window, experimentStatusSubscriber, batchStatusSubscriber, entityChangedSubscriber) {
        /**
         * Sidebar Toggle & Cookie Control
         */
        var mobileView = 992;

        var updateToggle = _.debounce(function () {
            var initState = $scope.toggle;
            if ($($window).width() >= mobileView) {
                if (angular.isDefined($cookieStore.get('toggle'))) {
                    $scope.toggle = $cookieStore.get('toggle');
                } else {
                    $scope.toggle = true;
                }
            } else {
                $scope.toggle = false;
            }
            if ($scope.toggle !== initState) {
                $scope.$apply();
            }
        });
        updateToggle();


        $scope.toggleSidebar = function () {
            $scope.toggle = !$scope.toggle;
            $cookieStore.put('toggle', $scope.toggle);
        };

        angular.element($window).bind('resize', updateToggle);

        $scope.$on('$destroy', experimentStatusSubscriber.unSubscribe);
        $scope.$on('$destroy', batchStatusSubscriber.unSubscribe);

        //todo: refactoring
        experimentStatusSubscriber.onServerEvent(function (statuses) {
            $rootScope.$broadcast('experiment-status-changed', statuses);
        });
        batchStatusSubscriber.onServerEvent(function (statuses) {
            $rootScope.$broadcast('batch-registration-status-changed', statuses);
        });
        entityChangedSubscriber.onServerEvent(function (data) {
            $rootScope.$broadcast('entity-updated', data);
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