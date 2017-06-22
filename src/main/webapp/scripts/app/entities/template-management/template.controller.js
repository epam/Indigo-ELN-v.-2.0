(function () {
    angular
        .module('indigoeln')
        .controller('TemplateController', TemplateController);

    /* @ngInject */
    function TemplateController(Template, ParseLinks) {
        var self = this;

        self.templates = [];
        self.predicate = 'id';
        self.reverse = true;
        self.page = 1;
        self.itemsPerPage = 10;

        self.loadAll    = loadAll;
        self.loadPage   = loadPage;
        self.refresh    = refresh;
        self.clear      = clear;

        self.loadAll();

        function loadAll() {
            Template.query({
                page: self.page - 1,
                size: self.itemsPerPage,
                sort: [self.predicate + ',' + (self.reverse ? 'asc' : 'desc'), 'id']
            }, function (result, headers) {
                self.links = ParseLinks.parse(headers('link'));
                self.totalItems = headers('X-Total-Count');
                self.templates = result;
            });
        }

        function loadPage() {
            self.page = page;
            self.loadAll();
        }

        function refresh() {
            self.loadAll();
            self.clear();
        }

        function clear() {
            self.template = {
                name: null,
                id: null
            };
        }
    }
})();