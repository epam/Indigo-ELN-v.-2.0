(function() {
    angular
        .module('indigoeln')
        .controller('CreateNewExperimentModalController', CreateNewExperimentModalController);

    /* @ngInject */
    function CreateNewExperimentModalController($scope, componentsUtils, $uibModalInstance, Experiment, Principal, $q,
                                                localStorageService, fullNotebookId, NotebooksForSubCreation, Template) {
        var vm = this;
        var userId;
        var lastSelectedTemplateIdKey = '.lastSelectedTemplateId';
        var lastSelectedExperimentIdKey = '.lastSelectedExperimentId';

        init();

        function init() {
            vm.fullNotebookId = fullNotebookId;
            vm.selectedNotebook = '';
            vm.selectedTemplate = '';
            vm.experiment = {
                name: null,
                experimentNumber: null,
                template: null,
                id: null,
                components: {}
            };

            vm.ok = save;
            vm.cancel = cancelPressed;

            $q.all([NotebooksForSubCreation.query().$promise, Template.query({size: 100000}).$promise, updateUserId()])
                .then(function(responses) {
                    vm.notebooks = responses[0];
                    vm.templates = responses[1];
                    selectNotebookById();
                    selectTemplateById();
                });

            bindEvents();
        }

        function updateUserId() {
            return Principal.identity()
                .then(function(user) {
                    userId = user.id;
                });
        }

        function bindEvents() {
            $scope.$watch('vm.selectedTemplate', function() {
                // EPMLSOPELN-415 Remember last selected parent and template
                if (vm.selectedTemplate) {
                    localStorageService.set(userId + lastSelectedTemplateIdKey, vm.selectedTemplate.id);
                }
            });

            $scope.$watch('vm.selectedNotebook', function() {
                if (vm.selectedNotebook) {
                    localStorageService.set(userId + lastSelectedExperimentIdKey, vm.selectedNotebook.id);
                }
            });
        }

        function selectNotebookById() {
            var lastNotebookId = vm.fullNotebookId || localStorageService.get(userId + lastSelectedTemplateIdKey);

            if (lastNotebookId) {
                vm.selectedNotebook = _.find(vm.notebooks, {fullId: lastNotebookId});
            }
        }

        function selectTemplateById() {
            var lastSelectedTemplateId = localStorageService.get(userId + lastSelectedTemplateIdKey);

            if (lastSelectedTemplateId) {
                vm.selectedTemplate = _.find(vm.templates, {id: lastSelectedTemplateId});
            }
        }

        function save() {
            vm.isSaving = true;
            vm.experiment = _.extend(vm.experiment, {
                template: vm.selectedTemplate
            });

            initComponents(vm.experiment);

            Experiment.save({
                notebookId: vm.selectedNotebook.id,
                projectId: vm.selectedNotebook.parentId
            }, vm.experiment, onSaveSuccess, onSaveError);
        }

        function initComponents(experiment) {
            componentsUtils.initComponents(
                experiment.components,
                experiment.template.templateContent
            );
        }

        function cancelPressed() {
            $uibModalInstance.dismiss();
        }

        function onSaveSuccess(result) {
            var experiment = {
                projectId: vm.selectedNotebook.parentId,
                notebookId: vm.selectedNotebook.id,
                id: result.id
            };
            onSaveSuccess.isSaving = false;
            $uibModalInstance.close(experiment);
        }

        function onSaveError() {
            vm.isSaving = false;
        }
    }
})();
