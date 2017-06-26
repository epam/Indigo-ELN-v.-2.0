(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentCreationFromEntitiesControlsController', ExperimentCreationFromEntitiesControlsController);

    /* @ngInject */
    function ExperimentCreationFromEntitiesControlsController($scope, $rootScope, $uibModalInstance, parents,
                                                              templates, Experiment, Principal, localStorageService) {
        var vm = this;

        vm.parents = parents;
        vm.selectedParent = '';
        vm.templates = templates;
        vm.selectedTemplate = '';
        vm.experiment = {name: null, experimentNumber: null, template: null, id: null};

        vm.ok = save;
        vm.cancel = cancelPressed;

        //EPMLSOPELN-415 Remember last selected parent and template
        Principal.identity()
            .then(function (user) {
                var tkey = user.id + '.' + 'lastSelectedTemplateId',
                    tval = localStorageService.get(tkey),
                    pkey = user.id + '.' + 'lastSelectedExperimentId',
                    pval = localStorageService.get(pkey);
                if (tval) {
                    vm.selectedTemplate = templates.filter(function (t) {
                        return t.id === tval;
                    })[0];
                }
                if (pval) {
                    vm.selectedParent = parents.filter(function (p) {
                        return p.id === pval;
                    })[0];
                }
                var unsubscribe = $scope.$watchGroup(function () {
                    return [vm.selectedTemplate, vm.selectedParent];
                }, function () {
                    if (vm.selectedTemplate) {
                        localStorageService.set(tkey, vm.selectedTemplate.id);
                    }
                    if (vm.selectedParent) {
                        localStorageService.set(pkey, vm.selectedParent.id);
                    }
                });
                $scope.$on('$destroy', function () {
                    unsubscribe();
                });
            });

        function save() {
            vm.isSaving = true;
            vm.experiment = _.extend(vm.experiment, {template: vm.selectedTemplate});
            Experiment.save({
                notebookId: vm.selectedParent.id,
                projectId: vm.selectedParent.parentId
            }, vm.experiment, onSaveSuccess, onSaveError);
        }

        function cancelPressed() {
            $uibModalInstance.dismiss();
        }

        function onSaveSuccess(result) {
            onSaveSuccess.isSaving = false;
            $rootScope.$broadcast('experiment-created', {
                projectId: vm.selectedParent.parentId,
                notebookId: vm.selectedParent.id,
                id: result.id
            });
            $uibModalInstance.close({
                projectId: vm.selectedParent.parentId,
                notebookId: vm.selectedParent.id,
                id: result.id
            });
        }

        function onSaveError() {
            vm.isSaving = false;
        }
    }
})();