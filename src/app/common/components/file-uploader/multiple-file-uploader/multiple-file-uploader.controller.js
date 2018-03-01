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

MultipleFileUploaderController.$inject = ['$uibModalInstance', '$cookies', 'notifyService', 'fileUploader',
    'FileUploader', 'params', 'uploadUrl'];

function MultipleFileUploaderController($uibModalInstance, $cookies, notifyService, fileUploader, FileUploader,
                                        params, uploadUrl) {
    var vm = this;
    var formData = [];
    var uploader;
    var paramsForUpload;

    vm.files = [];

    vm.remove = remove;
    vm.cancel = cancel;

    init();

    function init() {
        paramsForUpload = {
            projectId: params.projectId,
            experimentId: params.projectId + '-' + params.notebookId + '-' + params.experimentId
        };

        formData.push(paramsForUpload);

        uploader = new FileUploader({
            url: uploadUrl,
            alias: 'file',
            withCredentials: true,
            headers: {
                'X-CSRF-TOKEN': $cookies.get('CSRF-TOKEN')
            },
            formData: formData
        });

        vm.uploader = uploader;

        uploader.filters.push({
            name: 'customFilter',
            fn: function() {
                return this.queue.length < 10;
            }
        });
        // CALLBACKS
        uploader.onSuccessItem = function(fileItem, response) {
            vm.files.push(response);
            notifyService.success('Attachments are saved successfully.');
        };
        uploader.onErrorItem = function() {
            notifyService.error('Uploaded file size should be less than 10 Mb');
        };
    }

    function remove(index) {
        vm.files.splice(index, 1);
    }

    function cancel() {
        fileUploader.addFiles(vm.files);
        $uibModalInstance.close(vm.files);
    }
}

module.exports = MultipleFileUploaderController;
