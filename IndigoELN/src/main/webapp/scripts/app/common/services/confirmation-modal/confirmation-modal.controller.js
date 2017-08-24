angular
    .module('indigoeln')
    .controller('ConfirmationModalController', ConfirmationModalController);

/* @ngInject */
function ConfirmationModalController($uibModalInstance, title, message, buttons) {
    var vm = this;

    init();

    function init() {
        vm.title = title;
        vm.message = message;
        vm.resolveText = buttons.yes;
        vm.rejectText = buttons.no;
        vm.cancelText = buttons.cancel;
        vm.isVisibleYes = !!buttons.yes;
        vm.isVisibleNo = !!buttons.no;
        vm.isVisibleCancel = !!buttons.cancel;

        vm.cancel = cancel;
        vm.resolve = resolve;
        vm.reject = reject;
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }

    function resolve() {
        $uibModalInstance.close('resolve');
    }

    function reject() {
        $uibModalInstance.close('reject');
    }
}
