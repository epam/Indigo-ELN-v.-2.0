/* @ngInject */
function principalService(accountService) {
    var identity;
    var identityPromise;
    var authenticated = false;
    var userChangeListenersIds = 0;
    var userChangeListeners = {};

    return {
        isIdentityResolved: isIdentityResolved,
        isAuthenticated: isAuthenticated,
        hasAuthority: hasAuthority,
        hasAnyAuthority: hasAnyAuthority,
        hasAuthorityIdentitySafe: hasAuthorityIdentitySafe,
        authenticate: authenticate,
        checkIdentity: checkIdentity,
        getIdentity: getIdentity,
        addUserChangeListener: addUserChangeListener,
        getUserId: getUserId
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
        return !_.isUndefined(identity);
    }

    function isAuthenticated() {
        return authenticated;
    }

    function hasAuthority(authority) {
        if (!identity.authorities) {
            return false;
        }

        return identity.authorities.indexOf(authority) !== -1;
    }

    function hasAnyAuthority(authorities) {
        if (!identity.authorities) {
            return false;
        }

        for (var i = 0; i < authorities.length; i++) {
            if (identity.authorities.indexOf(authorities[i]) !== -1) {
                return true;
            }
        }

        return false;
    }

    function hasAuthorityIdentitySafe(authority) {
        return checkIdentity()
            .then(
                function(_id) {
                    return _id.authorities && _id.authorities.indexOf(authority) !== -1;
                },
                function() {
                    return false;
                });
    }

    function authenticate(userIdentity) {
        identity = userIdentity;
        authenticated = userIdentity !== null;

        return identity;
    }

    function checkIdentity(isNeedUpdate) {
        if (isNeedUpdate) {
            identity = undefined;
            identityPromise = null;
        }
        // check and see if we have retrieved the identity data from the server.
        // if we have, reuse it by immediately resolving

        if (identityPromise) {
            return identityPromise;
        }

        // retrieve the identity data from the server, update the identity object, and then resolve.
        identityPromise = accountService.get().$promise
            .then(function(account) {
                return authenticate(account.data);
            }, function() {
                return authenticate(null);
            })
            .finally(callUserChangeListeners);

        return identityPromise;
    }

    function getIdentity() {
        return identity;
    }

    function getUserId() {
        return identity && identity.id;
    }
}

module.exports = principalService;
