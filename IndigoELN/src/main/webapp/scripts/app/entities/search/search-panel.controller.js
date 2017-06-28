(function () {
    angular
        .module('indigoeln')
        .controller('SearchPanelController', SearchPanelController);

    /* @ngInject */
    function SearchPanelController($scope, SearchService, $state, SearchUtilService, pageInfo, EntitiesCache) {
        var OWN_ENTITY = 'OWN_ENTITY';
        var USERS_ENTITIES = 'USERS_ENTITIES';
        var CACHE_STATE_KEY = $state.$current.data.tab.state;
        var vm = this;
        vm.identity = pageInfo.identity;
        vm.users = _.map(pageInfo.users.words, function (item) {
            return {
                name: item.name, id: item.id
            };
        });
        vm.structureTypes = [{
            name: 'Reaction'
        }, {
            name: 'Product'
        }];
        vm.conditionSimilarity = [{
            name: 'equal'
        }, {
            name: 'substructure'
        }, {
            name: 'similarity'
        }];

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

        init();


        function init() {
            if (EntitiesCache.getByName(CACHE_STATE_KEY)) {
                vm.state = EntitiesCache.getByName(CACHE_STATE_KEY);
            } else {
                initDefaultState();
            }
        }


        function initDefaultState() {
            vm.state = {};
            vm.state.model = SearchUtilService.getStoredModel();
            vm.state.$$isCollapsed = SearchUtilService.getStoredOptions().isCollapsed;
            vm.state.selectedItemsFlags = {};
            vm.state.selectedEntitiesFlags = {};
            vm.state.selectedUsers = [];
            vm.state.itemsPerPage = 10;
            vm.state.page = 1;
            vm.state.domainModel = '';
            vm.state.searchResults = [];
            vm.state.searchResultsPaged = [];
        }

        function clear() {
            vm.state.model = SearchUtilService.getStoredModel(true);
        }

        function isAdvancedSearchFilled() {
            return SearchUtilService.isAdvancedSearchFilled(vm.state.model.restrictions.advancedSearch);
        }

        function changeDomain() {
            vm.state.model.restrictions.advancedSearch.entityDomain.value = [];
            if (vm.state.domainModel === OWN_ENTITY) {
                vm.state.model.restrictions.advancedSearch.entityDomain.value.push(vm.identity.id);
            } else if (vm.state.domainModel === USERS_ENTITIES) {
                vm.state.model.restrictions.advancedSearch.entityDomain.value = _.map(vm.state.selectedUsers, function (user) {
                    return user.id;
                });
            }
        }

        function selectedUsersChange() {
            if (vm.state.domainModel === USERS_ENTITIES) {
                vm.state.model.restrictions.advancedSearch.entityDomain.value = _.map(vm.state.selectedUsers, function (user) {
                    return user.id;
                });
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
            var searchRequest = SearchUtilService.prepareSearchRequest(vm.state.model.restrictions);
            SearchService.searchAll(searchRequest, function (result) {
                vm.loading = false;
                vm.state.searchResults = result;
                doPage(1);
            });
        }


        function goTo(entity, print) {
            var url = (print) ? entity.kind.toLowerCase() + '-print' : 'entities.' + entity.kind.toLowerCase() + '-detail';
            $state.go(url, {
                experimentId: entity.experimentId,
                notebookId: entity.notebookId,
                projectId: entity.projectId
            });
        }

        function doPage(page) {
            if (page) {
                vm.state.page = page;
            }

            var ind = (vm.state.page - 1) * vm.state.itemsPerPage;
            vm.state.searchResultsPaged = vm.state.searchResults.slice(ind, ind + vm.state.itemsPerPage);
        }

        function onChangeModel(model) {
            vm.state.model = model;
        }

        $scope.$on('$destroy', function () {
            EntitiesCache.putByName(CACHE_STATE_KEY, vm.state);
        });
    }
})();

