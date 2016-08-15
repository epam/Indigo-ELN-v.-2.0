/**
 * Created by Stepan_Litvinov on 2/17/2016.
 */
angular.module('indigoeln')
    .factory('EntitiesBrowser', function ($rootScope, Experiment, Notebook, Project, $q, $state,
                                          Principal, AlertModal, DialogService, AutosaveService, $log) {

        var delay = 5000;

        var tabs = {};
        var cache = {};
        var kindConf = {
            experiment: {
                service: Experiment,
                go: function (params) {
                    $state.go('entities.experiment-detail', params);
                }
            },
            notebook: {
                service: Notebook,
                go: function (params) {
                    $state.go('entities.notebook-detail', params);
                }
            },
            project: {
                service: Project,
                go: function (params) {
                    $state.go('entities.project-detail', params);
                }
            }
        };

        var getUserId = function () {
            var id = Principal.getIdentity().id;
            tabs[id] = tabs[id] || {};
            cache[id] = cache[id] || {};
            return id;
        };

        var resolvePrincipal = function (func) {
            return Principal.identity().then(func);
        };

        var experimentStatusChangedEvent = $rootScope.$on('experiment-status-changed', function (event, statuses) {
            resolvePrincipal(function () {
                var userId = getUserId();
                _.each(statuses, function (status, fullId) {
                    delete cache[userId][fullId];
                });
            });
        });

        var experimentVersionCreatedEvent = $rootScope.$on('experiment-version-created', function (event, data) {
            resolvePrincipal(function () {
                var userId = getUserId();
                _.each(cache[userId], function (cachedItem, fullId) {
                    cachedItem.then(function (item) {
                        if (item.name === data.name) {
                            delete cache[userId][fullId];
                        }
                    });
                });
            });
        });

        var userLogoutEvent = $rootScope.$on('user-logout', function (event, data) {
            cache[data.id] = {};
        });

        $rootScope.$on('$destroy', function () {
            experimentStatusChangedEvent();
            experimentVersionCreatedEvent();
            userLogoutEvent();
        });

        var extractParams = function (obj) {
            return {
                projectId: obj.projectId,
                notebookId: obj.notebookId,
                experimentId: obj.experimentId
            };
        };

        var isEntityChanged = function (entity) {
            return entity.$$form.$dirty;
        };

        var getAllEntities = function (userId) {
            var entityPromises = _.map(tabs[userId], function (tab, fullId) {
                return tabs[userId][fullId];
            });
            return $q.all(entityPromises);
        };

        var getChangedEntities = function (userId) {
            var deferred = $q.defer();
            var promise = deferred.promise;
            var changedEntities = [];
            getAllEntities(userId).then(function (entities) {
                _.each(entities, function (entity) {
                    if (isEntityChanged(entity)) {
                        changedEntities.push(entity);
                    }
                });
                deferred.resolve(changedEntities);
            });
            return promise;
        };

        function deleteClosedTabAndGoToActive(userId, fullId, current, that, forced) {
            if (!forced) {
                var keys = _.keys(tabs[userId]);
                var positionForClose = _.indexOf(keys, fullId);
                var curPosition = _.indexOf(keys, current);
                var nextKey;
                if (curPosition === positionForClose) {
                    nextKey = keys[positionForClose - 1] || keys[positionForClose + 1];
                } else {
                    nextKey = keys[curPosition];
                }
                delete tabs[userId][fullId];
                delete cache[userId][fullId];
                if (current === nextKey) {
                    $rootScope.$broadcast('updateTabs', that.expandIds(current));
                } else {
                    if (keys.length > 1) {
                        that.goToTab(nextKey);
                    } else if (keys.length === 1) {
                        $state.go('experiment');
                    }
                }
            } else {
                delete tabs[userId][fullId];
                delete cache[userId][fullId];
            }
        }

        return {
            compactIds: function (params) {
                params = extractParams(params);
                var paramsArr = [];
                if (params.projectId) {
                    paramsArr.push(params.projectId);
                }
                if (params.notebookId) {
                    paramsArr.push(params.notebookId);
                }
                if (params.experimentId) {
                    paramsArr.push(params.experimentId);
                }
                return paramsArr.join('-');
            },
            expandIds: function (ids) {
                var idsArr = ids.split('-');
                return {projectId: idsArr[0], notebookId: idsArr[1], experimentId: idsArr[2]};
            },
            getKind: function (params) {
                params = extractParams(params);
                if (params.experimentId) {
                    return 'experiment';
                } else if (params.notebookId) {
                    return 'notebook';
                } else {
                    return 'project';
                }
            },
            resolveTabs: function (params) {
                params = extractParams(params);
                var that = this;
                return resolvePrincipal(function () {
                    var userId = getUserId();
                    if (that.compactIds(params)) {
                        tabs[userId][that.compactIds(params)] = that.resolveFromCache(params);
                    }
                    return $q.all(_.values(tabs[userId]));
                });
            },
            getCurrentEntity: function (params) {
                params = extractParams(params);
                var that = this;
                return resolvePrincipal(function () {
                    var userId = getUserId();
                    tabs[userId][that.compactIds(params)] = that.resolveFromCache(params);
                    return tabs[userId][that.compactIds(params)];
                });
            },
            getIdByVal: function (entity) {
                return resolvePrincipal(function () {
                    var userId = getUserId();
                    return Object.keys(tabs).filter(function (key) {
                        return tabs[userId][key] === entity.$promise;
                    })[0];
                });
            },
            goToTab: function (fullId) {
                var params = this.expandIds(fullId);
                kindConf[this.getKind(params)].go(params);
            },
            saveEntity: function (fullId) {
                var that = this;
                var userId = getUserId();
                var deferred = $q.defer();
                var promise = deferred.promise;
                var params = this.expandIds(fullId);
                tabs[userId][fullId].then(function (entity) {
                    kindConf[that.getKind(params)].service.update(params, entity).$promise
                        .then(function () {
                            entity.$$form.$setPristine();
                            deferred.resolve();
                        });
                });
                return promise;
            },
            close: function (fullId, current, forced) {
                var that = this;
                var userId = getUserId();
                var deferred = $q.defer();
                var promise = deferred.promise;
                tabs[userId][fullId].then(function (entity) {
                    if (forced) {
                        deferred.resolve();
                    } else {
                        if (isEntityChanged(entity)) {
                            AlertModal.save('Do you want to save the changes?', null, function (isSave) {
                                if (isSave) {
                                    that.saveEntity(fullId).then(function () {
                                        deferred.resolve();
                                    });
                                } else {
                                    deferred.resolve();
                                }
                            });
                        } else {
                            deferred.resolve();
                        }
                    }
                });
                return promise.then(function () {
                    deleteClosedTabAndGoToActive(userId, fullId, current, that, forced);
                });
            },
            closeAllForced: function (userId) {
                var that = this;
                var closeAllPromises = [];
                getAllEntities(userId).then(function (allEntities) {
                    _.each(allEntities, function (entity) {
                        closeAllPromises.push(that.close(entity.fullId, entity.fullId, true));
                    });
                });
                return $q.all(closeAllPromises).then(function () {
                    $state.go('experiment');
                });
            },
            closeAll: function () {
                var that = this;
                var userId = getUserId();
                var deferred = $q.defer();
                getChangedEntities(userId).then(function (changedEntities) {
                    if (changedEntities.length) {
                        DialogService.selectEntitiesToSave(changedEntities, function (entitiesToSave) {
                            if (entitiesToSave.length) {
                                var saveEntityPromises = [];
                                _.each(entitiesToSave, function (entityToSave) {
                                    saveEntityPromises.push(that.saveEntity(entityToSave.fullId));
                                });
                                $q.all(saveEntityPromises).then(function () {
                                    that.closeAllForced(userId).then(function () {
                                        deferred.resolve();
                                    });
                                });
                            } else {
                                that.closeAllForced(userId).then(function () {
                                    deferred.resolve();
                                });
                            }
                        });
                    } else {
                        that.closeAllForced(userId).then(function () {
                            deferred.resolve();
                        });
                    }
                });
                return deferred.promise;
            },
            resolveFromCache: function (params) {
                params = extractParams(params);
                var that = this;
                return resolvePrincipal(function () {
                    var userId = getUserId();
                    var entitiyId = that.compactIds(params);
                    if (!cache[userId][entitiyId]) {
                        cache[userId][entitiyId] = that.loadEntity(that, params);
                    }
                    cache[userId][entitiyId].catch(function () {
                        delete cache[userId][entitiyId];
                        delete tabs[userId][entitiyId];
                    });
                    return cache[userId][entitiyId];
                });
            },
            loadEntity: function (that, params) {
                var fullId = that.compactIds(params);
                var deferred = $q.defer();
                var loadOrigin = function () {
                    kindConf[that.getKind(params)].service.get(params, function (entity) {
                        deferred.resolve(entity);
                    });
                };
                AutosaveService.get({id: fullId}, function (entity) {
                    if (entity.fullId) {
                        AlertModal.autorecover('Auto-Recover file(s) found for ' + entity.name + '. Do you want to recover from this files?', null, function () {
                            AutosaveService.delete({id: fullId}, function () {
                                deferred.resolve(entity);
                            });
                        }, function () {
                            AutosaveService.delete({id: fullId}, function () {
                                loadOrigin();
                            });
                        });
                    } else {
                        loadOrigin();
                    }
                });
                return deferred.promise;
            },
            updateCacheAndTab: function (params) {
                params = extractParams(params);
                var that = this;
                return resolvePrincipal(function () {
                    var userId = getUserId();
                    var entitiyId = that.compactIds(params);
                    cache[userId][entitiyId] = that.loadEntity(that, params);
                    tabs[userId][entitiyId] = cache[userId][entitiyId];
                    return tabs[userId][entitiyId];
                });
            },
            getTabs: function () {
                return resolvePrincipal(function () {
                    var userId = getUserId();
                    return $q.all(_.values(tabs[userId]));
                });
            },
            getNotebookFromCache: function (params) {
                params = extractParams(params);
                var notebookParams = {projectId: params.projectId, notebookId: params.notebookId};
                return this.resolveFromCache(notebookParams);
            },
            getProjectFromCache: function (params) {
                params = extractParams(params);
                var projectParams = {projectId: params.projectId};
                return this.resolveFromCache(projectParams);
            },
            onEntityChanged: function (entity) {
                AutosaveService.save({id: entity.fullId}, angular.toJson(entity));
            },
            trackEntityChanges: function (form, $scope) {
                var that = this;
                var kind = that.getKind($state.params);
                that.getCurrentEntity($state.params).then(function (entity) {
                    if (entity.$$form && entity.$$form.$dirty) {
                        form.$setDirty();
                    }
                    entity.$$form = form;
                    var onChange = _.debounce(function (entity) {
                        if (entity.$$form && entity.$$form.$dirty) {
                            that.onEntityChanged(entity);
                        }
                    }, delay);
                    var unbind = $scope.$watch(kind, onChange, true);
                    $scope.$on('$destroy', function () {
                        unbind();
                    });
                });
            }
        };
    });