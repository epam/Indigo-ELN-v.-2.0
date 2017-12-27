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
            onRefresh: '&'
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
        vm.field = vm.field || 'name';
        vm.allowClear = vm.allowClear || false;

        bindEvents();
    }

    function refresh(query) {
        var queryLowerCase = _.lowerCase(query);
        vm.filteredItems = _.filter(vm.items, function(item) {
            return _.includes(item[vm.field].toLowerCase(), queryLowerCase);
        });
    }

    function bindEvents() {
        $scope.$watch('vm.items', function() {
            refresh('');
        });
    }
}

module.exports = autocomplete;
