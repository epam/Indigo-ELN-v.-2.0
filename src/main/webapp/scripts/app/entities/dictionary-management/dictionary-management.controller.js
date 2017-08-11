(function() {
    angular
        .module('indigoeln')
        .controller('DictionaryManagementController', DictionaryManagementController);

    /* @ngInject */
    function DictionaryManagementController($scope, $filter, $uibModal, notifyService, Dictionary, ParseLinks) {
        var vm = this;

        vm.dictionaries = [];
        vm.selectedDictionaryId = null;
        vm.selectedDictionary = null;
        vm.page = 1;
        vm.itemsPerPage = 5;
        vm.searchText = '';
        vm.isCollapsed = true;

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
            Dictionary.query({
                page: vm.page - 1,
                size: vm.itemsPerPage,
                search: vm.searchText
            }, function(result, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.dictionaries = result;
                vm.word = null;
            });
        }

        function saveDictionary() {
            vm.isSaving = true;
            if (vm.dictionary.id) {
                Dictionary.update(vm.dictionary, onSaveSuccess, onSaveError);
            } else {
                Dictionary.save(vm.dictionary, onSaveSuccess, onSaveError);
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
            Dictionary.query({
                page: vm.page - 1,
                size: vm.itemsPerPage,
                search: vm.searchText
            }, function(result, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                vm.dictionaries = $filter('filter')(result, vm.searchText);
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
            Dictionary.update(vm.selectedDictionary, onSaveSuccess, onSaveError);
        }

        function editWord(word) {
            vm.word = _.extend({}, word);
        }

        function deleteWord(word) {
            $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/dictionary-management/delete-dialog/dictionary-management-delete-word-dialog.html',
                controller: 'DictionaryManagementDeleteWordController',
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
                    Dictionary.update(vm.selectedDictionary, onSaveSuccess, onSaveError);
                }
            }
        }
    }
})();
