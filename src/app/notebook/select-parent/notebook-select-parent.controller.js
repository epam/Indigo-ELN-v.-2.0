/* @ngInject */
function NotebookSelectParentController($uibModalInstance, parents, principalService, simpleLocalCache) {
    var vm = this;
    var lastSelectedNotebookIdKey = '.lastSelectedNotebookId';
    var keyOfLastSelectedProject;

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
        simpleLocalCache.putByKey(keyOfLastSelectedProject, vm.selectedParent.id);
    }

    function init() {
        principalService.checkIdentity()
            .then(function(user) {
                keyOfLastSelectedProject = user.id + lastSelectedNotebookIdKey;
                var lastSelectedProjectId = simpleLocalCache.getByKey(keyOfLastSelectedProject);

                if (lastSelectedProjectId) {
                    vm.selectedParent = _.find(parents, {id: lastSelectedProjectId});
                }
            });
    }
}

module.exports = NotebookSelectParentController;
