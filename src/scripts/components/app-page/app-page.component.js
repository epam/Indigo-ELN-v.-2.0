angular
    .module('indigoeln')
    .directive('appPage', function() {
        return {
            scope: true,
            restrict: 'E',
            controller: AppPageController,
            controllerAs: 'vm',
            templateUrl: 'scripts/components/app-page/app-page.component.html',
            bindToController: true
        };
    });

AppPageController.$inject = ['$rootScope', '$scope', '$cookies', '$window', 'wsService', '$timeout'];

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
        onSubscribe('experiment_status', 'experiment-status-changed');
        onSubscribe('registration_status', 'batch-registration-status-changed');
        onSubscribe('entity_updated', 'entity-updated');
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
