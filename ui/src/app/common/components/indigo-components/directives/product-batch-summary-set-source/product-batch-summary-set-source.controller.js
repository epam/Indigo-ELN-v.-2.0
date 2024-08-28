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

ProductBatchSummarySetSourceController.$inject = ['$uibModalInstance', 'name', '$q', 'dictionaryService'];

function ProductBatchSummarySetSourceController($uibModalInstance, name, $q, dictionaryService) {
    var vm = this;
    var sources = dictionaryService.getByName({name: 'Source'}).$promise;
    var sourceDetails = dictionaryService.getByName({name: 'Source Details'}).$promise;
    $q.all([sources, sourceDetails]).then(function(results) {
        vm.name = name;
        vm.sourceValues = results[0].words;
        vm.sourceDetails = results[1].words;
        vm.save = save;
        vm.clear = clear;

        function save() {
            $uibModalInstance.close({
                source: vm.source, sourceDetail: vm.sourceDetail
            });
        }

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }
    });
}

module.exports = ProductBatchSummarySetSourceController;

