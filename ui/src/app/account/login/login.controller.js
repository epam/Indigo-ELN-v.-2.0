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
function LoginController($state, authService) {
    var vm = this;

    vm.user = {};
    vm.errors = {};
    vm.rememberMe = true;

    vm.login = login;

    function login(event) {
        vm.loading = true;
        event.preventDefault();
        vm.authenticationError = false;
        vm.serverError = false;
        authService.login({
            username: vm.username,
            password: vm.password,
            rememberMe: vm.rememberMe
        }).then(function() {
            vm.authenticationError = false;
            $state.go('experiment');
            vm.loading = false;
        }).catch(function(e) {
            if (e.status === 401) {
                vm.authenticationError = true;
            } else {
                vm.serverError = true;
            }
            vm.shake = !vm.shake;
            vm.loading = false;
        });
    }
}

module.exports = LoginController;
