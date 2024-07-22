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
