function indigoSort() {
    return {
        restrict: 'A',
        scope: {
            indigoSort: '=',
            isAscending: '=',
            onSort: '&'
        },
        controller: IndigoSortController,
        controllerAs: 'vm',
        bindToController: true
    };

    function IndigoSortController() {
        var vm = this;

        $onInit();

        function $onInit() {
            vm.sort = sort;
        }

        function sort(field) {
            if (field !== vm.indigoSort) {
                vm.isAscending = true;
            } else {
                vm.isAscending = !vm.isAscending;
            }

            vm.indigoSort = field;
            vm.onSort({predicate: field, isAscending: vm.isAscending});
        }
    }
}

module.exports = indigoSort;

