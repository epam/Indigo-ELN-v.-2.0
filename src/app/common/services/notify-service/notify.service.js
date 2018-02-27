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

var template = require('./notify-service.html');

/* @ngInject */
function notifyService(notify, $document) {
    /* eslint angular/module-getter: 0*/
    notify.config({
        template: template,
        startTop: 0
    });

    return {
        success: success,
        error: error,
        warning: warning,
        info: info
    };

    function customNotify(msg, type) {
        return notify({
            container: $document[0].getElementById('content-wrapper'),
            message: msg,
            classes: type
        });
    }

    function success(msg) {
        customNotify(msg, 'success-notify');
    }

    function error(msg) {
        customNotify(msg, 'danger-notify');
    }

    function warning(msg) {
        customNotify(msg, 'warning-notify');
    }

    function info(msg) {
        customNotify(msg, 'info-notify');
    }
}

module.exports = notifyService;
