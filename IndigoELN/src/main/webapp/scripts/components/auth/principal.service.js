angular.module('indigoeln')
    .factory('Principal', function ($q, Account) {
        var _identity, deferred,
            _authenticated = false;

        return {
            isIdentityResolved: function () {
                return angular.isDefined(_identity);
            },
            isAuthenticated: function () {
                return _authenticated;
            },
            hasAuthority: function (authority) {
                if (!_authenticated) {
                    return $q.when(false);
                }

                return this.identity().then(function (_id) {
                    return _id.authorities && _id.authorities.indexOf(authority) !== -1;
                }, function () {
                    return false;
                });
            },
            hasAnyAuthority: function (authorities) {
                return this.identity().then(function (_id) {
                    if (!_id.authorities) {
                        return false;
                    }
                    for (var i = 0; i < authorities.length; i++) {
                        if (_id.authorities.indexOf(authorities[i]) !== -1) {
                            return true;
                        }
                    }

                    return false;
                })
            },
            hasAuthorityIdentitySafe: function (authority) {
                return this.identity().then(function (_id) {
                    return _id.authorities && _id.authorities.indexOf(authority) !== -1;
                }, function () {
                    return false;
                });
            },
            authenticate: function (identity) {
                _identity = identity;
                _authenticated = identity !== null;
            },
            identity: function (force) {

                if (force === true) {
                    _identity = undefined;
                    deferred = null;
                }
                // check and see if we have retrieved the identity data from the server.
                // if we have, reuse it by immediately resolving

                if (!deferred) 
                    deferred = $q.defer();
                else {
                    return deferred.promise;
                }
                
                // retrieve the identity data from the server, update the identity object, and then resolve.
                Account.get().$promise
                    .then(function (account) {
                        _identity = account.data;
                        _authenticated = true;
                        deferred.resolve(_identity);
                    })
                    .catch(function () {
                        _identity = null;
                        _authenticated = false;
                        deferred.resolve(_identity);
                    });
                return deferred.promise;
            },
            getIdentity: function () {
                return _identity;
            }
        };
    });
