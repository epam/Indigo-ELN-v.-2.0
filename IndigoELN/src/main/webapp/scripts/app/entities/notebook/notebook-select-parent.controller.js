(function () {
    angular.module('indigoeln')
        .controller('NotebookSelectParentController', NotebookSelectParentController);

    /* @ngInject */
    function NotebookSelectParentController($scope, $uibModalInstance, parents, Principal, localStorageService) {
        var vm = this;
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
            //EPMLSOPELN-415 Remember last selected parent and template
            Principal.identity()
                .then(function (user) {
                    var pkey = user.id + '.' + 'lastSelectedProjectId',
                        pval = localStorageService.get(pkey);
                    if (pval) {
                        vm.selectedParent = parents.filter(function (p) {
                            return p.id === pval;
                        })[0];
                    }
                    var unsubscribe = $scope.$watchGroup(['selectedParent'], function () {
                        if (vm.selectedParent) {
                            localStorageService.set(pkey, vm.selectedParent.id);
                        }
                    });
                    $scope.$on('$destroy', function () {
                        unsubscribe();
                    });
                });
        }
    }
})();

