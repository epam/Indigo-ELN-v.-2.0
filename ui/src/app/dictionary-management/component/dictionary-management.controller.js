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

var dictionaryManagementDeleteWordDialogTemplate =
    require('../../dictionary-management/delete-word-dialog/dictionary-management-delete-word-dialog.html');

/* @ngInject */
function DictionaryManagementController($scope, $uibModal, notifyService, dictionaryService, parseLinks) {
    var vm = this;

    vm.dictionaries = [];
    vm.selectedDictionaryId = null;
    vm.selectedDictionary = null;
    vm.page = 1;
    vm.itemsPerPage = 5;
    vm.searchText = '';
    vm.isCollapsed = true;
    vm.sortBy = {
        field: 'name',
        isAscending: true
    };

    vm.loadAllDictionaries = loadAllDictionaries;
    vm.saveDictionary = saveDictionary;
    vm.createDictionary = createDictionary;
    vm.editDictionary = editDictionary;
    vm.searchDictionary = searchDictionary;
    vm.clearDictionary = clearDictionary;
    vm.setSelected = setSelected;
    vm.clearSelection = clearSelection;
    vm.addWord = addWord;
    vm.clearWord = clearWord;
    vm.saveWord = saveWord;
    vm.editWord = editWord;
    vm.deleteWord = deleteWord;
    vm.sortDictionaries = sortDictionaries;

    init();

    function init() {
        var unsubscribe = $scope.$watch(function() {
            return vm.selectedDictionaryId;
        }, function() {
            if (vm.selectedDictionaryId) {
                vm.isCollapsed = false;
                vm.selectedDictionary = _.find(vm.dictionaries, function(dict) {
                    return dict.id === vm.selectedDictionaryId;
                });
                // set sorting way by mean of rank value
                vm.selectedDictionary.words.sort(function(a, b) {
                    return a.rank > b.rank;
                });
            }
        });

        $scope.$on('$destroy', function() {
            unsubscribe();
        });

        $scope.$watchCollection('vm.selectedDictionary.words', function() {
            if (vm.selectedDictionary) {
                updateRanks(vm.selectedDictionary.words.length);
            }
        });

        vm.loadAllDictionaries();
    }

    function loadAllDictionaries() {
        dictionaryService.query({
            page: vm.page - 1,
            size: vm.itemsPerPage,
            search: vm.searchText,
            sort: vm.sortBy.field + ',' + (vm.sortBy.isAscending ? 'asc' : 'desc')
        }, function(result, headers) {
            vm.links = parseLinks.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.dictionaries = result;
            vm.word = null;
        });
    }

    function saveDictionary() {
        vm.isSaving = true;
        if (vm.dictionary.id) {
            dictionaryService.update(vm.dictionary, onSaveSuccess, onSaveError);
        } else {
            dictionaryService.save(vm.dictionary, onSaveSuccess, onSaveError);
        }
    }

    function createDictionary() {
        vm.dictionary = {
            id: null, name: null, description: null, words: []
        };
    }

    function editDictionary(dictionary) {
        vm.loadAllDictionaries();
        vm.dictionary = _.extend({}, dictionary);
    }

    function searchDictionary() {
        dictionaryService.query({
            page: vm.page - 1,
            size: vm.itemsPerPage,
            search: vm.searchText,
            sort: vm.sortBy.field + ',' + (vm.sortBy.isAscending ? 'asc' : 'desc')
        }, function(result, headers) {
            vm.links = parseLinks.parse(headers('link'));
            vm.totalItems = headers('X-Total-Count');
            vm.dictionaries = result;
        });
    }

    function clearDictionary() {
        vm.dictionary = null;
    }

    function setSelected(id) {
        vm.selectedDictionaryId = id;
    }

    function clearSelection() {
        vm.word = null;
        vm.selectedDictionaryId = null;
        vm.selectedDictionary = null;
    }

    function addWord() {
        vm.word = {
            id: null, name: null, description: null, enable: true, rank: vm.selectedDictionary.words.length
        };
    }

    function clearWord() {
        vm.word = null;
    }

    function saveWord() {
        vm.isWordSaving = true;
        if (vm.word.rank === vm.selectedDictionary.words.length) {
            // add if this is a new one
            vm.selectedDictionary.words.push(vm.word);
        } else {
            // update if this is an edited one
            _.each(vm.selectedDictionary.words, function(wrd) {
                if (wrd.rank === vm.word.rank) {
                    _.extend(wrd, vm.word);

                    return false;
                }
            });
        }
        dictionaryService.update(vm.selectedDictionary, onSaveSuccess, onSaveError);
    }

    function editWord(word) {
        vm.word = _.extend({}, word);
    }

    function deleteWord(word) {
        $uibModal.open({
            animation: true,
            template: dictionaryManagementDeleteWordDialogTemplate,
            controller: 'DictionaryManagementDeleteWordDialogController',
            controllerAs: 'vm'
        }).result.then(function() {
            // remove the word
            var len = vm.selectedDictionary.words.length;
            for (var i = 0; i < len; i++) {
                if (vm.selectedDictionary.words[i].rank === word.rank) {
                    vm.selectedDictionary.words.splice(i, 1);
                    break;
                }
            }
            updateRanks(len);
        });
    }

    function onSaveSuccess() {
        vm.isSaving = false;
        vm.isWordSaving = false;
        vm.dictionary = null;
        vm.loadAllDictionaries();
    }

    function onSaveError() {
        vm.isSaving = false;
        vm.isWordSaving = false;
        vm.loadAllDictionaries();
        notifyService.error('Dictionary is not saved due to server error!');
    }

    function updateRanks(len) {
        // check if an element removed
        var modified = vm.selectedDictionary.words.length !== len;
        // update ranks
        if (vm.selectedDictionary) {
            for (var i = 0; i < vm.selectedDictionary.words.length; i++) {
                if (vm.selectedDictionary.words[i].rank !== i) {
                    vm.selectedDictionary.words[i].rank = i;
                    modified = true;
                }
            }
            if (modified) {
                dictionaryService.update(vm.selectedDictionary, onSaveSuccess, onSaveError);
            }
        }
    }

    function sortDictionaries(predicate, isAscending) {
        vm.sortBy.field = predicate;
        vm.sortBy.isAscending = isAscending;
        loadAllDictionaries();
    }
}

module.exports = DictionaryManagementController;
