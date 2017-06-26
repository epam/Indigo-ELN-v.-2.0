(function () {
    angular.module('indigoeln')
        .controller('EntitiesController', EntitiesController);

    function EntitiesController($scope, EntitiesBrowser, $rootScope, $q,
                                Principal, EntitiesCache, AlertModal,
                                AutoRecoverEngine, Alert, Experiment, Notebook, Project, DialogService) {
        var vm = this;

        vm.onTabClick = onTabClick;
        vm.onCloseTabClick = onCloseTabClick;
        vm.onCloseAllTabs = onCloseAllTabs;

        init();


        function init() {

            EntitiesBrowser.getTabs(function (tabs) {
                vm.tabs = tabs;
                vm.activeTab = EntitiesBrowser.getActiveTab();
            });
            var events = bindEvents();

            $scope.$on('$destroy', function () {
                _.each(events, function (event) {
                    event();
                });
            });
        }


        function onSaveTab(tab) {
            EntitiesBrowser.close(tab.tabKey);
            EntitiesCache.removeByKey(tab.tabKey);
        }

        //TODO: need to inject service by name but app does't have root elem
        function getService(kind) {
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
                default:
                    break;
            }

            return service;
        }


        function saveEntity(tab) {
            var entityPromise = EntitiesCache.getByKey(tab.tabKey);
            if (entityPromise) {
                entityPromise.then(function (entity) {
                    var service = getService(tab.kind);
                    if (service) {
                        return service.update(tab.params, entity).$promise
                            .then(function () {
                                onSaveTab(tab);
                                clearRecovery(tab);
                            });
                    }
                });
            }
            return $q.resolve();
        }


        function onTabChanged(tab) {
            if (!EntitiesBrowser.getUpdateCurrentEntityFunc()) {
                return;
            }

            if (tab.dirty) {
                AlertModal.alert('Warning', tab.name + ' ' + tab.$$title +
                    ' has been changed by another user while you have not applied changes. ' +
                    'You can Accept or Reject saved changes. ' +
                    '"Accept" button reloads page to show saved data,' +
                    ' "Reject" button leave entered data and allows you to save them.',
                    null,
                    function () {
                        EntitiesBrowser.callUpdateCurrentEntity();
                    },
                    function () {
                        EntitiesBrowser.callUpdateCurrentEntity(true);
                    }, 'Accept', true, 'Reject'
                );
                return;
            }
            Alert.info(tab.name + ' ' + tab.$$title + ' has been changed by another user and reloaded');
            EntitiesBrowser.callUpdateCurrentEntity();

        }

        function onCloseAllTabs(exceptCurrent) {
            var editTabs = _.filter(vm.tabs, function (o) {
                return o.dirty;
            });
            if (editTabs.length) {
                DialogService.selectEntitiesToSave(editTabs, function (tabsToSave) {
                    if (tabsToSave.length) {
                        var saveEntityPromises = [];
                        _.each(tabsToSave, function (tabToSave) {
                            saveEntityPromises.push(saveEntity(tabToSave));
                        });
                        $q.all(saveEntityPromises).then(function () {
                            // close remained tabs
                            _.each($scope.tabs, function (tab) {
                                onSaveTab(tab);
                                clearRecovery(tab);
                            });
                        });
                    } else {
                        _.each($scope.tabs, function (tab) {
                            onSaveTab(tab);
                        });
                    }
                });
                return;
            }
            _.each(vm.tabs, function (tab) {
                //TODO: we cannot compare objects like this
                if (exceptCurrent && tab === EntitiesBrowser.getActiveTab()) {
                    return;
                }
                onSaveTab(tab);
            });
        }

        function clearRecovery(tab) {
            var entityPromise = EntitiesCache.getByKey(tab.tabKey);
            if (entityPromise) {
                entityPromise.then(function (entity) {
                    AutoRecoverEngine.clearRecovery(tab.kind, entity);
                });
            }
        }

        function onCloseTabClick($event, tab) {
            $event.stopPropagation();
            if (tab.dirty) {
                AlertModal.save('Do you want to save the changes?', null, function (isSave) {
                    if (isSave) {
                        saveEntity(tab);
                        return;
                    }
                    clearRecovery(tab);

                    onSaveTab(tab);
                });
                return;
            }
            onSaveTab(tab);
        }


        function onTabClick($event, tab) {
            $event.stopPropagation();
            EntitiesBrowser.goToTab(tab);
        }

        function bindEvents() {
            var events = [];

            events.push($scope.$watch(function () {
                return EntitiesBrowser.getActiveTab();
            }, function (value) {
                vm.activeTab = value;
            }));

            events.push($rootScope.$on('entity-updated', function (event, data) {
                Principal.identity(true).then(function (user) {
                    EntitiesBrowser.getTabByParams(data.entity).then(function (tab) {
                        if (tab && user.id !== data.user) {
                            onTabChanged(tab, data.entity);
                        }
                    });
                });
            }));

            return events;
        }
    }
})();
