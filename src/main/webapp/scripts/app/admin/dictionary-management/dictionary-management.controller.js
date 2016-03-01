'use strict';

angular.module('indigoeln')
    .controller('DictionaryManagementController', function ($scope, Dictionary, ParseLinks, $filter, $uibModal) {
        $scope.dictionaries = [];
        $scope.selectedDictionaryId = null;
        $scope.selectedDictionary = null;

        $scope.page = 1;
        $scope.itemsPerPage = 5;
        $scope.searchText = "";

        $scope.isCollapsed = true;

        $scope.loadAllDictionaries = function () {
            Dictionary.query({page: $scope.page - 1, size: $scope.itemsPerPage, search: $scope.searchText}, function (result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.dictionaries = result;
                $scope.word = null;
            });
        };

        $scope.loadAllDictionaries();

        $scope.$watch('selectedDictionaryId', function (newValue) {
            if ($scope.selectedDictionaryId) {
                $scope.isCollapsed = false;
                $scope.selectedDictionary = _.find($scope.dictionaries, function (dict) {
                    return dict.id == $scope.selectedDictionaryId
                });
                // set sorting way by mean of rank value
                $scope.selectedDictionary.words.sort(function(a,b){ return a.rank > b.rank;});
            }
        });

        $scope.$watchCollection('selectedDictionary.words', function (newValue) {
            if ($scope.selectedDictionary) {
                updateRanks($scope.selectedDictionary.words.length);
            }
        });

        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
            $scope.isWordSaving = false;
            $scope.dictionary = null;
            $scope.loadAllDictionaries();
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
            $scope.isWordSaving = false;
            $scope.loadAllDictionaries();
        };

        $scope.saveDictionary = function () {
            $scope.isSaving = true;
            if ($scope.dictionary.id != null) {
                Dictionary.update($scope.dictionary, onSaveSuccess, onSaveError);
            } else {
                Dictionary.save($scope.dictionary, onSaveSuccess, onSaveError);
            }
        };

        $scope.createDictionary = function () {
            $scope.dictionary = {
                id: null, name: null, description: null, words: []
            }
        };

        $scope.editDictionary = function (dictionary) {
            $scope.loadAllDictionaries();
            $scope.dictionary = _.extend({}, dictionary);
        };

        $scope.searchDictionary = function () {
            Dictionary.query({page: $scope.page - 1, size: $scope.itemsPerPage, search: $scope.searchText}, function (result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.dictionaries = $filter('filter')(result, $scope.searchText);
            });
        };

        $scope.clearDictionary = function () {
            $scope.dictionary = null;
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };

        $scope.setSelected = function (id) {
            $scope.selectedDictionaryId = id;
        }

        $scope.clearSelection = function () {
            $scope.word = null;
            $scope.selectedDictionaryId = null;
            $scope.selectedDictionary = null;
        }

        $scope.addWord = function () {
            $scope.word = {
                id: null, name: null, description: null, enable: true, rank: $scope.selectedDictionary.words.length
            }
        }

        $scope.clearWord = function () {
            $scope.word = null;
            $scope.editWordForm.$setPristine();
            $scope.editWordForm.$setUntouched();
        };

        $scope.saveWord = function () {
            $scope.isWordSaving = true;
            if ($scope.word.rank == $scope.selectedDictionary.words.length) {
                // add if this is a new one
                $scope.selectedDictionary.words.push($scope.word);
            } else {
                // update if this is an edited one
                _.each($scope.selectedDictionary.words, function (wrd) {
                    if (wrd.rank == $scope.word.rank) {
                        _.extend(wrd, $scope.word);
                        return false;
                    }
                })
            }
            Dictionary.update($scope.selectedDictionary, onSaveSuccess, onSaveError);
        }

        $scope.editWord = function (word) {
            $scope.word = _.extend({}, word);
        }

        $scope.deleteWord = function (word) {

            var modalInstance = $uibModal.open({
                animation: true,
                templateUrl: 'scripts/app/admin/dictionary-management/dictionary-management-delete-word-dialog.html',
                controller: function ($scope, $uibModalInstance) {
                    $scope.dismiss = function () {$uibModalInstance.dismiss('cancel');};
                    $scope.confirmDeleteWord = function () { $uibModalInstance.close(true);};
                }
            }).result.then(function () {
                // remove the word
                var len = $scope.selectedDictionary.words.length;
                for(var i = 0; i < len; i++) {
                    if($scope.selectedDictionary.words[i].rank == word.rank) {
                        $scope.selectedDictionary.words.splice(i, 1);
                        break;
                    }
                }
                updateRanks(len);
            })
        };

        var updateRanks = function(len) {
            // check if an element removed
            var modified = $scope.selectedDictionary.words.length != len;
            // update ranks
            if ($scope.selectedDictionary) {
                for(var i = 0; i < $scope.selectedDictionary.words.length; i++) {
                    if ($scope.selectedDictionary.words[i].rank != i) {
                        $scope.selectedDictionary.words[i].rank = i;
                        modified = true;
                    }
                }
                if (modified) {
                    Dictionary.update($scope.selectedDictionary, onSaveSuccess, onSaveError);
                }
            }
        }

    });