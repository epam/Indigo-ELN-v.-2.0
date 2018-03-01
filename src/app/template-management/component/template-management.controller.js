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
        isAscending: true
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
            sort: vm.sortBy.field + ',' + (vm.sortBy.isAscending ? 'asc' : 'desc')
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

    function sortTemplates(predicate, isAscending) {
        vm.sortBy.field = predicate;
        vm.sortBy.isAscending = isAscending;
        loadAll();
    }
}

module.exports = TemplateManagementController;
