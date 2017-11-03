angular
    .module('indigoeln')
    .factory('usersService', usersService);

usersService.$inject = ['dictionaryService'];

function usersService(dictionaryService) {
    var usersPromise;
    var allUsers;

    return {
        get: function(force) {
            if (!usersPromise || force) {
                usersPromise = dictionaryService.get({
                    id: 'users'
                }).$promise.then(function(dictionary) {
                    allUsers = _.keyBy(dictionary.words, 'id');

                    return dictionary;
                });
            }

            return usersPromise;
        },
        getUsersById: getUsersById
    };

    function getUsersById(ids) {
        return _.map(ids, function(id) {
            return allUsers[id];
        });
    }
}
