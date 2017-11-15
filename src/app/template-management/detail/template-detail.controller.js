/* @ngInject */
function TemplateDetailController($stateParams, templateService, componentsUtil) {
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
                componentsUtil.initComponents(vm.model, vm.template.templateContent);
            }
        );
    }
}

module.exports = TemplateDetailController;
