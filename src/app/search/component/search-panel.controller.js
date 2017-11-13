/* @ngInject */
function SearchPanelController($scope, searchService, $state, $stateParams, searchUtilService, pageInfo,
                               entitiesCache, printModal, dictionaryService) {
    var OWN_ENTITY = 'OWN_ENTITY';
    var USERS_ENTITIES = 'USERS_ENTITIES';
    var CACHE_STATE_KEY = $state.$current.data.tab.state;
    var vm = this;

    init();

    function init() {
        vm.identity = pageInfo.identity;
        vm.users = _.map(pageInfo.users.words, function(item) {
            return {
                name: item.name, id: item.id
            };
        });
        vm.structureTypes = [
            {
                name: 'Reaction'
            },
            {
                name: 'Product'
            }
        ];
        vm.conditionSimilarity = [
            {
                name: 'equal'
            },
            {
                name: 'substructure'
            },
            {
                name: 'similarity'
            }
        ];

        vm.clear = clear;
        vm.isAdvancedSearchFilled = isAdvancedSearchFilled;
        vm.changeDomain = changeDomain;
        vm.selectedUsersChange = selectedUsersChange;
        vm.selectItem = selectItem;
        vm.selectEntity = selectEntity;
        vm.search = search;
        vm.goTo = goTo;
        vm.doPage = doPage;
        vm.onChangeModel = onChangeModel;
        vm.printEntity = printEntity;

        if (entitiesCache.getByName(CACHE_STATE_KEY)) {
            vm.state = entitiesCache.getByName(CACHE_STATE_KEY);
        } else {
            initDefaultState();
        }
        if ($stateParams.query) {
            vm.state.model.restrictions.searchQuery = $stateParams.query;
            search();
        }
    }

    function initDefaultState() {
        vm.state = {};
        vm.clearStructureTrigger = 0;
        vm.state.model = searchUtilService.getStoredModel();
        vm.state.$$isCollapsed = searchUtilService.getStoredOptions().isCollapsed;
        vm.state.selectedItemsFlags = {};
        vm.state.selectedEntitiesFlags = {};
        vm.state.selectedUsers = [];
        vm.state.itemsPerPage = 10;
        vm.state.page = 1;
        vm.state.domainModel = '';
        vm.state.searchResults = [];
        vm.state.searchResultsPaged = [];

        initDropdownInfoForSelectSearch();
    }

    function initDropdownInfoForSelectSearch() {
        _.forEach(vm.state.model.restrictions.advancedSearch, function(data) {
            if (data.isSelect) {
                dictionaryService.get({
                    id: data.field
                }).$promise.then(function(dictionary) {
                    vm.state.model.restrictions.advancedSearch[data.field].searchConditions = dictionary.words;
                });
            }
        });
    }

    function clear() {
        vm.clearStructureTrigger = !vm.clearStructureTrigger;
        vm.state.model = searchUtilService.getStoredModel(true);
        vm.state.searchResults = [];
        vm.state.searchResultsPaged = [];

        initDropdownInfoForSelectSearch();
    }

    function isAdvancedSearchFilled() {
        return searchUtilService.isAdvancedSearchFilled(vm.state.model.restrictions.advancedSearch);
    }

    function changeDomain() {
        vm.state.model.restrictions.advancedSearch.entityDomain.value = [];
        if (vm.state.domainModel === OWN_ENTITY) {
            vm.state.model.restrictions.advancedSearch.entityDomain.value.push(vm.identity.id);
        } else if (vm.state.domainModel === USERS_ENTITIES) {
            vm.state.model.restrictions.advancedSearch.entityDomain.value = _.map(
                vm.state.selectedUsers,
                function(user) {
                    return user.id;
                }
            );
        }
    }

    function selectedUsersChange() {
        if (vm.state.domainModel === USERS_ENTITIES) {
            vm.state.model.restrictions.advancedSearch.entityDomain.value = _.map(
                vm.state.selectedUsers,
                function(user) {
                    return user.id;
                }
            );
        }
    }

    function selectItem(item) {
        if (vm.state.selectedItemsFlags[item]) {
            vm.state.model.restrictions.advancedSearch.statusCriteria.value.push(item);
        } else {
            var index = _.indexOf(vm.state.model.restrictions.advancedSearch.statusCriteria.value, item);
            if (index !== -1) {
                vm.state.model.restrictions.advancedSearch.statusCriteria.value.splice(index, 1);
            }
        }
    }

    function selectEntity(item) {
        if (vm.state.selectedEntitiesFlags[item]) {
            vm.state.model.restrictions.advancedSearch.entityTypeCriteria.value.push(item);
        } else {
            var index = _.indexOf(vm.state.model.restrictions.advancedSearch.entityTypeCriteria.value, item);
            if (index !== -1) {
                vm.state.model.restrictions.advancedSearch.entityTypeCriteria.value.splice(index, 1);
            }
        }
    }

    function search() {
        vm.loading = true;
        var searchRequest = searchUtilService.prepareSearchRequest(vm.state.model.restrictions);
        searchService.searchAll(searchRequest, function(result) {
            vm.loading = false;
            vm.state.searchResults = result;
            doPage(1);
        });
    }

    function printEntity(entity) {
        printModal
            .showPopup(getParameters(entity), entity.kind)
            .catch(function() {
                $state.go('^');
            });
    }

    function getParameters(entity) {
        return {
            experimentId: entity.experimentId,
            notebookId: entity.notebookId,
            projectId: entity.projectId
        };
    }

    function goTo(entity) {
        var url = 'entities.' + entity.kind.toLowerCase() + '-detail';
        $state.go(url, getParameters(entity));
    }

    function doPage(page) {
        if (page) {
            vm.state.page = page;
        }
        var ind = (vm.state.page - 1) * vm.state.itemsPerPage;
        vm.state.searchResultsPaged = vm.state.searchResults.slice(ind, ind + vm.state.itemsPerPage);
    }

    function onChangeModel(structure) {
        angular.extend(vm.state.model.restrictions.structure, structure);
    }

    $scope.$on('$destroy', function() {
        entitiesCache.putByName(CACHE_STATE_KEY, vm.state);
    });
}

module.exports = SearchPanelController;
