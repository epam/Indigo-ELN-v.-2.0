angular
    .module('indigoeln')
    .directive('appPage', function() {
        return {
            restrict: 'E',
            controller: appPageController,
            controllerAs: 'vm',
            templateUrl: 'scripts/components/app-page/app-page.component.html',
            bindToController: true
        };
    });

function appPageController($rootScope, $scope, $cookieStore, $window, WSService, $timeout) {
    var vm = this;
    var mobileViewWidth = 992;
    var updateToggle;
    var windowElement;
    var subscribers = [];

    init();

    function init() {
        windowElement = angular.element($window);
        updateToggle = createDebounce();
        updateToggle();
        bindEvents();
    }

    vm.onMouseWheel = function($event) {
        var prevent = function() {
            $event.stopPropagation();
            $event.preventDefault();
            $event.returnValue = false;
            return false;
        };
        return prevent();
    };

    function bindSubscribes() {
        onSubscribe('experiment_status', 'experiment-status-changed');
        onSubscribe('registration_status', 'batch-registration-status-changed');
        onSubscribe('entity_updated', 'entity-updated');
    }

    function onSubscribe(destination, broadcastEventName) {
        WSService.subscribe(destination).then(function(subscribe) {
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
                    if (angular.isDefined($cookieStore.get('toggle'))) {
                        vm.toggle = $cookieStore.get('toggle');
                    } else {
                        vm.toggle = true;
                    }
                } else {
                    vm.toggle = false;
                }
            });
        });
    }
}