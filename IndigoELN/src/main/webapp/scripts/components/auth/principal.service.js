angular
    .module('indigoeln')
    .factory('Principal', principal);

/* @ngInject */
function principal($q, Account) {
    var _identity;
    var identityPromise;
    var _authenticated = false;
    var userChangeListenersIds = 0;
    var userChangeListeners = {};

    return {
        isIdentityResolved: isIdentityResolved,
        isAuthenticated: isAuthenticated,
        hasAuthority: hasAuthority,
        hasAnyAuthority: hasAnyAuthority,
        hasAuthorityIdentitySafe: hasAuthorityIdentitySafe,
        authenticate: authenticate,
        identity: identity,
        getIdentity: getIdentity,
        addUserChangeListener: addUserChangeListener
    };

    function addUserChangeListener(clbk) {
        var id = userChangeListenersIds++;
        userChangeListeners[id] = clbk;

        return function() {
            userChangeListeners[id] = undefined;
        };
    }

    function callUserChangeListeners(user) {
        var id = user ? user.id : user;
        _.forEach(userChangeListeners, function(clbk) {
            clbk(id);
        });
    }

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

        return _identity;
    }

    function identity(isNeedUpdate) {
        if (isNeedUpdate) {
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
                return authenticate(account.data);
            }, function() {
                return authenticate(null);
            })
            .finally(callUserChangeListeners);

        return identityPromise;
    }

    function getIdentity() {
        return _identity;
    }
}

