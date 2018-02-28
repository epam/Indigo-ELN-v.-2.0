/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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

        callUserChangeListeners(identity);

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
                callUserChangeListeners(account.data);

                return authenticate(account.data);
            }, function() {
                callUserChangeListeners(null);

                return authenticate(null);
            });

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
