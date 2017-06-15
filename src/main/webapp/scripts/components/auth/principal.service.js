angular
    .module('indigoeln')
    .factory('Principal', principal);

/* @ngInject */
function principal($q, Account) {
    var _identity, deferred,
        _authenticated = false;

    return {
        isIdentityResolved: isIdentityResolved,
        isAuthenticated: isAuthenticated,
        hasAuthority: hasAuthority,
        hasAnyAuthority: hasAnyAuthority,
        hasAuthorityIdentitySafe: hasAuthorityIdentitySafe,
        authenticate: authenticate,
        identity: identity,
        getIdentity: getIdentity
    };


    function isIdentityResolved() {
        return angular.isDefined(_identity);
    }

    function isAuthenticated() {
        return _authenticated;
    }

    function hasAuthority(authority) {
        if (!_authenticated) {
            return $q.when(false);
        }

        return this.identity().then(function (_id) {
            return _id.authorities && _id.authorities.indexOf(authority) !== -1;
        }, function () {
            return false;
        });
    }

    function hasAnyAuthority(authorities) {
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
        });
    }

    function hasAuthorityIdentitySafe(authority) {
        return this.identity().then(function (_id) {
            return _id.authorities && _id.authorities.indexOf(authority) !== -1;
        }, function () {
            return false;
        });
    }

    function authenticate(identity) {
        _identity = identity;
        _authenticated = identity !== null;
    }

    function identity(force) {
        if (force === true) {
            _identity = undefined;
            deferred = null;
        }
        // check and see if we have retrieved the identity data from the server.
        // if we have, reuse it by immediately resolving

        if (!deferred){
            deferred = $q.defer();
        } else {
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
    }

    function getIdentity() {
        return _identity;
    }
}


