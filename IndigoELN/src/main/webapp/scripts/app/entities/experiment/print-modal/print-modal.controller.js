(function() {
    angular
        .module('indigoeln')
        .controller('PrintModalController', PrintModalController);

    /* @ngInject */
    function PrintModalController($uibModalInstance, Principal, localStorageService, $stateParams, Experiment, Notebook, Project, Components) {
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
                vm.hasAttachments = false;
                vm.printContent = false;
                vm.components = [];
                initFromTemplate(entity);
            }
        }

        function mapComponentById(experiment, id) {
            var key;
            var component = _.find(Components, function(comp, ckey) {
                key = ckey;
                return comp.id === id;
            });

            return {
                id: key,
                value: component.name
            };
        }
        // this will not work for compounds. Need to ask Backend to support it properly
        function initFromTemplate(experiment) {
            var tabs = _.get(experiment, 'template.templateContent');
            _.each(tabs, function(tab) {
                _.each(tab.components, function(comp) {
                    vm.components.push(mapComponentById(experiment, comp.id));
                    if (comp.id === Components.attachments.id) {
                        vm.hasAttachments = true;
                    }
                });
            });
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