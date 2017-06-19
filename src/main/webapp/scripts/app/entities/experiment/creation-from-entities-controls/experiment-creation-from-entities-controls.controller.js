(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentCreationFromEntitiesControlsController', ExperimentCreationFromEntitiesControlsController);

    /* @ngInject */
    function ExperimentCreationFromEntitiesControlsController($scope, $rootScope, $uibModalInstance, parents,
                                                              templates, Experiment, Principal, localStorageService) {
        var self = this;

        self.parents = parents;
        self.selectedParent = '';
        self.templates = templates;
        self.selectedTemplate = '';
        self.experiment = {name: null, experimentNumber: null, template: null, id: null};

        self.ok     = save;
        self.cancel = cancelPressed;

        //EPMLSOPELN-415 Remember last selected parent and template
        Principal.identity()
            .then(function (user) {
                var tkey = user.id + '.' + 'lastSelectedTemplateId',
                    tval = localStorageService.get(tkey),
                    pkey = user.id + '.' + 'lastSelectedExperimentId',
                    pval = localStorageService.get(pkey);
                if (tval) {
                    self.selectedTemplate = templates.filter(function (t) {
                        return t.id === tval;
                    })[0];
                }
                if (pval) {
                    self.selectedParent = parents.filter(function (p) {
                        return p.id === pval;
                    })[0];
                }
                var unsubscribe = $scope.$watchGroup(function () {
                    return [self.selectedTemplate, self.selectedParent];
                }, function () {
                    if (self.selectedTemplate) {
                        localStorageService.set(tkey, self.selectedTemplate.id);
                    }
                    if (self.selectedParent) {
                        localStorageService.set(pkey, self.selectedParent.id);
                    }
                });
                $scope.$on('$destroy', function () {
                    unsubscribe();
                });
            });

        function save() {
            self.isSaving = true;
            self.experiment = _.extend(self.experiment, {template: self.selectedTemplate});
            Experiment.save({
                notebookId: self.selectedParent.id,
                projectId: self.selectedParent.parentId
            }, self.experiment, onSaveSuccess, onSaveError);
        }

        function cancelPressed() {
            $uibModalInstance.dismiss();
        }

        function onSaveSuccess(result) {
            onSaveSuccess.isSaving = false;
            $rootScope.$broadcast('experiment-created', {
                projectId: self.selectedParent.parentId,
                notebookId: self.selectedParent.id,
                id: result.id
            });
            $uibModalInstance.close({
                projectId: self.selectedParent.parentId,
                notebookId: self.selectedParent.id,
                id: result.id
            });
        }

        function onSaveError() {
            self.isSaving = false;
        }
    }
})();