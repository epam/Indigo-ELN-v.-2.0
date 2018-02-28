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

var multipleFileUploaderTemplate = require('../multiple-file-uploader/multiple-file-uploader.html');
var deleteDialogTemplate = require('../delete-dialog/delete-dialog.html');

/* @ngInject */
function FileUploaderController($uibModal, $filter, $stateParams, fileUploader,
                                parseLinks, notifyService, projectFileUploaderService,
                                experimentFileUploaderService, $timeout, apiUrl, $scope) {
    var vm = this;
    var params = $stateParams;
    var UploaderService = params.experimentId ? experimentFileUploaderService : projectFileUploaderService;
    var dlg;

    init();

    function init() {
        vm.apiUrl = apiUrl;
        vm.pagination = {
            page: 1,
            pageSize: 10
        };

        vm.loadAll = loadAll;
        vm.upload = upload;
        vm.deleteFile = deleteFile;
        vm.search = _.debounce(search, 300);
        vm.onPageChanged = onPageChanged;

        if (params.projectId) {
            vm.loadAll();
        }

        bindEvents();
    }

    function loadAll() {
        UploaderService.query({
            projectId: params.projectId,
            notebookId: params.notebookId,
            experimentId: params.experimentId
        }, function(result, headers) {
            vm.links = parseLinks.parse(headers('link'));
            vm.files = result;
            updateRowsForDisplay(vm.files);
        });
    }

    function upload() {
        if (!params.projectId) {
            notifyService.error('Please save project before attach files.');

            return;
        }

        closeDialog();
        dlg = $uibModal.open({
            animation: true,
            size: 'lg',
            template: multipleFileUploaderTemplate,
            controller: 'MultipleFileUploaderController',
            controllerAs: 'vm',
            resolve: {
                params: function() {
                    return params;
                },
                uploadUrl: function() {
                    return vm.uploadUrl;
                }
            }
        });
        dlg.result.then(function(result) {
            vm.files = _.union(result, vm.files);
            updateRowsForDisplay(vm.files);
            if (vm.files.length) {
                vm.onChanged();
            }
        });
    }

    function deleteFile(file) {
        closeDialog();
        dlg = $uibModal.open({
            animation: true,
            template: deleteDialogTemplate,
            controller: 'FileUploaderDeleteDialogController',
            controllerAs: 'vm',
            resolve: {
                params: function() {
                    return params;
                },
                file: function() {
                    return file;
                },
                UploaderService: function() {
                    return UploaderService;
                }
            }
        });
        dlg.result.then(function(fileToDelete) {
            vm.files = _.without(vm.files, fileToDelete);
            updateRowsForDisplay(vm.files);
            fileUploader.removeFile(fileToDelete);
            notifyService.success('File was successfully deleted');
        });
    }

    function search() {
        vm.filteredFiles = $filter('filter')(vm.files, vm.searchText);
    }

    function onPageChanged() {
        updateRowsForDisplay(vm.rowsForDisplay);
    }

    function getSkipItems() {
        return (vm.pagination.page - 1) * vm.pagination.pageSize;
    }

    function updateRowsForDisplay(rows) {
        if (!rows || rows.length === 0) {
            vm.limit = 0;
            vm.rowsForDisplay = null;

            return;
        }
        var skip = getSkipItems(rows);
        if (skip >= rows.length) {
            updateCurrentPage(rows);
            skip = getSkipItems();
        }
        $timeout(function() {
            vm.limit = skip + vm.pagination.pageSize;
            vm.rowsForDisplay = rows;
        });
    }

    function updateCurrentPage(rows) {
        vm.pagination.page = _.ceil(rows.length / vm.pagination.pageSize);
    }

    function bindEvents() {
        $scope.$on('$destroy', function() {
            closeDialog();
        });
    }

    function closeDialog() {
        if (dlg) {
            dlg.dismiss();
            dlg = null;
        }
    }
}

module.exports = FileUploaderController;
