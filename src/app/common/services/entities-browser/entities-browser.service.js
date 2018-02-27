/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/* @ngInject */
function entitiesBrowserService($q, $state, notifyService, dialogService,
                                autorecoveryCache, principalService, tabKeyService, CacheFactory,
                                entitiesCache, projectService, notebookService,
                                experimentService) {
    var tabs = {};
    var activeTab = {};
    var entityActions;
    var restored = false;

    var resolvePrincipal = function(func) {
        return principalService.checkIdentity().then(func);
    };
    var tabCache;
    if (!CacheFactory.get('tabCache')) {
        tabCache = CacheFactory.createCache('tabCache', {
            storageMode: 'localStorage',
            // a week
            maxAge: 60 * 60 * 1000 * 24 * 7,
            deleteOnExpire: 'aggressive',
            // 10 minutes
            recycleFreq: 60 * 10000
        });
    }

    return {
        getTabs: getTabs,
        setEntityActions: setEntityActions,
        getEntityActions: getEntityActions,
        goToTab: goToTab,
        saveEntity: saveEntity,
        close: close,
        getActiveTab: getActiveTab,
        setCurrentTabTitle: setCurrentTabTitle,
        changeDirtyTab: changeDirtyTab,
        addTab: addTab,
        getTabByParams: getTabByParams,
        restoreTabs: restoreTabs,
        setExperimentTab: setExperimentTab,
        getExperimentTab: getExperimentTab,
        closeTab: closeTab,
        closeAllTabs: closeAllTabs
    };

    function getExperimentTabById(user, experimentFullId) {
        return user.id + '.' + experimentFullId + '.current-exp-tab';
    }

    function setExperimentTab(index, experimentFullId) {
        return resolvePrincipal().then(function(user) {
            tabCache.put(getExperimentTabById(user, experimentFullId), index);
        });
    }

    function getExperimentTab(experimentFullId) {
        return resolvePrincipal().then(function(user) {
            return tabCache.get(getExperimentTabById(user, experimentFullId));
        });
    }

    function getUserId(user) {
        var id = user.id;
        tabs[id] = tabs[id] || {};

        return id;
    }

    function getTabKey(tab) {
        return tab && tab.tabKey ? tab.tabKey : tabKeyService.getTabKeyFromTab(tab);
    }

    function getTabs(success) {
        resolvePrincipal(function(user) {
            success(tabs[user.id]);
        });
    }

    function setEntityActions(actions) {
        entityActions = actions;
    }

    function getEntityActions() {
        return entityActions;
    }

    function goToTab(tab) {
        var curTab = tab;

        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            var tabKey = getTabKey(curTab);
            if (tabs[userId][tabKey]) {
                $state.go(tab.state, tab.params);
            }
        });
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

    function saveEntity(tab) {
        var entity = entitiesCache.get(tab.params);
        if (entity) {
            var service = getService(tab.kind);

            if (service) {
                if (tab.params.isNewEntity) {
                    if (tab.params.parentId) {
                        // notebook
                        return service.save({projectId: tab.params.parentId}, entity).$promise;
                    }

                    // project
                    return service.save(entity).$promise;
                }

                return service.update(tab.params, entity).$promise;
            }
        }

        return $q.resolve();
    }

    function close(tabKey) {
        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            deleteClosedTabAndGoToActive(userId, tabKey);
        });
    }

    function setActiveTab(tab) {
        activeTab = tab;
        entityActions = null;
    }

    function getActiveTab() {
        return activeTab;
    }

    function getTab(user, stateParams) {
        var userId = getUserId(user);
        var result = tabKeyService.getTabKeyFromParams(stateParams);

        return tabs[userId][result];
    }

    function setCurrentTabTitle(tabTitle, stateParams) {
        return resolvePrincipal(function(user) {
            var tab = getTab(user, stateParams);
            if (tab) {
                tab.$$title = tabTitle;
                tab.title = tabTitle;
                saveTabs(user);
            }
        });
    }

    function saveTabs(user) {
        if (!user) {
            return;
        }
        var storageKey = user.id + '.current-tabs';
        var id = user.id;
        var tabsToSave = angular.copy(tabs[id]);
        _.forEach(tabsToSave, function(tab) {
            delete tab.dirty;
        });
        tabCache.put(storageKey, tabsToSave);
    }

    function restoreTabs(user) {
        var storageKey = user.id + '.current-tabs';
        var restoredTabs = tabCache.get(storageKey);
        restored = true;
        _.forEach(restoredTabs, function(tab, tabKey) {
            if (!tabs[user.id][tabKey]) {
                tab.tabKey = tabKey;
                tab.$$title = tab.title;
                tabs[user.id][tabKey] = tab;
            }
        });
    }

    function changeDirtyTab(stateParams, dirty) {
        return resolvePrincipal(function(user) {
            var tab = getTab(user, stateParams);
            if (tab) {
                tab.dirty = dirty;
            }
        });
    }

    function addTab(tab) {
        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            var tabKey = tabKeyService.getTabKeyFromTab(tab);

            if (!tabs[userId][tabKey]) {
                tab.tabKey = tabKey;
                tabs[userId][tabKey] = tab;
                if (restored) {
                    saveTabs(user);
                }
            }

            setActiveTab(tabs[userId][tabKey]);
        });
    }

    function getTabByParams(params) {
        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            var tabKey = tabKeyService.getTabKeyFromParams(params);

            return tabs[userId][tabKey];
        });
    }

    function deleteClosedTabAndGoToActive(userId, tabKey) {
        var keys = _.keys(tabs[userId]);
        var positionForClose = _.indexOf(keys, tabKey);
        var curPosition = _.indexOf(keys, activeTab.tabKey);
        var nextKey;
        var storageKey = userId + '.current-tabs';
        if (curPosition === positionForClose) {
            nextKey = keys[positionForClose - 1] || keys[positionForClose + 1];
        } else {
            nextKey = keys[curPosition];
        }
        delete tabs[userId][tabKey];
        if (tabs[userId][nextKey]) {
            goToTab(tabs[userId][nextKey]);
        } else {
            $state.go('experiment');
        }
        removeTabFromCache(storageKey, tabKey);
        saveTabs();
    }

    function removeTabFromCache(storageKey, tabKey) {
        var cacheTabs = tabCache.get(storageKey);

        cacheTabs = _.omit(cacheTabs, tabKey);
        tabCache.put(storageKey, cacheTabs);
    }

    function closeTab(tab) {
        close(tab.tabKey);
        entitiesCache.removeByKey(tab.tabKey);
        autorecoveryCache.remove(tab.params);
    }

    function openCloseDialog(editTabs) {
        return dialogService
            .selectEntitiesToSave(editTabs)
            .then(function(tabsToSave) {
                var savePromises = [];
                _.forEach(tabsToSave, function(tabToSave) {
                    var pr = saveEntity(tabToSave)
                        .then(function() {
                            closeTab(tabToSave);
                        })
                        .catch(function() {
                            notifyService.error('Error saving ' + tabToSave.kind + ' ' + tabToSave.name + '.');
                        });

                    savePromises.push(pr);
                });

                _.each(editTabs, function(tab) {
                    if (!_.find(tabsToSave, {tabKey: tab.tabKey})) {
                        closeTab(tab);
                    }
                });

                return $q.all(savePromises);
            });
    }

    function doCloseAllTabs(userId, exceptCurrent) {
        var tabsToClose = tabs[userId];
        if (exceptCurrent) {
            tabsToClose = _.filter(tabs[userId], function(tab) {
                return tab !== activeTab;
            });
        }

        var modifiedTabs = [];
        var unmodifiedTabs = [];
        _.each(tabsToClose, function(tab) {
            if (tab.dirty) {
                modifiedTabs.push(tab);
            } else {
                unmodifiedTabs.push(tab);
            }
        });

        if (modifiedTabs.length) {
            return openCloseDialog(modifiedTabs).then(function() {
                _.each(unmodifiedTabs, closeTab);

                return true;
            });
        }

        _.each(unmodifiedTabs, closeTab);

        return $q.when(null);
    }

    function closeAllTabs(exceptCurrent) {
        return resolvePrincipal(function(user) {
            return doCloseAllTabs(user.id, exceptCurrent);
        });
    }
}

module.exports = entitiesBrowserService;
