(function() {
    angular
        .module('indigoeln')
        .controller('PrintModalController', PrintModalController);

    /* @ngInject */
    function PrintModalController($uibModalInstance, Principal, localStorageService, $stateParams, Experiment, Notebook, Project) {
        var vm = this;
        var userId;
        var storageKey = '-selected-components-for-print';
        var resource;
        vm.dismiss = dismiss;
        vm.confirmCompletion = confirmCompletion;

        init();

        function init() {
            if ($stateParams.experimentId >= 0) {
                resource = Experiment;
            } else if ($stateParams.notebookId >= 0) {
                resource = Notebook;
            } else if ($stateParams.projectId >= 0) {
                resource = Project;
            }
            vm.loading = resource.get($stateParams).$promise.then(function(entity) {
                initCheckboxesForEntity(entity);
                Principal.identity()
                    .then(function(user) {
                        userId = user.id;
                        restoreSelection();
                    });
            });
        }

        function initCheckboxesForEntity(entity) {
            if (resource === Project) {
                vm.hasAttachments = true;
            } else if (resource === Notebook) {
                vm.askContents = true;
            } else {
                var tabs = _.get(entity, 'template.templateContent');
                vm.hasAttachments = false;
                vm.printContent = false;
                vm.components = [];
                _.each(tabs, function(tab) {
                    _.each(tab.components, function(comp) {
                        vm.components.push({
                            id: comp.id,
                            value: comp.name
                        });
                        //Should use constant('Components' when ready
                        if (comp.id === 'attachments') {
                            vm.hasAttachments = true;
                        }
                    });
                });
            }
        }

        function saveSelection() {
            var userSelection = localStorageService.get(userId + storageKey) || {};
            _.each(vm.components, function(comp) {
                userSelection[comp.id] = comp.isChecked;
            });
            userSelection.printAttachedPDF = vm.printAttachedPDF;
            userSelection.printContent = vm.printContent;
            localStorageService.set(userId + storageKey, userSelection);
        }

        function restoreSelection() {
            var userSelection = localStorageService.get(userId + storageKey);
            if (userSelection) {
                _.each(vm.components, function(comp) {
                    comp.isChecked = userSelection[comp.id];
                });
                vm.printAttachedPDF = userSelection.printAttachedPDF;
                vm.printContent = userSelection.printContent;
            }
        }

        function dismiss() {
            saveSelection();
            $uibModalInstance.dismiss('cancel');
        }

        function confirmCompletion() {
            var checked = _.filter(vm.components, function(c) {
                return c.isChecked;
            });
            var resultForService = {
                components: _.map(checked, function(c) {
                    return c.id;
                }).join(',')
            };
            if (vm.printAttachedPDF) {
                resultForService.includeAttachments = vm.printAttachedPDF;
            }
            if (vm.printContent) {
                resultForService.withContent = vm.printAttachedPDF;
            }
            saveSelection();
            $uibModalInstance.close(resultForService);
        }
    }
})();
