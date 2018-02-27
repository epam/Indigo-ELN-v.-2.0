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

SelectFromDictionaryController.$inject = ['$uibModalInstance', 'data', 'dictionary', 'title'];

function SelectFromDictionaryController($uibModalInstance, data, dictionary, title) {
    var vm = this;

    init();

    function init() {
        vm.title = title;
        vm.dictionary = dictionary;
        vm.model = data || {};
        vm.model.data = vm.model.data || [];
        vm.selectedItemsFlags = getSelectedItems();

        vm.selectItem = selectItem;
        vm.save = save;
        vm.cancel = cancel;
    }

    function getSelectedItems() {
        return _.map(vm.dictionary.words, function(item) {
            return _.includes(vm.model.data, item.name);
        });
    }

    function selectItem(index, item) {
        if (vm.selectedItemsFlags[index]) {
            vm.model.data[index] = item;
        } else {
            delete vm.model.data[index];
        }
    }

    function save() {
        vm.model.data = [];
        _.each(vm.selectedItemsFlags, function(isSelected, index) {
            if (isSelected) {
                vm.model.data.push(vm.dictionary.words[index].name);
            }
        });
        vm.model.asString = vm.model.data.join(', ');
        $uibModalInstance.close(vm.model);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}

module.exports = SelectFromDictionaryController;

