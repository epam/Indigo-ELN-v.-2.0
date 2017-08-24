(function() {
    angular
        .module('indigoeln')
        .controller('EntitiesController', EntitiesController);

    function EntitiesController($scope, EntitiesBrowser, $q, Principal, EntitiesCache, AlertModal, dialogService,
                                autorecoveryCache, Project, Notebook, Experiment) {
        var vm = this;

        init();

        function init() {
            vm.onTabClick = onTabClick;
            vm.onCloseTabClick = onCloseTabClick;
            vm.onCloseAllTabs = onCloseAllTabs;
            vm.saveEntity = saveEntity;

            bindEvents();
            Principal.identity().then(function(user) {
                EntitiesBrowser.restoreTabs(user);
                EntitiesBrowser.getTabs(function(tabs) {
                    vm.tabs = tabs;
                    vm.activeTab = EntitiesBrowser.getActiveTab();
                });
            });
        }

        function closeTab(tab) {
            EntitiesBrowser.close(tab.tabKey);
            EntitiesCache.removeByKey(tab.tabKey);
            autorecoveryCache.remove(tab.params);
        }

        // TODO: need to inject service by name but app does't have root elem
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
            if (tab === vm.activeTab) {
                var defer = $q.defer();
                $scope.$broadcast('ON_ENTITY_SAVE', {
                    tab: tab,
                    defer: defer
                });

                return defer.promise;
            }

            var entity = EntitiesCache.get(tab.params);
            if (entity) {
                var service = getService(tab.kind);

                return service ? service.update(tab.params, entity).$promise : $q.resovle();
            }

            return $q.resolve();
        }

        function openCloseDialog(editTabs) {
            return dialogService.selectEntitiesToSave(editTabs, function(tabsToSave) {
                return $q.all(_.map(tabsToSave, function(tabToSave) {
                    return saveEntity(tabToSave).then(function() {
                        closeTab(tabToSave);
                    });
                }));
            });
        }

        function onCloseAllTabs(exceptCurrent) {
            var tabs = !exceptCurrent ? vm.tabs : _.filter(vm.tabs, function(tab) {
                return tab !== vm.activeTab;
            });
            var editTabs = _.filter(tabs, function(tab) {
                return tab.dirty && (exceptCurrent ? tab !== vm.activeTab : true);
            });

            $q.when(editTabs.length ? openCloseDialog(editTabs) : null)
                .then(function() {
                    _.each(tabs, closeTab);
                });
        }

        function onCloseTabClick($event, tab) {
            $event.stopPropagation();
            if (tab.dirty) {
                AlertModal.save('Do you want to save the changes?', null, function(isSave) {
                    if (isSave) {
                        saveEntity(tab).then(function() {
                            closeTab(tab);
                        });
                    } else {
                        closeTab(tab);
                    }
                });

                return;
            }

            closeTab(tab);
        }

        function onTabClick($event, tab) {
            $event.stopPropagation();
            EntitiesBrowser.goToTab(tab);
        }

        function bindEvents() {
            $scope.$watch(function() {
                return EntitiesBrowser.getActiveTab();
            }, function(value) {
                vm.activeTab = value;
            });
        }
    }
})();
