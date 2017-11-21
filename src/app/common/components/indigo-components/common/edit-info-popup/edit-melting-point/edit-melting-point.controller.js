EditMeltingPointController.$inject = ['$uibModalInstance', 'data'];

function EditMeltingPointController($uibModalInstance, data) {
    var vm = this;

    init();

    function init() {
        vm.meltingPoint = data || {};

        vm.save = save;
        vm.cancel = cancel;
    }

    function resultToString() {
        if (vm.meltingPoint.lower && vm.meltingPoint.upper) {
            return vm.meltingPoint.lower + ' ~ ' + vm.meltingPoint.upper + '\xB0C' + addCommentIfExist();
        }

        return null;
    }

    function addCommentIfExist() {
        return vm.meltingPoint.comments ? ', ' + vm.meltingPoint.comments : '';
    }

    function save() {
        vm.meltingPoint.asString = resultToString();
        $uibModalInstance.close(vm.meltingPoint);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = EditMeltingPointController;
