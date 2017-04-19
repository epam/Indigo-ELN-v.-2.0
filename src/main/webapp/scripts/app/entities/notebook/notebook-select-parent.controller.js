angular.module('indigoeln')
    .controller('NotebookSelectParentController', function ($scope, $uibModalInstance, parents, Principal, localStorageService) {
        $scope.parents = parents;
        $scope.selectedParent = '';

        $scope.ok = okPressed;
        $scope.cancel = cancelPressed;

        function okPressed () {
            $uibModalInstance.close($scope.selectedParent.id);
        }

        function cancelPressed () {
            $uibModalInstance.dismiss();
        }

        //EPMLSOPELN-415 Remember last selected parent and template
        Principal.identity()
            .then(function(user) {
                var pkey = user.id + '.' + 'lastSelectedProjectId',
                    pval = localStorageService.get(pkey);
                if (pval) {
                    $scope.selectedParent = parents.filter(function(p) {
                        return p.id == pval;
                    })[0]
                }
                var unsubscribe = $scope.$watchGroup(['selectedParent'], function() {
                    if ($scope.selectedParent) {
                        localStorageService.set(pkey, $scope.selectedParent.id)
                    }
                })
                $scope.$on('$destroy', function() {
                    unsubscribe();
                });
            });
    });
