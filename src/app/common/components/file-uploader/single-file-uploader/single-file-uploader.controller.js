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

SingleFileUploaderController.$inject = ['$uibModalInstance', '$cookies', 'FileUploader', 'url'];

function SingleFileUploaderController($uibModalInstance, $cookies, FileUploader, url) {
    var vm = this;
    var uploader;

    vm.cancel = cancel;

    init();

    function init() {
        uploader = new FileUploader({
            url: url,
            alias: 'file',
            withCredentials: true,
            headers: {
                'X-CSRF-TOKEN': $cookies.get('CSRF-TOKEN')
            }
        });

        vm.uploader = uploader;

        uploader.filters.push({
            name: 'customFilter',
            fn: function() {
                return this.queue.length < 1;
            }
        });

        uploader.onAfterAddingFile = function(fileItem) {
            fileItem.upload();
        };
        uploader.onErrorItem = function() {
            $uibModalInstance.dismiss();
        };
        uploader.onSuccessItem = function(fileItem, response) {
            $uibModalInstance.close(response);
        };
    }

    function cancel() {
        if (uploader.queue.length === 1 && !uploader.queue[0].isUploading) {
            uploader.queue[0].cancel();
        }
        $uibModalInstance.close();
    }
}

module.exports = SingleFileUploaderController;
