AlertModalController.$inject = ['$uibModalInstance', 'title', 'message', 'okText', 'noText', 'cancelVisible',
    'okCallback', 'noCallback', 'type'];

function AlertModalController($uibModalInstance, title, message, okText, noText, cancelVisible, okCallback,
                              noCallback, type) {
    var vm = this;

    var btnClasses = {
        ERROR: 'btn-danger',
        WARNING: 'btn-danger',
        NORMAL: 'btn-primary'
    };

    $onInit();

    function $onInit() {
        vm.cancelVisible = cancelVisible;
        vm.okText = okText || 'Ok';
        vm.noText = noText || 'No';
        vm.title = title;
        vm.message = message;
        vm.hasOkCallback = !!okCallback;
        vm.hasNoCallback = !!noCallback;

        vm.cancel = cancel;
        vm.ok = ok;
        vm.no = no;
        vm.getBtnClass = getBtnClass;
    }

    function getBtnClass() {
        return btnClasses[type] || 'btn-default';
    }
    function cancel() {
        $uibModalInstance.dismiss('cancel');
        if (noCallback) {
            noCallback();
        }
    }

    function ok() {
        $uibModalInstance.close();
        if (_.isFunction(okCallback)) {
            okCallback();
        }
    }

    function no() {
        $uibModalInstance.dismiss('cancel');
        if (_.isFunction(noCallback)) {
            noCallback();
        }
    }
}

module.exports = AlertModalController;
