angular.module('indigoeln')
    .controller('EntitiesController', function ($scope, EntitiesBrowser, $rootScope, $q,
                                                $location, $state, Principal, EntitiesCache, AlertModal, Experiment, Notebook, Project, DialogService) {

        var userId = Principal.getIdentity().id;


        var init = function () {
            $scope.tabs = EntitiesBrowser.tabs[userId];
            $scope.activeTab = EntitiesBrowser.activeTab;

            var unsubscribe = $scope.$watch(function () {
                return EntitiesBrowser.activeTab;
            }, function (value) {
                $scope.activeTab = value;
            });



            var entityClickListener = $scope.$on('entity-clicked', function (event, tab) {
                $scope.onTabClick(tab);
            });
            var entityCloseListener = $scope.$on('entity-closing', function (event, tab) {
                $scope.onCloseTabClick(tab);
            });
            var entityCloseAllListener = $scope.$on('entity-close-all', function () {
                $scope.onCloseAllTabs();
            });


            $scope.$on('$destroy', function () {
                unsubscribe();
                entityClickListener();
                entityCloseListener();
                entityCloseAllListener();
            });

        };

        var onSaveTab = function (tab) {
            EntitiesBrowser.close(tab.tabKey);
            EntitiesCache.removeByKey(tab.tabKey);
        };

        //TODO: need to inject service by name but app does't have root elem
        var getService = function (kind) {
            var service;
            switch (kind) {
                case 'project':
                    service = Project;
                    break;
                case 'notebook':
                    service = Notebook;
                    break;
                case 'experiment':
                    service = Experiment;
                    break;
            }

            return service;
        };


        var saveEntity = function(tab){
            var entityPromise = EntitiesCache.getByKey(tab.tabKey);
            if (entityPromise) {
                entityPromise.then(function (entity) {
                    var service = getService(tab.kind);
                    if (service) {
                        return service.update(tab.params, entity).$promise
                            .then(function () {
                                onSaveTab(tab);
                            });
                    }
                });
            }
            return $q.resolve();
        };


        $scope.onCloseAllTabs = function () {
            var editTabs =_.filter($scope.tabs, function(o) { return o.dirty; });
            if(editTabs.length){
                DialogService.selectEntitiesToSave(editTabs, function (tabsToSave) {
                    if (tabsToSave.length) {
                        var saveEntityPromises = [];
                        _.each(tabsToSave, function (tabToSave) {
                            saveEntityPromises.push(saveEntity(tabToSave));
                        });
                        $q.all(saveEntityPromises).then(function () {
                            // close remained tabs
                            _.each($scope.tabs, function(tab){
                                onSaveTab(tab);
                            });
                        });
                    } else {
                        _.each($scope.tabs, function(tab){
                            onSaveTab(tab);
                        });
                    }
                });
                return;
            }
            _.each($scope.tabs, function(tab){
                onSaveTab(tab);
            });
        };

        $scope.onCloseTabClick = function (tab) {
            if (tab.dirty) {
                AlertModal.save('Do you want to save the changes?', null, function (isSave) {
                    if (isSave) {
                        saveEntity(tab);
                        return;
                    }
                    onSaveTab(tab);

                });
                return;
            }
            onSaveTab(tab);
        };

        $scope.onTabClick = function (tab) {
            EntitiesBrowser.goToTab(tab);
        };

        init();

    });
