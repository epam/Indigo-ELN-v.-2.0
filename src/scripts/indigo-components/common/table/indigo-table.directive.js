(function() {
    angular
        .module('indigoeln.componentsModule')
        .directive('indigoTable', indigoTable);

    function indigoTable() {
        return {
            restrict: 'E',
            transclude: true,
            scope: {
                indigoId: '@',
                indigoLabel: '@',
                indigoColumns: '=',
                indigoRows: '=',
                isReadonly: '=',
                selectedBatch: '=',
                selectedBatchTrigger: '=',
                indigoDraggableRows: '=',
                indigoDraggableColumns: '=',
                indigoHideColumnSettings: '=',
                indigoSearchColumns: '=',
                onSelectRow: '&',
                onRemoveBatches: '&',
                onChanged: '&',
                onChangedVisibleColumn: '&',
                onCloseCell: '&'
            },
            controller: 'IndigoTableController',
            controllerAs: 'vm',
            bindToController: true,
            compile: compile,
            templateUrl: 'scripts/indigo-components/common/table/table.html'
        };

        /* @ngInject */
        function compile($element, tAttrs) {
            if (tAttrs.indigoDraggableRows) {
                var $tBody = $element.find('tbody');
                $tBody.attr('dragula', '"my-table-rows"');
                $tBody.attr('dragula-model', 'vm.indigoRows');
            }
            if (tAttrs.indigoDraggableColumns) {
                var $tr = $element.find('thead tr');
                $tr.attr('dragula', '"my-table-columns"');
                $tr.attr('dragula-model', 'vm.columns');
            }
        }
    }
})();
