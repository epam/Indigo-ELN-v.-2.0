var template = require('./app-layout.html');

function appLayout() {
    return {
        scope: true,
        restrict: 'E',
        controller: AppPageController,
        controllerAs: 'vm',
        template: template,
        bindToController: true
    };

    /* @ngInject */
    function AppPageController($rootScope, $scope, $cookies, $window, wsService, $timeout) {
        var vm = this;
        var mobileViewWidth = 992;
        var updateToggle;
        var windowElement;
        var subscribers = [];

        init();

        function init() {
            vm.isOpen = true;
            windowElement = angular.element($window);
            updateToggle = createDebounce();

            vm.toggleSidebar = toggleSidebar;
            bindEvents();
        }

        function toggleSidebar() {
            vm.isOpen = !vm.isOpen;
            $cookies.put('toggle', vm.isOpen);
        }

        function bindSubscribes() {
            onSubscribe('/topic/experiment_status', 'experiment-status-changed');
            onSubscribe('/topic/registration_status', 'batch-registration-status-changed');
            onSubscribe('/user/topic/entity_updated', 'entity-updated');
        }

        function onSubscribe(destination, broadcastEventName) {
            wsService.subscribe(destination).then(function(subscribe) {
                subscribers.push(subscribe);
                subscribe.onServerEvent(function(statuses) {
                    $rootScope.$broadcast(broadcastEventName, statuses);
                });
            });
        }

        function bindEvents() {
            windowElement.bind('resize', updateToggle);
            bindSubscribes();

            $scope.$on('$destroy', function() {
                _.forEach(subscribers, function(subscribe) {
                    subscribe.unSubscribe();
                });
                windowElement.off('resize', updateToggle);
            });
        }

        function createDebounce() {
            return _.debounce(function() {
                $timeout(function() {
                    if (windowElement.width() >= mobileViewWidth) {
                        if (!_.isUndefined($cookies.get('toggle'))) {
                            vm.isOpen = $cookies.get('toggle');
                        } else {
                            vm.isOpen = true;
                        }
                    } else {
                        vm.isOpen = false;
                    }
                });
            }, 100);
        }
    }
}

module.exports = appLayout;
