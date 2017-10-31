(function() {
    angular
        .module('indigoeln.componentButtons')
        .directive('deleteBatches', deleteBatchesDirective);

    /* @ngInject */
    function deleteBatchesDirective() {
        return {
            restrict: 'E',
            scope: {
                isReadonly: '=',
                batches: '=?',
                deleteBatches: '=?',
                deleteBatch: '=?',
                onRemoveBatches: '&'
            },
            templateUrl: 'scripts/app/common/directives/component-buttons/delete-batches/delete-batches.html',
            controller: deleteBatchesController,
            controllerAs: 'vm',
            bindToController: true,
            link: function($scope, $element, $attr, controllers) {
                $element.addClass('component-button');
            }
        };
    }

    deleteBatchesController.$inject = [];

    function deleteBatchesController() {
        var vm = this;

        init();

        function init() {
            vm.deleteBatches = deleteBatches;
        }

        function deleteBatches() {
            vm.onRemoveBatches({batches: getBatches()});
        }

        function getBatches() {
            if (_.isArray(vm.deleteBatches)) {
                return vm.deleteBatches;
            }
            if (_.isArray(vm.batches)) {
                return _.filter(vm.batches, {$$select: true});
            }

            return vm.deleteBatch ? [vm.deleteBatch] : null;
        }
    }
})();
