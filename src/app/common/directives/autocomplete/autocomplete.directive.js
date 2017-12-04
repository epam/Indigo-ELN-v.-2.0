require('./autocomplete.less');
var template = require('./autocomplete.html');

function autocomplete() {
    return {
        restrict: 'E',
        scope: {
            label: '@',
            placeholder: '@',
            field: '@',
            model: '=',
            items: '=',
            readonly: '=',
            onSelect: '&',
            onRemove: '&',
            onRefresh: '&'
        },
        controller: autocompleteController,
        controllerAs: 'vm',
        bindToController: true,
        template: template,
        link: function($scope, $element, $attr) {
            if (!_.isUndefined($attr.multiple)) {
                $element.find('ui-select').attr('multiple', '');
            }
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

        bindEvents();
    }

    function refresh(query) {
        var queryLowerCase = _.lowerCase(query);
        vm.filteredItems = _.filter(vm.items, function(item) {
            return _.includes(item[vm.field].toLowerCase(), queryLowerCase);
        });
    }

    function bindEvents() {
        $scope.$watch('items', function() {
            refresh('');
        });
    }
}

module.exports = autocomplete;
