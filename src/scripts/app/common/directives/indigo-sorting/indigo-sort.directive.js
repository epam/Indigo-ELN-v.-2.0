(function() {
    angular
        .module('indigoeln')
        .directive('indigoSort', indigoSort);

    function indigoSort() {
        return {
            restrict: 'A',
            scope: {
                indigoSort: '=',
                ascending: '=',
                callback: '&'
            },
            controller: IndigoSortController,
            controllerAs: 'vm',
            bindToController: true
        };

        /* @ngInject */
        function IndigoSortController() {
            var vm = this;

            $onInit();

            function $onInit() {
                vm.sort = sort;
            }

            function sort(field) {
                if (field !== vm.indigoSort) {
                    vm.ascending = true;
                } else {
                    vm.ascending = !vm.ascending;
                }
                vm.indigoSort = field;
                vm.callback();
            }
        }
    }
})();
