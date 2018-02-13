/* @ngInject */
function SearchPanelController(searchService, $state, $stateParams, searchUtil, pageInfo,
                               entitiesCache, printModal, dictionaryService, tabKeyService, $scope) {
    var OWN_ENTITY = 'OWN_ENTITY';
    var USERS_ENTITIES = 'USERS_ENTITIES';
    var CACHE_STATE_KEY = tabKeyService.getTabKeyFromTab($state.current.data.tab);
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
        vm.ownEntitySelected = false;

        if (entitiesCache.getByKey(CACHE_STATE_KEY)) {
            vm.state = entitiesCache.getByKey(CACHE_STATE_KEY);
        } else {
            initDefaultState();
        }
        if ($stateParams.query) {
            vm.state.restrictions.searchQuery = $stateParams.query;
            search();
        }

        initEvents();
    }

    function initDefaultState() {
        vm.clearStructureTrigger = 0;
        vm.state = {
            restrictions: searchUtil.getStoredModel(),
            $$isCollapsed: searchUtil.getStoredOptions().isCollapsed,
            selectedItemsFlags: {},
            selectedEntitiesFlags: {},
            selectedUsers: [],
            itemsPerPage: 10,
            page: 1,
            domainModel: '',
            searchResults: [],
            searchResultsPaged: []
        };

        initDropdownInfoForSelectSearch();

        entitiesCache.putByKey(CACHE_STATE_KEY, vm.state);
    }

    function initDropdownInfoForSelectSearch() {
        _.forEach(vm.state.restrictions.advancedSearch, function(data) {
            if (data.isSelect) {
                dictionaryService.get({
                    id: data.field
                }).$promise.then(function(dictionary) {
                    vm.state.restrictions.advancedSearch[data.field].searchConditions = dictionary.words;
                });
            }
        });
    }

    function clear() {
        vm.clearStructureTrigger = !vm.clearStructureTrigger;
        vm.state.restrictions = searchUtil.getStoredModel();
        vm.state.searchResults = [];
        vm.state.searchResultsPaged = [];
        vm.state.domainModel = '';
        vm.state.selectedEntitiesFlags = {};
        vm.state.selectedItemsFlags = {};

        initDropdownInfoForSelectSearch();
    }

    function isAdvancedSearchFilled() {
        return searchUtil.isAdvancedSearchFilled(vm.state.restrictions.advancedSearch);
    }

    function changeDomain() {
        vm.state.restrictions.advancedSearch.entityDomain.value = [];
        vm.ownEntitySelected = vm.state.restrictions.advancedSearch.entityDomain.ownEntitySelected;
        if (vm.state.domainModel === OWN_ENTITY) {
            vm.state.restrictions.advancedSearch.entityDomain.value.push(vm.identity.id);
            vm.ownEntitySelected = true;
        } else if (vm.state.domainModel === USERS_ENTITIES) {
            vm.ownEntitySelected = false;
            vm.state.restrictions.advancedSearch.entityDomain.value = _.map(
                vm.state.selectedUsers,
                function(user) {
                    return user.id;
                }
            );
        }
    }

    function selectedUsersChange() {
        if (vm.state.domainModel === USERS_ENTITIES) {
            vm.state.restrictions.advancedSearch.entityDomain.value = _.map(
                vm.state.selectedUsers,
                function(user) {
                    return user.id;
                }
            );
        }
    }

    function selectItem(item) {
        if (vm.state.selectedItemsFlags[item]) {
            vm.state.restrictions.advancedSearch.statusCriteria.value.push(item);
        } else {
            var index = _.indexOf(vm.state.restrictions.advancedSearch.statusCriteria.value, item);
            if (index !== -1) {
                vm.state.restrictions.advancedSearch.statusCriteria.value.splice(index, 1);
            }
        }
    }

    function selectEntity(item) {
        if (vm.state.selectedEntitiesFlags[item]) {
            vm.state.restrictions.advancedSearch.entityTypeCriteria.value.push(item);
        } else {
            var index = _.indexOf(vm.state.restrictions.advancedSearch.entityTypeCriteria.value, item);
            if (index !== -1) {
                vm.state.restrictions.advancedSearch.entityTypeCriteria.value.splice(index, 1);
            }
        }
    }

    function search() {
        var searchRequest = searchUtil.prepareSearchRequest(vm.state.restrictions);

        vm.state.restrictions.advancedSummary = searchRequest.advancedSearch;
        vm.loading = true;

        searchService.searchAll(searchRequest, function(result) {
            vm.loading = false;
            vm.state.searchResults = result;
            doPage(1);
        });
    }

    function printEntity(entity) {
        printModal.showPopup(getParameters(entity), entity.kind);
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
        angular.extend(vm.state.restrictions.structure, structure);
    }

    function initEvents() {
        $scope.$on('$destroy', function() {
            printModal.close();
        });
    }
}

module.exports = SearchPanelController;
