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

/* @ngInject */
function EntitiesController($scope, entitiesBrowserService, $q, principalService, entitiesCache,
                            alertModal, dialogService, autorecoveryCache, projectService,
                            notebookService, experimentService, notifyService, entityTreeService) {
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

    function getService(type) {
        if (type === 'project') {
            return projectService;
        }
        if (type === 'notebook') {
            return notebookService;
        }

        return experimentService;
    }

    function getTreeServiceMethod(type) {
        if (type === 'project') {
            return entityTreeService.updateProject;
        }
        if (type === 'notebook') {
            return entityTreeService.updateNotebook;
        }

        return entityTreeService.updateExperiment;
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
            var treeServiceUpdate = getTreeServiceMethod(tab.kind);

            if (service) {
                if (tab.params.isNewEntity) {
                    if (tab.params.parentId) {
                        // notebook
                        return service.save({projectId: tab.params.parentId}, entity).$promise.then(function(result) {
                            entity.id = result.id;
                            entity.fullId = tab.params.parentId + '-' + entity.id;
                            entityTreeService.addNotebook(entity, tab.params.parentId);

                            return true;
                        });
                    }

                    // project
                    return service.save(entity).$promise.then(function(result) {
                        entity.id = result.id;
                        entityTreeService.addProject(entity);

                        return true;
                    });
                }

                return service.update(tab.params, entity).$promise.then(function() {
                    treeServiceUpdate(entity);

                    return true;
                });
            }
        }

        return $q.resolve();
    }

    function openCloseDialog(editTabs) {
        return dialogService
            .selectEntitiesToSave(editTabs)
            .then(function(tabsToSave) {
                var savePromises = _.map(tabsToSave, function(tabToSave) {
                    return saveEntity(tabToSave)
                        .then(function() {
                            closeTab(tabToSave);
                        })
                        .catch(function() {
                            notifyService.error('Error saving ' + tabToSave.kind + ' ' + tabToSave.name + '.');
                        });
                });

                _.each(editTabs, function(tab) {
                    if (!_.find(tabsToSave, {tabKey: tab.tabKey})) {
                        closeTab(tab);
                    }
                });

                return $q.all(savePromises);
            });
    }

    function onCloseAllTabs(exceptCurrent) {
        var tabsToClose = !exceptCurrent ? vm.tabs : _.filter(vm.tabs, function(tab) {
            return tab !== vm.activeTab;
        });
        var modifiedTabs = [];
        var unmodifiedTabs = [];
        _.each(tabsToClose, function(tab) {
            if (tab.dirty) {
                modifiedTabs.push(tab);
            } else {
                unmodifiedTabs.push(tab);
            }
        });

        $q.when(modifiedTabs.length ? openCloseDialog(modifiedTabs) : null)
            .finally(function() {
                _.each(unmodifiedTabs, closeTab);
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
