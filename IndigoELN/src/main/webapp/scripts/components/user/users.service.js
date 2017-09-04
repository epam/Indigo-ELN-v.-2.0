angular
    .module('indigoeln')
    .factory('Users', function($q, Dictionary) {
        var deferred;
        var allUsers;

        return {
            get: function(force) {
                if (!deferred || force) {
                    deferred = Dictionary.get({
                        id: 'users'
                    }).$promise.then(function(dictionary) {
                        allUsers = _.keyBy(dictionary.words, 'id');
                        return dictionary;
                    });
                }
                return deferred;
            },
            getUsersById: getUsersById
        };

        function getUsersById(ids) {
            return _.map(ids, function(id) {
                return allUsers[id];
            });
        }
    });
