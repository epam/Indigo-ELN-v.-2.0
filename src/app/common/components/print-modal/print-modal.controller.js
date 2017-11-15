PrintModalController.$inject = ['$uibModalInstance', 'params', 'resourceName', 'resource', 'typeOfComponents',
    'componentsUtil'];

function PrintModalController($uibModalInstance, params, resourceName, resource,
                              typeOfComponents, componentsUtil) {
    var vm = this;

    init();

    function init() {
        vm.loading = resource.get(params).$promise.then(function(entity) {
            initCheckboxesForEntity(entity);
        });

        vm.dismiss = dismiss;
        vm.confirmCompletion = confirmCompletion;
    }

    function initCheckboxesForEntity(entity) {
        if (resourceName === 'projectService') {
            vm.hasAttachments = true;
        } else if (resourceName === 'notebookService') {
            vm.askContents = true;
        } else {
            vm.hasAttachments = false;
            vm.printContent = false;
            vm.components = [];
            initFromTemplate(entity);
        }
    }

    function buildComponentItem(component) {
        return {
            id: component.field,
            value: component ? component.name : 'Unknown'
        };
    }

    // this will not work for compounds. Need to ask Backend to support it properly
    function initFromTemplate(experiment) {
        var tabs = _.get(experiment, 'template.templateContent');

        _.each(componentsUtil.getComponentsFromTemplateContent(tabs), function(component) {
            if (typeOfComponents[component.field].isPrint) {
                vm.components.push(buildComponentItem(component));
            }
        });
        vm.hasAttachments = !!_.find(vm.components, {id: 'attachments'});
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

module.exports = PrintModalController;
