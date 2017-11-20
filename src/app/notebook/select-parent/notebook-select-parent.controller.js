/* @ngInject */
function NotebookSelectParentController($scope, $uibModalInstance, parents, principalService, simpleLocalCache) {
    var vm = this;
    var lastSelectedNotebookIdKey = '.lastSelectedNotebookId';
    var keyOfLastSelectedProject;

    vm.parents = parents;
    vm.selectedParent = '';

    vm.ok = okPressed;
    vm.cancel = cancelPressed;

    init();

    function okPressed() {
        $uibModalInstance.close(vm.selectedParent.id);
    }

    function cancelPressed() {
        $uibModalInstance.dismiss();
    }

    function init() {
        principalService.checkIdentity()
            .then(function(user) {
                keyOfLastSelectedProject = user.id + lastSelectedNotebookIdKey;
                var lastSelectedProjectId = simpleLocalCache.getByKey(keyOfLastSelectedProject);

                if (lastSelectedProjectId) {
                    vm.selectedParent = _.find(parents, {id: lastSelectedProjectId});
                }

                bindEvents();
            });
    }

    function bindEvents() {
        $scope.$watch('vm.selectedParent', function() {
            if (vm.selectedParent) {
                simpleLocalCache.putByKey(keyOfLastSelectedProject, vm.selectedParent.id);
            }
        });
    }
}

module.exports = NotebookSelectParentController;
