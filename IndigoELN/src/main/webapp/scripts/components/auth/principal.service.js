angular
    .module('indigoeln')
    .factory('Principal', principal);

/* @ngInject */
function principal($q, Account) {
    var _identity;
    var identityPromise;
    var _authenticated = false;

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
        return !_.isUndefined(_identity);
    }

    function isAuthenticated() {
        return _authenticated;
    }

    function hasAuthority(authority) {
        if (!_authenticated) {
            return $q.when(false);
        }

        return this.identity().then(function(_id) {
            return _id.authorities && _id.authorities.indexOf(authority) !== -1;
        }, function() {
            return false;
        });
    }

    function hasAnyAuthority(authorities) {
        return this.identity().then(function(_id) {
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
        return this.identity().then(function(_id) {
            return _id.authorities && _id.authorities.indexOf(authority) !== -1;
        }, function() {
            return false;
        });
    }

    function authenticate(userIdentity) {
        _identity = userIdentity;
        _authenticated = userIdentity !== null;
    }

    function identity(force) {
        if (force === true) {
            _identity = undefined;
            identityPromise = null;
        }
        // check and see if we have retrieved the identity data from the server.
        // if we have, reuse it by immediately resolving

        if (identityPromise) {
            return identityPromise;
        }

        // retrieve the identity data from the server, update the identity object, and then resolve.
        identityPromise = Account.get().$promise
            .then(function(account) {
                _identity = account.data;
                _authenticated = true;

                return _identity;
            })
            .catch(function() {
                _identity = null;
                _authenticated = false;

                return _identity;
            });

        return identityPromise;
    }

    function getIdentity() {
        return _identity;
    }
}

