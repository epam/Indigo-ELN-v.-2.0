/* @ngInject */
function TemplateManagementController(templateService, parseLinks) {
    var vm = this;

    vm.templates = [];
    vm.predicate = 'id';
    vm.reverse = true;
    vm.page = 1;
    vm.itemsPerPage = 10;
    vm.sortBy = {
        field: 'name',
        reverse: false
    };

    vm.loadAll = loadAll;
    vm.loadPage = loadPage;
    vm.refresh = refresh;
    vm.clear = clear;
    vm.sortTemplates = sortTemplates;

    vm.loadAll();

    function loadAll() {
        templateService.query({
            page: vm.page - 1,
            size: vm.itemsPerPage,
            sort: vm.sortBy.field + ',' + (vm.sortBy.reverse ? 'desc' : 'asc')
        }, function(result, headers) {
            vm.links = parseLinks.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.templates = result;
        });
    }

    function loadPage(page) {
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

    function sortTemplates(predicate, reverse) {
        vm.sortBy.field = predicate;
        vm.sortBy.reverse = reverse;
        loadAll();
    }
}

module.exports = TemplateManagementController;
