angular
    .module('indigoeln')
    .factory('SearchPanel', function ($q, $resource, PermissionManagement) {

        return {
            get: function(){
                return $q.when({name:"Tab", fullId:"1", isExternal:true});
            }

        };
    });
