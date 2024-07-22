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

require('./indigo-select.less');
indigoSelect.$inject = ['formUtils'];

function indigoSelect(formUtils) {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            indigoLabel: '@',
            indigoModel: '=',
            indigoItems: '=',
            indigoDictionary: '@',
            indigoMultiple: '=',
            indigoLabelVertical: '=',
            indigoLabelColumnsNum: '=',
            indigoControl: '=',
            indigoPlaceHolder: '@',
            indigoItemProp: '@',
            indigoOrderByProp: '@',
            indigoClasses: '@',
            indigoChange: '&',
            indigoRemove: '&',
            indigoName: '@',
            indigoRequired: '=',
            indigoReadonly: '='
        },
        controller: IndigoSelectController,
        controllerAs: 'vm',
        bindToController: true,
        compile: compile,
        template: function(tElement, tAttrs) {
            var itemProp = tAttrs.indigoItemProp || 'name';
            var content = _.chain(itemProp.split(','))
                .map(function(prop) {
                    return '{{ $select.selected.' + prop + '}}';
                })
                .reduce(function(memo, num) {
                    return memo + (memo.length > 0 ? ' - ' : '') + num;
                }, '').value();

            return '<div class="form-group {{vm.indigoClasses}}">' +
                '<label ng-bind="vm.indigoLabel"></label>' +
                // '<div class="col-xs-10">' +
                '<ui-select ng-model="vm.indigoModel" name="{{vm.indigoName}}"' +
                ' indigo-select-required="{{vm.indigoRequired}}" theme="bootstrap"' +
                ' ng-disabled="vm.indigoReadonly"' +
                ' onkeypress="return false;" on-select="vm.indigoChange({selected: vm.indigoModel})"' +
                ' on-remove="vm.indigoRemove()"' +
                ' >' +
                '<ui-select-match placeholder="{{vm.indigoPlaceHolder}}" >' + content + '</ui-select-match>' +
                '<ui-select-choices repeat="item in vm.indigoItems | filter: $select.search">' +
                '</ui-select-choices>' +
                '</ui-select>' +
                // '</div>' +
                '</div>';
        }
    };

    /* @ngInject */
    function compile(tElement, tAttrs) {
        tAttrs.indigoItemProp = tAttrs.indigoItemProp || 'name';
        tAttrs.indigoOrderByProp = tAttrs.indigoOrderByProp || 'rank';
        if (tAttrs.indigoMultiple) {
            tElement.find('ui-select').attr('multiple', true);
            tElement.find('ui-select-match').html('{{$item.' + tAttrs.indigoItemProp + '}}');
        }
        formUtils.doVertical(tAttrs, tElement);
        var select = tElement.find('ui-select-choices');
        var htmlContent = _.reduce(tAttrs.indigoItemProp.split(','), function(memo, num) {
            return memo + (memo.length > 0 ? ' + \' - \' + ' : '') + 'item.' + num;
        }, '');
        select.append('<span ng-bind-html="' + htmlContent + ' | highlight: $select.search"></span>');
        var repeat = select.attr('repeat');
        select.attr('repeat', repeat + ' | orderBy:"' + tAttrs.indigoOrderByProp + '"');
        formUtils.clearLabel(tAttrs, tElement);
        formUtils.setLabelColumns(tAttrs, tElement);

        return {
            post: function(scope) {
                formUtils.addOnChange(scope);
            }
        };
    }

    /* @ngInject */
    function IndigoSelectController(dictionaryService) {
        var vm = this;

        vm.control = vm.indigoControl || {};
        vm.control.setSelection = setSelection;
        vm.control.unSelect = unSelect;

        init();

        function setSelection(select) {
            vm.indigoModel = select;
        }

        function unSelect() {
            vm.indigoModel = {};
        }

        function init() {
            if (vm.indigoDictionary) {
                dictionaryService.getByName({
                    name: vm.indigoDictionary
                }, function(dictionary) {
                    vm.indigoItems = dictionary.words;
                });
            }
        }
    }
}

module.exports = indigoSelect;
