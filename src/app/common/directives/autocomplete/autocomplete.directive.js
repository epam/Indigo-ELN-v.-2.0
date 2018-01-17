require('./autocomplete.less');

var template = require('./autocomplete.html');
var templateMultiple = require('./autocomplete-multiple.html');

function autocomplete() {
    return {
        restrict: 'E',
        scope: {
            label: '@',
            placeholder: '@',
            field: '@',
            elName: '@',
            autofocus: '=',
            model: '=',
            items: '=',
            isRequired: '=',
            readonly: '=',
            isMultiple: '@',
            allowClear: '=',
            onSelect: '&',
            onRemove: '&',
            onRefresh: '&?',
            onLoadPage: '&?'
        },
        controller: autocompleteController,
        controllerAs: 'vm',
        bindToController: true,
        template: function($element, $attr) {
            return $attr.isMultiple ? templateMultiple : template;
        }
    };
}

/* @ngInject */
function autocompleteController($scope) {
    var vm = this;

    init();

    function init() {
        vm.refresh = refresh;
        vm.loadPage = loadPage;
        vm.field = vm.field || 'name';
        vm.allowClear = vm.allowClear || false;
        vm.isLoading = false;

        bindEvents();
    }

    function refresh(query) {
        // If external callback is provided use it here (e.g. for requesting items through $http)
        if (vm.onRefresh) {
            vm.isLoading = true;
            vm.onRefresh({query: query})
                .then(function() {
                    vm.isLoading = false;
                });

            return;
        }

        // Otherwise the items will be filtered here
        filterItems(query);
    }

    function loadPage(query) {
        // If this callback is provided it will be executed when ui-select-choices is scrolled to the bottom
        if (vm.onLoadPage) {
            vm.isLoading = true;
            vm.onLoadPage({query: query})
                .then(function() {
                    vm.isLoading = false;
                });
        }
    }

    function bindEvents() {
        $scope.$watch('vm.items', function() {
            filterItems('');
        });
    }

    function filterItems(query) {
        var queryLowerCase = _.lowerCase(query);
        vm.filteredItems = _.filter(vm.items, function(item) {
            return _.includes(item[vm.field].toLowerCase(), queryLowerCase);
        });
    }
}

module.exports = autocomplete;
