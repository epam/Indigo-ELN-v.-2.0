(function () {
    angular
        .module('indigoeln')
        .controller('TemplateController', TemplateController);

    /* @ngInject */
    function TemplateController(Template, ParseLinks) {
        var vm = this;

        vm.templates = [];
        vm.predicate = 'id';
        vm.reverse = true;
        vm.page = 1;
        vm.itemsPerPage = 10;

        vm.loadAll = loadAll;
        vm.loadPage = loadPage;
        vm.refresh = refresh;
        vm.clear = clear;

        vm.loadAll();

        function loadAll() {
            Template.query({
                page: vm.page - 1,
                size: vm.itemsPerPage,
                sort: [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'), 'id']
            }, function (result, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.templates = result;
            });
        }

        function loadPage() {
            vm.page = page;
            vm.loadAll();
        }

        function refresh() {
            vm.loadAll();
            vm.clear();
        }

        function clear() {
            vm.template = {
                name: null,
                id: null
            };
        }
    }
})();