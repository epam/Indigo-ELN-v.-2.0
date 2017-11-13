/* @ngInject */
function TemplateDetailController($stateParams, templateService, componentsUtils) {
    var vm = this;

    vm.load = load;
    vm.model = {};

    if ($stateParams.id) {
        vm.load($stateParams.id);
    }

    function load(id) {
        templateService.get(
            {
                id: id
            },
            function(result) {
                vm.template = result;
                componentsUtils.initComponents(vm.model, vm.template.templateContent);
            }
        );
    }
}

module.exports = TemplateDetailController;
