/* @ngInject */
function NotebookSelectParentController($uibModalInstance, parents, principalService, simpleLocalCache) {
    var vm = this;
    var lastSelectedProjectIdKey = '.lastSelectedProjectId';

    vm.parents = parents;
    vm.selectedParent = '';

    vm.ok = okPressed;
    vm.cancel = cancelPressed;
    vm.onSelect = onSelect;

    init();

    function okPressed() {
        $uibModalInstance.close(vm.selectedParent.id);
    }

    function cancelPressed() {
        $uibModalInstance.dismiss();
    }

    function onSelect() {
        simpleLocalCache.putByKey(getKey(lastSelectedProjectIdKey), vm.selectedParent.id);
    }

    function getKey(suffix) {
        return principalService.getIdentity().id + suffix;
    }

    function init() {
        var lastSelectedProjectId = simpleLocalCache.getByKey(getKey(lastSelectedProjectIdKey));

        if (lastSelectedProjectId) {
            vm.selectedParent = _.find(parents, {id: lastSelectedProjectId});
        }
    }
}

module.exports = NotebookSelectParentController;
