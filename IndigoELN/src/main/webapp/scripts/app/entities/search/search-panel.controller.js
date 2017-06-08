angular.module('indigoeln')
    .controller('SearchPanelController', function ($rootScope, $scope, $sce, $filter, SearchService, $state, SearchUtilService, pageInfo) {
        var OWN_ENTITY = 'OWN_ENTITY';
        var USERS_ENTITIES = 'USERS_ENTITIES';
        $scope.identity = pageInfo.identity;
        $scope.users = _.map(pageInfo.users.words, function (item) {
            return {name: item.name, id: item.id};
        });
        $scope.model = SearchUtilService.getStoredModel();
        $scope.$$isCollapsed = SearchUtilService.getStoredOptions().isCollapsed;

        $scope.structureTypes = [{name:'Reaction'},{name:'Product'}];
        $scope.conditionSimilarity = [{name: 'equal'}, {name: 'substructure'}, {name: 'similarity'}];

        $scope.clear = function() {
            $scope.model = SearchUtilService.getStoredModel(true);
        };

        $scope.isAdvancedSearchFilled = function () {
            return SearchUtilService.isAdvancedSearchFilled($scope.model.restrictions.advancedSearch);
        };

        $scope.changeDomain = function () {
            $scope.model.restrictions.advancedSearch.entityDomain.value = [];
            if($scope.domainModel === OWN_ENTITY){
                $scope.model.restrictions.advancedSearch.entityDomain.value.push($scope.identity.id);
            }else if($scope.domainModel === USERS_ENTITIES){
                $scope.model.restrictions.advancedSearch.entityDomain.value = _.map($scope.selectedUsers, function(user){
                    return user.id;
                });
            }
        };
        $scope.$on('toggle-search', function(e, data) {
            $scope.model.restrictions.searchQuery = data.query;
            $scope.search()
        })
        $scope.selectedUsersChange = function () {
            if($scope.domainModel === USERS_ENTITIES){
                $scope.model.restrictions.advancedSearch.entityDomain.value = _.map($scope.selectedUsers, function(user){
                    return user.id;
                });
            }
        };

        $scope.selectedItemsFlags = {};

        $scope.selectItem = function (item) {
            if ($scope.selectedItemsFlags[item]) {
                $scope.model.restrictions.advancedSearch.statusCriteria.value.push(item)
            } else {
                var index = _.indexOf($scope.model.restrictions.advancedSearch.statusCriteria.value, item);
                if(index!= -1){
                    $scope.model.restrictions.advancedSearch.statusCriteria.value.splice(index, 1);
                }
            }
        };

        $scope.selectedEntitiesFlags = {};

        $scope.selectEntity = function (item) {
            if ($scope.selectedEntitiesFlags[item]) {
                $scope.model.restrictions.advancedSearch.entityTypeCriteria.value.push(item)
            } else {
                var index = _.indexOf($scope.model.restrictions.advancedSearch.entityTypeCriteria.value, item);
                if(index!= -1){
                    $scope.model.restrictions.advancedSearch.entityTypeCriteria.value.splice(index, 1);
                }
            }
        };

        $scope.search = function () {
            $scope.loading = true;
            var searchRequest = SearchUtilService.prepareSearchRequest($scope.model.restrictions);
            SearchService.searchAll(searchRequest, function (result) {
                $scope.loading = false;
                $scope.searchResults = result;
                $scope.doPage(1)
                console.log('result', result)
            });
        };
        $scope.itemsPerPage= 10;

        $scope.goTo = function(entity, print) {
            var url = (print) ? entity.kind.toLowerCase() + '-print' : 'entities.' + entity.kind.toLowerCase() + '-detail';
            $state.go(url, { experimentId  : entity.experimentId, notebookId  : entity.notebookId, projectId  : entity.projectId  });
        }

        $scope.doPage = function(page) {
            if (page)
                $scope.page = page;
            else
                page = $scope.page;
            var ind = (page - 1) * $scope.itemsPerPage;
            $scope.searchResultsPaged = $scope.searchResults.slice(ind, ind + $scope.itemsPerPage);
            console.log($scope.searchResults, $scope.searchResultsPaged)
        }

   });
