angular.module('indigoeln')
    .controller('ExperimentSelectParentTemplateController', function ($scope, $rootScope, Template,  $state, $uibModalInstance, parents, templates, Experiment, Principal, localStorageService) {
        $scope.parents = parents;
        $scope.selectedParent = '';
        $scope.templates = templates;
        $scope.selectedTemplate = '';
        $scope.experiment = {name: null, experimentNumber: null, template: null, id: null};

        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
            $rootScope.$broadcast('experiment-created', {
                projectId: $scope.selectedParent.parentId,
                notebookId: $scope.selectedParent.id,
                id: result.id
            });
            $uibModalInstance.close({
                projectId: $scope.selectedParent.parentId,
                notebookId: $scope.selectedParent.id,
                id: result.id
            });
        };

        var onSaveError = function () {
            $scope.isSaving = false;
        };

        function save() {
            $scope.isSaving = true;
            $scope.experiment = _.extend($scope.experiment, {template: $scope.selectedTemplate});
            Experiment.save({
                notebookId: $scope.selectedParent.id,
                projectId: $scope.selectedParent.parentId
            }, $scope.experiment, onSaveSuccess, onSaveError);
        }

        function cancelPressed() {
            $uibModalInstance.dismiss();
        }

        //EPMLSOPELN-415 Remember last selected parent and template
        Principal.identity()
            .then(function(user) {
                var tkey = user.id + '.' + 'lastSelectedTemplateId',
                    tval = localStorageService.get(tkey),
                    pkey = user.id + '.' + 'lastSelectedExperimentId',
                    pval = localStorageService.get(pkey);
                if (tval) {
                    $scope.selectedTemplate = templates.filter(function(t) {
                        return t.id == tval;
                    })[0]
                }
                if (pval) {
                    $scope.selectedParent = parents.filter(function(p) {
                        return p.id == pval;
                    })[0]
                }
                var unsubscribe = $scope.$watchGroup(['selectedTemplate', 'selectedParent'], function() {
                    if ($scope.selectedTemplate) {
                        localStorageService.set(tkey, $scope.selectedTemplate.id)
                    }
                    if ($scope.selectedParent) {
                        localStorageService.set(pkey, $scope.selectedParent.id)
                    }
                })
                $scope.$on('$destroy', function() {
                    unsubscribe();
                });
            });

        $scope.ok = save;
        $scope.cancel = cancelPressed;

    });
