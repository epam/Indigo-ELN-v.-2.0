(function() {
    angular
        .module('indigoeln')
        .controller('PrintModalController', PrintModalController);

    /* @ngInject */
    function PrintModalController($uibModalInstance, $stateParams, Experiment, Notebook, Project, Components, EntitiesBrowser) {
        var vm = this;
        var resource;
        vm.dismiss = dismiss;
        vm.confirmCompletion = confirmCompletion;

        init();

        function init() {
            initService(EntitiesBrowser.getActiveTab().kind);
            vm.loading = resource.get($stateParams).$promise.then(function(entity) {
                initCheckboxesForEntity(entity);
            });
        }

        function initService(kind) {
            switch (kind) {
                case 'project':
                    resource = Project;
                    break;
                case 'notebook':
                    resource = Notebook;
                    break;
                case 'experiment':
                    resource = Experiment;
                    break;
                default:
                    break;
            }

            return resource;
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

        function mapComponentById(id) {
            var key;
            var component = _.find(Components, function(comp, ckey) {
                key = ckey;

                return comp.id === id;
            });

            return {
                id: key,
                value: component ? component.name : 'Unknown'
            };
        }
        // this will not work for compounds. Need to ask Backend to support it properly
        function initFromTemplate(experiment) {
            var tabs = _.get(experiment, 'template.templateContent');
            _.each(tabs, function(tab) {
                _.each(tab.components, function(comp) {
                    vm.components.push(mapComponentById(comp.id));
                    if (comp.id === Components.attachments.id) {
                        vm.hasAttachments = true;
                    }
                });
            });
        }

        function dismiss() {
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
                resultForService.withContent = vm.printContent;
            }
            $uibModalInstance.close(resultForService);
        }
    }
})();
