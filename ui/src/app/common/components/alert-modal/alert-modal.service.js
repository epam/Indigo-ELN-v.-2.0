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

var template = require('./alert-modal.html');
var types = require('./types.json');

alertModal.$inject = ['$uibModal'];

function alertModal($uibModal) {
    return {
        alert: alert,
        error: error,
        warning: warning,
        info: info,
        confirm: confirm,
        save: save
    };

    function alert(options) {
        return openAlertModal(options);
    }

    function error(message, size) {
        return openAlertModal({
            title: 'Error',
            message: message,
            size: size
        });
    }

    function warning(message, size) {
        return openAlertModal({
            title: 'Warning',
            message: message,
            size: size
        });
    }

    function info(message, size, okCallback) {
        return openAlertModal({
            title: 'Info',
            message: message,
            size: size,
            okCallback: okCallback,
            noCallback: null
        });
    }

    function confirm(message, size, okCallback) {
        return openAlertModal({
            title: 'Confirm',
            message: message,
            size: size,
            okCallback: okCallback,
            noCallback: null
        });
    }

    function save(message, size, okCallback) {
        return openAlertModal({
            title: 'Save',
            message: message,
            size: size,
            okCallback: okCallback.bind(null, true),
            noCallback: okCallback.bind(null, false),
            okText: 'Yes'
        });
    }

    function openAlertModal(options) {
        return $uibModal.open({
            size: options.size || 'md',
            resolve: {
                type: function() {
                    return options.type || types.NORMAL;
                },
                title: function() {
                    return options.title;
                },
                message: function() {
                    return options.message;
                },
                okText: function() {
                    return options.okText;
                },
                noText: function() {
                    return options.noText;
                },
                cancelVisible: function() {
                    return _.isBoolean(options.hideCancel)
                        ? !options.hideCancel
                        : options.okCallback || options.noCallback;
                },
                okCallback: function() {
                    return options.okCallback;
                },
                noCallback: function() {
                    return options.noCallback;
                }
            },
            template: template,
            controller: 'AlertModalController',
            bindToController: true,
            controllerAs: 'vm'
        }).result;
    }
}

module.exports = alertModal;
