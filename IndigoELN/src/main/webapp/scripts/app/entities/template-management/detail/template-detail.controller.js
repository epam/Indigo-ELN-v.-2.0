(function () {
    angular
        .module('indigoeln')
        .controller('TemplateDetailController', TemplateDetailController);


    function TemplateDetailController($stateParams, Template) {
        var self = this;

        self.load = load;

        if ($stateParams.id) {
            self.load($stateParams.id);
        }

        function load(id) {
            Template.get({id: id}, function (result) {
                self.template = result;
            });
        }
    }
})();