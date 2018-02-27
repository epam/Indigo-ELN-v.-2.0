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

var template = require('./autorecovery.html');

function autorecovery() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            kind: '@',
            name: '@',
            onRestore: '&'
        },
        controller: AutoRecoveryController,
        bindToController: true,
        controllerAs: 'vm'
    };

    /* @ngInject */
    function AutoRecoveryController($stateParams, autorecoveryCache) {
        var vm = this;

        var recoveryData;
        var tempRecoveryData;

        init();

        function init() {
            recoveryData = autorecoveryCache.get($stateParams);
            tempRecoveryData = autorecoveryCache.getTempRecoveryData($stateParams);

            if (recoveryData && !tempRecoveryData) {
                autorecoveryCache.tryToVisible($stateParams);
                autorecoveryCache.putTempRecoveryData(recoveryData);
            }

            if (!recoveryData && !autorecoveryCache.isVisible($stateParams)) {
                autorecoveryCache.hide($stateParams);
            }

            vm.isVisible = (tempRecoveryData || recoveryData) && autorecoveryCache.isVisible($stateParams);

            vm.restore = restore;
            vm.remove = remove;
        }

        function restore() {
            vm.onRestore({recoveryData: tempRecoveryData || recoveryData});
            remove();
        }

        function remove() {
            autorecoveryCache.hide($stateParams);
            autorecoveryCache.remove($stateParams);
            autorecoveryCache.removeTempRecoveryData($stateParams);
            recoveryData = null;
            tempRecoveryData = null;
            vm.isVisible = false;
        }
    }
}

module.exports = autorecovery;
