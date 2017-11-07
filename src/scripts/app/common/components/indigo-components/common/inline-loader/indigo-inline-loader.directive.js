var template = require('./inline-loader.html');

function indigoInlineLoader() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            promise: '=',
            onStatusChanged: '&'
        },
        controller: IndigoInlineLoaderController,
        controllerAs: 'vm',
        bindToController: true
    };
}

IndigoInlineLoaderController.$inject = ['$scope'];

function IndigoInlineLoaderController($scope) {
    var vm = this;

    init();

    function init() {
        bindEvents();
    }

    function bindEvents() {
        $scope.$watch('vm.promise.$$state', function(val) {
            vm.isLoading = (val && val.status === 0);
            if (vm.onStatusChanged) {
                vm.onStatusChanged({completed: !vm.isLoading});
            }
        }, true);
    }
}

module.export = indigoInlineLoader;
