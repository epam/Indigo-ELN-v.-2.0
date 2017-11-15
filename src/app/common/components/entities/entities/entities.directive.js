var template = require('./entities.html');

function entities() {
    return {
        scope: true,
        template: template,
        controller: EntitiesController,
        controllerAs: 'vm',
        bindToController: true
    };
}

EntitiesController.$inject = ['$scope', 'entitiesBrowserService', '$q', 'principalService', 'entitiesCache',
    'alertModal', 'dialogService', 'autorecoveryCache',
    'projectService', 'notebookService', 'experimentService'];

function EntitiesController($scope, entitiesBrowserService, $q, principalService, entitiesCache,
                            alertModal, dialogService, autorecoveryCache, projectService,
                            notebookService, experimentService) {
    var vm = this;

    init();

    function init() {
        vm.onTabClick = onTabClick;
        vm.onCloseTabClick = onCloseTabClick;
        vm.onCloseAllTabs = onCloseAllTabs;
        vm.saveEntity = saveEntity;

        bindEvents();
        principalService.checkIdentity().then(function(user) {
            entitiesBrowserService.restoreTabs(user);
            entitiesBrowserService.getTabs(function(tabs) {
                vm.tabs = tabs;
                vm.activeTab = entitiesBrowserService.getActiveTab();
            });
        });
    }

    function closeTab(tab) {
        entitiesBrowserService.close(tab.tabKey);
        entitiesCache.removeByKey(tab.tabKey);
        autorecoveryCache.remove(tab.params);
    }

    function getService(kind) {
        var service;
        switch (kind) {
            case 'project':
                service = projectService;
                break;
            case 'notebook':
                service = notebookService;
                break;
            case 'experiment':
                service = experimentService;
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

        var entity = entitiesCache.get(tab.params);
        if (entity) {
            var service = getService(tab.kind);

            return service ? service.update(tab.params, entity).$promise : $q.resovle();
        }

        return $q.resolve();
    }

    function openCloseDialog(editTabs) {
        return dialogService
            .selectEntitiesToSave(editTabs)
            .then(function(tabsToSave) {
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
            alertModal.save('Do you want to save the changes?', null, function(isSave) {
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
        entitiesBrowserService.goToTab(tab);
    }

    function bindEvents() {
        $scope.$watch(function() {
            return entitiesBrowserService.getActiveTab();
        }, function(value) {
            vm.activeTab = value;
        });
    }
}

module.exports = entities;
