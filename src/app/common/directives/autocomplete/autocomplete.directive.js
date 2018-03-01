/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

require('./autocomplete.less');

var template = require('./autocomplete.html');
var templateMultiple = require('./autocomplete-multiple.html');

function autocomplete() {
    return {
        restrict: 'E',
        scope: {
            label: '@',
            placeholder: '@',
            dictionary: '@',
            field: '@',
            elName: '=',
            autofocus: '=',
            model: '=',
            items: '=',
            isRequired: '=',
            readonly: '=',
            isMultiple: '@',
            clearItem: '=',
            allowClear: '=',
            onSelect: '&',
            onRemove: '&',
            onRefresh: '&?',
            onLoadPage: '&?',
            ownEntitySelected: '='
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
function autocompleteController($scope, translateService, dictionaryService) {
    var vm = this;

    init();

    function init() {
        vm.refresh = refresh;
        vm.loadPage = loadPage;
        vm.field = vm.field || 'name';
        vm.allowClear = vm.allowClear || false;
        vm.isLoading = false;
        vm.loadingPlaceholder = translateService.translate('AUTOCOMPLETE_LOADING_PLACEHOLDER');
        vm.emptyListPlaceholder = translateService.translate('AUTOCOMPLETE_EMPTY_PLACEHOLDER');

        if (vm.dictionary) {
            dictionaryService.getByName({
                name: vm.dictionary
            }, function(dictionary) {
                vm.items = dictionary.words;
                vm.items = _.map(vm.items, function(item) {
                    item.label = _.reduce(vm.field.split(','), function(acc, field) {
                        return (acc.length ? acc + ' ' : '') + item[field];
                    }, '');

                    return item;
                });
            });
        } else {
            vm.items = _.map(vm.items, function(item) {
                item.label = _.reduce(vm.field.split(','), function(acc, field) {
                    return (acc.length ? acc + ' ' : '') + item[field];
                }, '');

                return item;
            });
        }

        bindEvents();
    }

    function refresh(query) {
        // If external callback is provided use it here (e.g. for requesting items through $http)
        if (_.isFunction(vm.onRefresh)) {
            vm.isLoading = true;
            vm.onRefresh({query: query})
                .finally(function() {
                    vm.isLoading = false;
                });

            return;
        }

        // Otherwise the items will be filtered here
        filterItems(query);
    }

    function loadPage(query) {
        // If this callback is provided it will be executed when ui-select-choices is scrolled to the bottom
        if (_.isFunction(vm.onLoadPage)) {
            vm.isLoading = true;
            vm.onLoadPage({query: query})
                .finally(function() {
                    vm.isLoading = false;
                });
        }
    }

    function bindEvents() {
        $scope.$watchCollection('vm.items', function() {
            filterItems('');
        });
    }

    function filterItems(query) {
        var queryLowerCase = _.lowerCase(query);
        vm.filteredItems = _.filter(vm.items, function(item) {
            return _.includes(item.label ? item.label.toLowerCase() : '', queryLowerCase);
        });
    }
}

module.exports = autocomplete;
