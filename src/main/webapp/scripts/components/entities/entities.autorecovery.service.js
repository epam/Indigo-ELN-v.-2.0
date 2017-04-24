angular.module('indigoeln')
    .factory('AutoRecoverEngine', function(AlertModal, Principal, localStorageService, $q) {
        var delay = 500;
        var servfields = ['lastModifiedBy', 'lastVersion', 'version', 'lastEditDate', 'creationDate', 'templateContent']
        var kinds = ['experiment'],
            save, get, clear;
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
                    console.log('recoveries', types)
                }

                function getKey(kind, entity) {
                    return subkey + kind + '.entity.' + entity.id;
                }
                clear = function(kind, entity) {
                    console.log('clear entity', kind, entity)
                    var type = types[kind];
                    var rec = type.recoveries[entity.id];
                    if (!rec) return;
                    delete type.recoveries[entity.id];
                    localStorageService.remove(getKey(kind, entity));
                    localStorageService.set(subkey, angular.toJson(types))

                }
                save = function(kind, entity) {
                    var clone = angular.copy(entity);
                    servfields.forEach(function(field) {
                        delete clone[field];
                    })
                    var type = types[kind];
                    var rec = type.recoveries[entity.id];
                    if (!rec) {
                        rec = type.recoveries[entity.id] = { id: entity.id, name: entity.name };
                    }
                    rec.date = +new Date();
                    localStorageService.set(subkey, angular.toJson(types))
                    localStorageService.set(getKey(kind, entity), angular.toJson(clone));
                };
                get = function(kind, entity) {
                    var type = types[kind];
                    var rec = type.recoveries[entity.id];
                    if (!rec) return;
                    return { entity: JSON.parse(localStorageService.get(getKey(kind, entity))), rec: rec }
                }
                deferred.resolve()
            });
        return {
            trackEntityChanges: function(entity, form, $scope, kind) {
                deferred.promise.then(function() {
                    console.log('trackEntityChanges', entity, kind)
                    var rec = get(kind, entity);
                    if (rec) {
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
                        console.log('restore', rec.rec, rec.entity)
                    }
                    var onChange = _.debounce(function(entity, old) {
                        if (form.$dirty) {
                            console.log('change', form.$dirty, entity);
                            save(kind, entity)
                        } else {}
                    }, delay);
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
            clearRecovery : function(kind, entity) {
            	deferred.promise.then(function() {
            		clear(kind, entity)
            	})
            }
        }
    });
