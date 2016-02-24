/**
 * Created by Selector on 21.02.2016.
 */
'use strict';

angular
    .module('indigoeln')
    .controller('AppPageController', function ($scope, $cookieStore) {


        /**
         * Sidebar Toggle & Cookie Control
         */
        var mobileView = 992;

        $scope.getWidth = function () {
            return window.innerWidth;
        };

        $scope.$watch($scope.getWidth, function (newValue, oldValue) {
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

        $scope.toggleSidebar = function () {
            $scope.toggle = !$scope.toggle;
            $cookieStore.put('toggle', $scope.toggle);
        };

        window.onresize = function () {
            $scope.$apply();
        };

        $scope.onMouseWheel = function ($event, $delta, $deltaX, $deltaY) {
            var $this = $(event.currentTarget),
                scrollTop = event.currentTarget.scrollTop,
                scrollHeight = event.currentTarget.scrollHeight,
                height = $this.height(),
                delta = ($event.type == 'DOMMouseScroll' ?
                $event.originalEvent.detail * -40 :
                    $event.originalEvent.wheelDelta),
                up = delta > 0;

            var prevent = function () {
                $event.stopPropagation();
                $event.preventDefault();
                $event.returnValue = false;
                return false;
            };

            if (!up && -delta > scrollHeight - height - scrollTop) {
                // Scrolling down, but this will take us past the bottom.
                $this.scrollTop(scrollHeight);
                return prevent();
            } else if (up && delta > scrollTop) {
                // Scrolling up, but this will take us past the top.
                $this.scrollTop(0);
                return prevent();
            }
        }
    });