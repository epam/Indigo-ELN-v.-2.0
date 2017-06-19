(function () {
    angular
        .module('indigoeln')
        .controller('DictionaryManagementController', DictionaryManagementController);

    /* @ngInject */
    function DictionaryManagementController($scope, $filter, $uibModal, Alert, Dictionary, ParseLinks) {
        var self = this;

        self.dictionaries = [];
        self.selectedDictionaryId = null;
        self.selectedDictionary = null;
        self.page = 1;
        self.itemsPerPage = 5;
        self.searchText = '';
        self.isCollapsed = true;

        self.loadAllDictionaries    = loadAllDictionaries;
        self.saveDictionary         = saveDictionary;
        self.createDictionary       = createDictionary;
        self.editDictionary         = editDictionary;
        self.searchDictionary       = searchDictionary;
        self.clearDictionary        = clearDictionary;
        self.setSelected            = setSelected;
        self.clearSelection         = clearSelection;
        self.addWord                = addWord;
        self.clearWord              = clearWord;
        self.saveWord               = saveWord;
        self.editWord               = editWord;
        self.deleteWord             = deleteWord;

        self.loadAllDictionaries();

        $scope.$on('$destroy', function () {
            unsubscribe();
        });

        $scope.$watchCollection('selectedDictionary.words', function () {
            if (self.selectedDictionary) {
                updateRanks(self.selectedDictionary.words.length);
            }
        });

        function loadAllDictionaries() {
            Dictionary.query({
                page: self.page - 1,
                size: self.itemsPerPage,
                search: self.searchText
            }, function (result, headers) {
                self.links = ParseLinks.parse(headers('link'));
                self.totalItems = headers('X-Total-Count');
                self.dictionaries = result;
                self.word = null;
            });
        }

        function saveDictionary() {
            self.isSaving = true;
            if (self.dictionary.id) {
                Dictionary.update(self.dictionary, onSaveSuccess, onSaveError);
            } else {
                Dictionary.save(self.dictionary, onSaveSuccess, onSaveError);
            }
        }

        function createDictionary() {
            self.dictionary = {
                id: null, name: null, description: null, words: []
            };
        }

        function editDictionary(dictionary) {
            self.loadAllDictionaries();
            self.dictionary = _.extend({}, dictionary);
        }

        function searchDictionary() {
            Dictionary.query({
                page: self.page - 1,
                size: self.itemsPerPage,
                search: self.searchText
            }, function (result, headers) {
                self.links = ParseLinks.parse(headers('link'));
                self.totalItems = headers('X-Total-Count');
                self.dictionaries = $filter('filter')(result, self.searchText);
            });
        }

        function clearDictionary() {
            self.dictionary = null;
        }

        function setSelected(id) {
            self.selectedDictionaryId = id;
        }

        function clearSelection() {
            self.word = null;
            self.selectedDictionaryId = null;
            self.selectedDictionary = null;
        }

        function addWord() {
            self.word = {
                id: null, name: null, description: null, enable: true, rank: self.selectedDictionary.words.length
            };
        }

        function clearWord() {
            self.word = null;
        }

        function saveWord() {
            self.isWordSaving = true;
            if (self.word.rank === self.selectedDictionary.words.length) {
                // add if this is a new one
                self.selectedDictionary.words.push(self.word);
            } else {
                // update if this is an edited one
                _.each(self.selectedDictionary.words, function (wrd) {
                    if (wrd.rank === self.word.rank) {
                        _.extend(wrd, self.word);
                        return false;
                    }
                });
            }
            Dictionary.update(self.selectedDictionary, onSaveSuccess, onSaveError);
        }

        function editWord(word) {
            self.word = _.extend({}, word);
        }

        function deleteWord(word) {
            $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/entities/dictionary-management/delete-dialog/dictionary-management-delete-word-dialog.html',
                controller: 'DictionaryManagementDeleteWordController',
                controllerAs: 'dictionaryManagementDeleteWordController'
            }).result.then(function () {
                // remove the word
                var len = self.selectedDictionary.words.length;
                for (var i = 0; i < len; i++) {
                    if (self.selectedDictionary.words[i].rank === word.rank) {
                        self.selectedDictionary.words.splice(i, 1);
                        break;
                    }
                }
                updateRanks(len);
            });
        }

        function unsubscribe() {
            $scope.$watch(function () {
                return self.selectedDictionaryId;
            }, function () {
                if (self.selectedDictionaryId) {
                    self.isCollapsed = false;
                    self.selectedDictionary = _.find(self.dictionaries, function (dict) {
                        return dict.id === self.selectedDictionaryId;
                    });
                    // set sorting way by mean of rank value
                    self.selectedDictionary.words.sort(function (a, b) {
                        return a.rank > b.rank;
                    });
                }
            });
        }

        function onSaveSuccess() {
            self.isSaving = false;
            self.isWordSaving = false;
            self.dictionary = null;
            self.loadAllDictionaries();
        }

        function onSaveError() {
            self.isSaving = false;
            self.isWordSaving = false;
            self.loadAllDictionaries();
            Alert.error('Dictionary is not saved due to server error!');

        }

        function updateRanks(len) {
            // check if an element removed
            var modified = self.selectedDictionary.words.length !== len;
            // update ranks
            if (self.selectedDictionary) {
                for (var i = 0; i < self.selectedDictionary.words.length; i++) {
                    if (self.selectedDictionary.words[i].rank !== i) {
                        self.selectedDictionary.words[i].rank = i;
                        modified = true;
                    }
                }
                if (modified) {
                    Dictionary.update(self.selectedDictionary, onSaveSuccess, onSaveError);
                }
            }
        }
    }
})();
