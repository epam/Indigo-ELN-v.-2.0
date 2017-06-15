angular.module('indigoeln')
    .controller('EntitiesController', function($scope, EntitiesBrowser, $rootScope, $q,
        $location, $state, Principal, EntitiesCache, AlertModal, AutoRecoverEngine, Alert, Experiment, Notebook, Project, DialogService) {





        var init = function() {

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

            EntitiesBrowser.getTabs(function(tabs) {
                $scope.entities = tabs;
                $scope.tabs = tabs;
                $scope.activeTab = EntitiesBrowser.getActiveTab();
            });

            var unsubscribe = $scope.$watch(function() {
                return EntitiesBrowser.getActiveTab();
            }, function(value) {
                $scope.activeTab = value;
            });

            var entityClickListener = $scope.$on('entity-clicked', function(event, tab) {
                $scope.onTabClick(tab);
            });
            var entityCloseListener = $scope.$on('entity-closing', function(event, tab) {
                $scope.onCloseTabClick(tab);
            });
            var entityCloseAllListener = $scope.$on('entity-close-all', function() {
                $scope.onCloseAllTabs();
            });
            var entityUpdatedListener = $rootScope.$on('entity-updated', function(event, data) {
                Principal.identity(true).then(function(user) {
                    EntitiesBrowser.getTabByParams(data.entity).then(function(tab) {
                        console.warn('entity-update', user.id, data, tab)
                        if (tab && user.id != data.user) {
                            $scope.onTabChanged(tab, data.entity);
                            console.warn('entity-updated', data.entity)
                        }
                    });
                })
            });

            $scope.$on('$destroy', function() {
                unsubscribe();
                entityClickListener();
                entityCloseListener();
                entityCloseAllListener();
                entityUpdatedListener();
            });

        };

        var onSaveTab = function(tab) {
            EntitiesBrowser.close(tab.tabKey);
            EntitiesCache.removeByKey(tab.tabKey);
        };

        //TODO: need to inject service by name but app does't have root elem
        var getService = function(kind) {
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


        var saveEntity = function(tab) {
            var entityPromise = EntitiesCache.getByKey(tab.tabKey);
            if (entityPromise) {
                entityPromise.then(function(entity) {
                    var service = getService(tab.kind);
                    if (service) {
                        return service.update(tab.params, entity).$promise
                            .then(function() {
                                onSaveTab(tab);
                                clearRecovery(tab)
                            });
                    }
                });
            }
            return $q.resolve();
        };

        $scope.onTabChanged = function(tab, entity) {
            if (!EntitiesBrowser.getUpdateCurrentEntityFunc()) return;

            if (tab.dirty) {
                AlertModal.alert('Warning', tab.name + ' ' + tab.$$title + ' has been changed by another user while you have not applied changes. You can Accept or Reject saved changes. "Accept" button reloads page to show saved data, "Reject" button leave entered data and allows you to save them.',
                    null,
                    function() {
                        EntitiesBrowser.callUpdateCurrentEntity();
                    },
                    function() {
                        EntitiesBrowser.callUpdateCurrentEntity(true)
                    }, 'Accept', true, 'Reject'
                );
            } else {
                Alert.info(tab.name + ' ' + tab.$$title + ' has been changed by another user and reloaded')
                EntitiesBrowser.callUpdateCurrentEntity()
            }
        };

        $scope.onUndo = function() {
            AutoRecoverEngine.undoAction(EntitiesBrowser.getCurrentEntity());
        };

        $scope.onRedo = function() {
            AutoRecoverEngine.redoAction(EntitiesBrowser.getCurrentEntity())
        };

        $scope.canUndo = function() {
            AutoRecoverEngine.canUndo(EntitiesBrowser.getCurrentEntity())
        };

        $scope.canRedo = function() {
            AutoRecoverEngine.canRedo(EntitiesBrowser.getCurrentEntity())
        };

        $scope.onCloseAllTabs = function(exceptCurrent) {
            var editTabs = _.filter($scope.tabs, function(o) {
                return o.dirty;
            });
            if (editTabs.length) {
                DialogService.selectEntitiesToSave(editTabs, function(tabsToSave) {
                    if (tabsToSave.length) {
                        var saveEntityPromises = [];
                        _.each(tabsToSave, function(tabToSave) {
                            saveEntityPromises.push(saveEntity(tabToSave));
                        });
                        $q.all(saveEntityPromises).then(function() {
                            // close remained tabs
                            _.each($scope.tabs, function(tab) {
                                onSaveTab(tab);
                                clearRecovery(tab)
                            });
                        });
                    } else {
                        _.each($scope.tabs, function(tab) {
                            onSaveTab(tab);
                        });
                    }
                });
                return;
            }
            _.each($scope.tabs, function(tab) {
                if (exceptCurrent && tab== EntitiesBrowser.activeTab) return;
                onSaveTab(tab);
            });
        };

        function clearRecovery(tab) {
            var entityPromise = EntitiesCache.getByKey(tab.tabKey);
            if (entityPromise) {
                entityPromise.then(function(entity) {
                    AutoRecoverEngine.clearRecovery(tab.kind, entity)
                })
            }
        }
        $scope.onCloseTabClick = function(tab) {
            if (tab.dirty) {
                AlertModal.save('Do you want to save the changes?', null, function(isSave) {
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

        $scope.openSearch = function() {
            $state.go('entities.search-panel')
        };

        $scope.onTabClick = function(tab) {
            EntitiesBrowser.goToTab(tab);
        };


        init();

    });
