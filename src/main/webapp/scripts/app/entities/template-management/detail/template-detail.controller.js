(function() {
    angular
        .module('indigoeln')
        .controller('TemplateDetailController', TemplateDetailController);

    /* @ngInject */
    function TemplateDetailController($stateParams, Template) {
        var vm = this;

        vm.load = load;

        if ($stateParams.id) {
            vm.load($stateParams.id);
        }

        function load(id) {
            Template.get({
                id: id
            }, function(result) {
                vm.template = result;
            });
        }
    }
})();
