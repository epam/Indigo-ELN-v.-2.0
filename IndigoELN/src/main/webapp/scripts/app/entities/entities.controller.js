angular.module('indigoeln')
    .controller('EntitiesController', function ($scope, EntitiesBrowser, $rootScope, $q,
                                                $location, $state, Principal, EntitiesCache, AlertModal, Experiment, Notebook, Project, DialogService) {





        var init = function () {

            $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
            $scope.PROJECT_CREATOR = 'PROJECT_CREATOR';
            $scope.NOTEBOOK_CREATOR = 'NOTEBOOK_CREATOR';
            $scope.EXPERIMENT_CREATOR = 'EXPERIMENT_CREATOR';
            $scope.GLOBAL_SEARCH = 'GLOBAL_SEARCH';
            $scope.PROJECT_CREATORS = [$scope.CONTENT_EDITOR, $scope.PROJECT_CREATOR].join(',');
            $scope.NOTEBOOK_CREATORS = [$scope.CONTENT_EDITOR, $scope.NOTEBOOK_CREATOR].join(',');
            $scope.EXPERIMENT_CREATORS = [$scope.CONTENT_EDITOR, $scope.EXPERIMENT_CREATOR].join(',');
            $scope.ENTITY_CREATORS = [$scope.CONTENT_EDITOR, $scope.PROJECT_CREATOR, $scope.NOTEBOOK_CREATOR, $scope.EXPERIMENT_CREATOR].join(',');

            $scope.Principal = Principal;

            var userId =  Principal.getIdentity().id;
            $scope.entities = EntitiesBrowser.tabs[userId];


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
            var entityUpdatedListener = $rootScope.$on('entity-updated', function(event, data) {
                EntitiesBrowser.getTabByParams(data.entity).then(function(tab) {
                   if (tab && userId !== data.user) {
                       $scope.onTabChanged(tab);
                   }
                });
            });

            $scope.$on('$destroy', function () {
                unsubscribe();
                entityClickListener();
                entityCloseListener();
                entityCloseAllListener();
                entityUpdatedListener();
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

        $scope.onTabChanged = function(tab) {
            if (tab.dirty) {
                AlertModal.alert('Warning', tab.name + ' ' + tab.$$title + ' has been changed by another user while you have not applied changes. Do you want to keep it open without possibility to save?.',
                    null, function() {}, function() {
                        onSaveTab(tab);
                    }, 'Yes', true
                );
            } else {
                AlertModal.alert('Warning', tab.name + ' ' + tab.$$title + ' has been changed by another user, it will be closed. You will need to reopen it.',
                    null, function() {
                    onSaveTab(tab);
                }, null, 'Ok', true);
            }
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

        $scope.openSearch = function () {
            $state.go('entities.search-panel')
        };

        $scope.onTabClick = function (tab) {
            EntitiesBrowser.goToTab(tab);
        };

        init();

    });
