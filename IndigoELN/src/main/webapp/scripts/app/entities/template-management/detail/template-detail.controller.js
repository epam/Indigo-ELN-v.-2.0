(function() {
    angular
        .module('indigoeln')
        .controller('TemplateDetailController', TemplateDetailController);

    /* @ngInject */
    function TemplateDetailController($stateParams, Template, componentsUtils) {
        var vm = this;

        vm.load = load;
        vm.model = {};

        if ($stateParams.id) {
            vm.load($stateParams.id);
        }

        function load(id) {
            Template.get({
                id: id
            }, function(result) {
                vm.template = result;
                componentsUtils.initComponents(vm.model, vm.template.templateContent);

            });
        }
    }
})();
