require('./linked-experiments.less');
var template = require('./linked-experiments.html');

function linkedExperiments() {
    return {
        restrict: 'E',
        scope: {
            indigoLabel: '@',
            indigoModel: '=',
            indigoReadonly: '=',
            indigoPlaceholder: '@',
            closeOnSelect: '='
        },
        controller: LinkedExperimentsController,
        controllerAs: 'vm',
        bindToController: true,
        compile: function($element, $attr) {
            if (!_.isUndefined($attr.indigoMultiple)) {
                $element.find('ui-select').attr('multiple', '');
            }
        },
        template: template
    };
}

/* @ngInject */
function LinkedExperimentsController(searchService) {
    var vm = this;

    init();

    function init() {
        vm.refresh = refresh;
    }

    function refresh(query) {
        var pageSize = 10;
        var pageNumber = 0;

        searchService.getExperiments({
            query: query,
            size: pageSize,
            page: pageNumber
        })
            .$promise
            .then(function(response) {
                vm.items = response;
            });
    }
}

module.exports = linkedExperiments;
