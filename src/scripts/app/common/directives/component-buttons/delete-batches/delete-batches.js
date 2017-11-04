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
        template: require('./delete-batches.html'),
        controller: DeleteBatchesController,
        controllerAs: 'vm',
        bindToController: true,
        link: function ($scope, $element, $attr, controllers) {
            $element.addClass('component-button');
        }
    };

    function DeleteBatchesController() {
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
}

module.exports = deleteBatchesDirective;
