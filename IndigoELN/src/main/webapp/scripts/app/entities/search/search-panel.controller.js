angular.module('indigoeln')
    .controller('SearchPanelController', function ($rootScope, $scope, $sce, $filter, SearchService, SearchUtilService, pageInfo) {
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



        $scope.columns = [
            {
                id: 'kind',
                name: 'Entity Type'
            },
            {
                id: 'name',
                name: 'Entity Name/Id'
            },
            {
                id: 'details',
                name: 'Details',
                type: 'html',
                format: function (val) {
                    var result = [];
                    if (val.creationDate) {
                        result.push('Creation date: ' + $filter('date')(val.creationDate, 'MMM DD, YYYY HH:mm:ss z'));
                    }
                    if (val.author) {
                        result.push('Owner: ' + val.author);
                    }
                    if (val.title) {
                        result.push('Subject\\Title: ' + val.title);
                    }
                    return $sce.trustAsHtml(result.join('<br/>'));
                }
            },
            {
                id: 'actions',
                name: 'Actions'
            }

        ];


        $scope.search = function () {
            var searchRequest = SearchUtilService.prepareSearchRequest($scope.model.restrictions);
            SearchService.searchAll(searchRequest, function (result) {
                $scope.searchResults = result;
            });
        };

    });
