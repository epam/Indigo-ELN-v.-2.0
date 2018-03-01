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

var types = require('../common/components/alert-modal/types.json');

/* @ngInject */
function roleManagementUtils(alertModal, i18en) {
    return {
        openRoleManagementDeleteDialog: openRoleManagementDeleteDialog
    };

    function openRoleManagementDeleteDialog() {
        // (title, message, size, okCallback, noCallback, okText, hideCancel, noText, type) {
        return alertModal.alert({
            title: i18en.CONFIRM_DELETE_OPERATION,
            message: i18en.ARE_YOU_SURE_YOU_WANT_TO_DELETE_THIS_ROLE,
            size: 'sm',
            okCallback: true,
            noCallback: null,
            okText: i18en.DELETE,
            hideCancel: false,
            type: types.WARNING
        });
    }
}

module.exports = roleManagementUtils;
