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
                            alertModal, projectService, notebookService, entityTreeService) {
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

    function getService(type) {
        if (type === 'project') {
            return projectService;
        }
        if (type === 'notebook') {
            return notebookService;
        }

        return entityTreeService;
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
                        return service.save({projectId: tab.params.parentId}, entity, function(result) {
                            entityTreeService.addNotebook(result, tab.params.parentId);
                        }).$promise;
                    }

                    // project
                    return service.save(entity, entityTreeService.addProject).$promise;
                }

                return service.update(tab.params, entity, treeServiceUpdate).$promise;
            }
        }

        return $q.resolve();
    }

    function onCloseAllTabs(exceptCurrent) {
        entitiesBrowserService.CloseAllTabs(exceptCurrent);
    }

    function onCloseTabClick($event, tab) {
        $event.stopPropagation();
        if (tab.dirty) {
            alertModal.save('Do you want to save the changes?', null, function(isSave) {
                if (isSave) {
                    saveEntity(tab).then(function() {
                        entitiesBrowserService.closeTab(tab);
                    });
                } else {
                    entitiesBrowserService.closeTab(tab);
                }
            });

            return;
        }

        entitiesBrowserService.closeTab(tab);
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
