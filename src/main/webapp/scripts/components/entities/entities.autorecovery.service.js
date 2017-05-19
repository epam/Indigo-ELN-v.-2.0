angular.module('indigoeln')
    .factory('AutoRecoverEngine', function($rootScope, AlertModal, Principal, localStorageService, $q, $timeout, EntitiesBrowser) {
        var delay = 1000;
        var servfields = ['lastModifiedBy', 'lastVersion', 'version', 'lastEditDate', 'creationDate', 'templateContent']
        var kinds = ['experiment', 'project', 'notebook'],
            save, get, clear, states = {};
        var deferred = $q.defer();
        Principal.identity()
            .then(function(user) {
                var subkey = user.id + '.autorecover.';
                var types = JSON.parse(localStorageService.get(subkey));
                if (!types) {
                    types = {}
                    kinds.forEach(function(kind) {
                        types[kind] = { recoveries: {} };
                    })
                } else {
                    //console.log('recoveries', types)
                }

                function getKey(kind, entity) {
                    var curtab = EntitiesBrowser.getActiveTab();
                    var id = entity.id  || curtab.tmpId;
                    return subkey + kind + '.entity.' + id;
                }
                clear = function(kind, entity) {
                    //console.log('clear entity', kind, entity)
                    var curtab = EntitiesBrowser.getActiveTab();
                    var id = entity.id || curtab.tmpId;
                    var type = types[kind];
                    var rec = type.recoveries[id];
                    if (!rec) return;
                    delete type.recoveries[id];
                    localStorageService.remove(getKey(kind, entity));
                    localStorageService.set(subkey, angular.toJson(types))
                    var curtab = EntitiesBrowser.getActiveTab();
                    delete curtab.tmpId;
                    EntitiesBrowser.saveTabs()
                }
                save = function(kind, entity) {
                    var curtab = EntitiesBrowser.getActiveTab();
                    var id = entity.id || curtab.tmpId;
                    var clone = angular.copy(entity);
                    servfields.forEach(function(field) {
                        delete clone[field];
                    })
                    var type = types[kind];
                    var rec = type.recoveries[id];
                    if (!rec) {
                        rec = type.recoveries[id] = { id: id, name: entity.name, kind: kind };
                    }
                    rec.date = +new Date();
        
                    delete rec.thisSession;
                    localStorageService.set(subkey, angular.toJson(types))
                    rec.thisSession = true;
                    localStorageService.set(getKey(kind, entity), angular.toJson(clone));
                    //console.warn('save rec', rec, id, getKey(kind, entity))
                };
                get = function(kind, entity) {
                    var curtab = EntitiesBrowser.getActiveTab();
                    var id = entity.id || curtab.tmpId;
                    var type = types[kind];
                    var rec = type.recoveries[id];
                    var curtab = EntitiesBrowser.getActiveTab();
                    if (!rec) return;
                    return { entity: JSON.parse(localStorageService.get(getKey(kind, entity))), rec: rec }
                }
                deferred.resolve()
            });
        function getFullId(entity) {
            if (!entity) return false;
            var curtab = EntitiesBrowser.getActiveTab()
            return entity.fullId || curtab.tmpId;
        }
        function canUndo(entity) {
            if (!entity) return false;
            var id = getFullId(entity);
            var state = states[id];
            console.warn(state && state.actions.length > 0 && state.aindex >= 0)
            return state && state.actions.length > 0 && state.aindex >= 0;
        }

        function canRedo(entity) {
            if (!entity) return false;
            var id = getFullId(entity);
            var state = states[id];
            return state && state.actions.length > 0 && state.aindex < state.actions.length - 1;
        }
        function undoAction(entity) {
            if (!entity) return false;
            var id = getFullId(entity);
            var state = states[id];
            if (state && state.aindex >= 0) {
                var act = state.actions[state.aindex--];
                angular.extend(entity, act);
                entity.$$undo = true;
            }
        }
        function redoAction(entity) {
            var id = getFullId(entity);
            if (!entity) return false;
            var state = states[id];
            if (states && state.aindex < state.actions.length - 1) {
                var act = (state.aindex < state.actions.length - 2) ? state.actions[state.aindex + 2] : state.last;
                state.aindex++;
                console.log('redo', state.aindex, state.actions)
                angular.extend(entity, act);
                entity.$$undo = true;
            }
        }
        return {
            canUndo:  canUndo,
            canRedo : canRedo,
            undoAction : undoAction,
            redoAction : redoAction,
            trackEntityChanges: function(entity, form, $scope, kind) {
                deferred.promise.then(function() {
                    var curtab = EntitiesBrowser.getActiveTab()
                    if (!entity.id && !curtab.tmpId) {
                        curtab.tmpId = +new Date();
                        EntitiesBrowser.saveTabs()
                    }
                    
                    var fullId = getFullId(entity);
                    console.warn(fullId)
                    var state = states[fullId] || { actions: [] };
                    states[fullId] = state;
                    var rec = get(kind, entity);
                   // console.warn( kind, curtab.tmpId, rec, curtab)
                    if (rec && !rec.rec.thisSession) {
                        $scope.restored = {
                            rec: rec,
                            resolve: function(val) {
                                if (val) {
                                    angular.extend(entity, rec.entity);
                                }
                                clear(kind, entity)
                                form.$setDirty(true);
                                $scope.restored = null;
                            }
                        }
                    } else if (rec && curtab.tmpId) {
                        angular.extend(entity, rec.entity);
                        form.$setDirty(true);
                    }
                    $scope.undoAction = function() {
                        undoAction(entity)
                    }
                    $scope.redoAction = function() {
                        redoAction(entity)
                    }
                    $scope.canUndo = function() {
                        return canUndo(entity);
                    }
                    $scope.canRedo = function() {
                        return canRedo(entity);
                    }
                    var ctimeout, prev;
                    var onChange = function(entity, old) {
                        if (ctimeout) {
                            $timeout.cancel(ctimeout);
                        } else {
                            prev = angular.extend({}, old);
                        }
                        ctimeout = $timeout(function() {
                            if (form.$dirty) {
                                save(kind, entity)
                                if (!entity.$$undo) {
                                    if (state.aindex < state.actions.length - 1) {
                                        state.actions = state.actions.slice(state.aindex)
                                    }
                                    state.actions.push(prev);
                                    //console.warn('onChange', state.actions)
                                    state.aindex = state.actions.length - 1;
                                    state.last = angular.extend({}, entity);
                                }
                            }
                            entity.$$undo = false;
                            ctimeout = null;
                        }, 1000)
                    };
                    var unbind = $scope.$watch(kind, onChange, true);
                    var unbindDirty = $scope.$watch(form.$name + '.$dirty', function(val, old) {
                        if (!val && old) {
                            clear(kind, entity)
                            $scope.restored = null;
                        }
                    }, true);
                    $scope.$on('$destroy', function() {
                        unbind();
                        unbindDirty();
                    });
                })
            },
            clearRecovery: function(kind, entity) {
                deferred.promise.then(function() {
                    clear(kind, entity)
                })
            }
        }
    });
