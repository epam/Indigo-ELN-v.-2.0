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

AlertModalController.$inject = ['$uibModalInstance', 'title', 'message', 'okText', 'noText', 'cancelVisible',
    'okCallback', 'noCallback', 'type'];

function AlertModalController($uibModalInstance, title, message, okText, noText, cancelVisible, okCallback,
                              noCallback, type) {
    var vm = this;

    var btnClasses = {
        ERROR: 'btn-danger',
        WARNING: 'btn-danger',
        NORMAL: 'btn-primary'
    };

    $onInit();

    function $onInit() {
        vm.cancelVisible = cancelVisible;
        vm.okText = okText || 'Ok';
        vm.noText = noText || 'No';
        vm.title = title;
        vm.message = message;
        vm.hasOkCallback = !!okCallback;
        vm.hasNoCallback = !!noCallback;

        vm.cancel = cancel;
        vm.ok = ok;
        vm.no = no;
        vm.getBtnClass = getBtnClass;
    }

    function getBtnClass() {
        return btnClasses[type] || 'btn-default';
    }
    function cancel() {
        $uibModalInstance.dismiss('cancel');
        if (noCallback) {
            noCallback();
        }
    }

    function ok() {
        $uibModalInstance.close();
        if (_.isFunction(okCallback)) {
            okCallback();
        }
    }

    function no() {
        $uibModalInstance.dismiss('cancel');
        if (_.isFunction(noCallback)) {
            noCallback();
        }
    }
}

module.exports = AlertModalController;
