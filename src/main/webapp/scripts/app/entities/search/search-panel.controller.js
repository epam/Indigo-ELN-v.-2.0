(function () {
    angular
        .module('indigoeln')
        .controller('SearchPanelController', SearchPanelController);

    /* @ngInject */
    function SearchPanelController($scope, SearchService, $state, SearchUtilService, pageInfo) {
        var OWN_ENTITY = 'OWN_ENTITY';
        var USERS_ENTITIES = 'USERS_ENTITIES';
        var vm = this;
        vm.identity = pageInfo.identity;
        vm.users = _.map(pageInfo.users.words, function (item) {
            return {name: item.name, id: item.id};
        });
        vm.model = SearchUtilService.getStoredModel();
        vm.$$isCollapsed = SearchUtilService.getStoredOptions().isCollapsed;
        vm.structureTypes = [{name: 'Reaction'}, {name: 'Product'}];
        vm.conditionSimilarity = [{name: 'equal'}, {name: 'substructure'}, {name: 'similarity'}];
        vm.selectedItemsFlags = {};
        vm.selectedEntitiesFlags = {};
        vm.selectedUsers = [];
        vm.itemsPerPage = 10;
        vm.domainModel = '';

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

        function clear() {
            vm.model = SearchUtilService.getStoredModel(true);
        }

        function isAdvancedSearchFilled() {
            return SearchUtilService.isAdvancedSearchFilled(vm.model.restrictions.advancedSearch);
        }

        function changeDomain() {
            vm.model.restrictions.advancedSearch.entityDomain.value = [];
            if (vm.domainModel === OWN_ENTITY) {
                vm.model.restrictions.advancedSearch.entityDomain.value.push(vm.identity.id);
            } else if (vm.domainModel === USERS_ENTITIES) {
                vm.model.restrictions.advancedSearch.entityDomain.value = _.map(vm.selectedUsers, function (user) {
                    return user.id;
                });
            }
        }

        function selectedUsersChange() {
            if (vm.domainModel === USERS_ENTITIES) {
                vm.model.restrictions.advancedSearch.entityDomain.value = _.map(vm.selectedUsers, function (user) {
                    return user.id;
                });
            }
        }

        function selectItem(item) {
            if (vm.selectedItemsFlags[item]) {
                vm.model.restrictions.advancedSearch.statusCriteria.value.push(item);
            } else {
                var index = _.indexOf(vm.model.restrictions.advancedSearch.statusCriteria.value, item);
                if (index !== -1) {
                    vm.model.restrictions.advancedSearch.statusCriteria.value.splice(index, 1);
                }
            }
        }

        function selectEntity(item) {
            if (vm.selectedEntitiesFlags[item]) {
                vm.model.restrictions.advancedSearch.entityTypeCriteria.value.push(item);
            } else {
                var index = _.indexOf(vm.model.restrictions.advancedSearch.entityTypeCriteria.value, item);
                if (index !== -1) {
                    vm.model.restrictions.advancedSearch.entityTypeCriteria.value.splice(index, 1);
                }
            }
        }

        function search() {
            vm.loading = true;
            var searchRequest = SearchUtilService.prepareSearchRequest(vm.model.restrictions);
            SearchService.searchAll(searchRequest, function (result) {
                vm.loading = false;
                vm.searchResults = result;
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
                vm.page = page;
            }

            var ind = (vm.page - 1) * vm.itemsPerPage;
            vm.searchResultsPaged = vm.searchResults.slice(ind, ind + vm.itemsPerPage);
        }

        function onChangeModel(model) {
            vm.model = model;
        }
    }
})();

