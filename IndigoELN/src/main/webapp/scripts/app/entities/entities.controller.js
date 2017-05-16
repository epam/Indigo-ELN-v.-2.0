angular.module('indigoeln')
    .controller('EntitiesController', function ($scope, EntitiesBrowser, $rootScope, $q,
                                                $location, $state, Principal, EntitiesCache, AlertModal, AutoRecoverEngine, Alert, Experiment, Notebook, Project, DialogService, AutoRecoverEngine) {





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
                    console.warn('entity-updated', userId, data)
                    if (tab && userId !== data.user) {
                       $scope.onTabChanged(tab, data.entity);
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
                                clearRecovery(tab)
                            });
                    }
                });
            }
            return $q.resolve();
        };

        $scope.onTabChanged = function(tab, entity) {
            if (!EntitiesBrowser.updateCurrentEntity) return;
            if (tab.dirty) {
                AlertModal.alert('Warning', tab.name + ' ' + tab.$$title + ' has been changed by another user while you have not applied changes. You can Accept or Reject saved changes. "Accept" button reloads page to show saved data, "Reject" button leave entered data and allows you to save them.',
                    null,
                    function() {
                        EntitiesBrowser.updateCurrentEntity()
                    },
                    function() {
                        EntitiesBrowser.updateCurrentEntity(true)
                    }, 'Accept', true, 'Reject'
                );
            } else {
                Alert.info(tab.name + ' ' + tab.$$title + ' has been changed by another user and reloaded')
                EntitiesBrowser.updateCurrentEntity()
            }
        };

        $scope.onUndo = function () {
            AutoRecoverEngine.undoAction(EntitiesBrowser.activeExperiment);
        }

        $scope.onRedo = function () {
            AutoRecoverEngine.redoAction(EntitiesBrowser.activeExperiment)
        }

        $scope.canUndo = function () {
            AutoRecoverEngine.canUndo(EntitiesBrowser.activeExperiment)
        }
        
        $scope.canRedo = function () {
            AutoRecoverEngine.canRedo(EntitiesBrowser.activeExperiment)
        }

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
                                clearRecovery(tab)
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

        function clearRecovery(tab) {
            var entityPromise = EntitiesCache.getByKey(tab.tabKey);
            if (entityPromise) {
                entityPromise.then(function (entity) {
                     AutoRecoverEngine.clearRecovery(tab.kind, entity)
                })
            }
        }
        $scope.onCloseTabClick = function (tab) {
            if (tab.dirty) {
                AlertModal.save('Do you want to save the changes?', null, function (isSave) {
                    if (isSave) {
                        saveEntity(tab);
                        return;
                    } else {
                        clearRecovery(tab)
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
