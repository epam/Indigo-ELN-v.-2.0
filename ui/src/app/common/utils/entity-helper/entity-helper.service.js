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
function entityHelper(confirmationModal, notifyService, entityTreeService) {
    var isConflictConfirmOpen = false;

    return {
        onEntityUpdate: onEntityUpdate
    };

    /**
     * Handles concurrent update of the opened entity
     * @param { Object } currentEntity - entity in the opened tab
     * @param { String } currentEntity.projectId
     * @param { String } currentEntity.notebookId
     * @param { String } currentEntity.experimentId
     * @param { Number } currentEntity.version
     * @param { 'Project' | 'Notebook' | 'Experiment' } currentEntity.type
     *
     * @param { String } currentEntity.title
     * @param { Object } updatedEntity - entity updated externally
     * @param { String } updatedEntity.projectId
     * @param { String } updatedEntity.notebookId
     * @param { String } updatedEntity.experimentId
     * @param { Number } updatedEntity.version
     *
     * @param { Boolean } hasUnsavedChanges - weather current tab has unsaved changes or not
     * @param { Function } refreshCallback - function to call if user accepts external changes to the current entity
     * @param { Function } rejectCallback - function to call if user rejects external changes to the current entity
     */
    function onEntityUpdate(currentEntity, updatedEntity, hasUnsavedChanges, refreshCallback, rejectCallback) {
        var isCurrentChanged = isEqualEntities(currentEntity, updatedEntity);

        if (isCurrentChanged && updatedEntity.version <= currentEntity.version) {
            return;
        }

        var updatedParentEntity = isCurrentChanged
            ? null
            : getUpdatedParentEntity(currentEntity, updatedEntity);

        // If nothing changed in current entity or its parents do nothing
        if (!updatedParentEntity && !isCurrentChanged) {
            return;
        }

        // Has local changes which could be saved before update
        if (hasUnsavedChanges) {
            displayConflictModal(currentEntity.title, refreshCallback, rejectCallback);

            return;
        }

        refreshCallback().then(function() {
            displayUpdateNotification(currentEntity, updatedParentEntity);
        });
    }

    function displayConflictModal(entityTitle, refreshCallback, rejectCallback) {
        if (isConflictConfirmOpen) {
            return;
        }

        isConflictConfirmOpen = true;

        confirmationModal
            .openEntityVersionsConflictConfirm(entityTitle)
            .then(refreshCallback, rejectCallback)
            .finally(function() {
                isConflictConfirmOpen = false;
            });
    }

    function displayUpdateNotification(currentEntity, updatedParentEntity) {
        var message = currentEntity.title + ' has been changed by another user and had been reloaded';
        var currentEntityType = currentEntity.experimentId ? 'experiment' : 'notebook';

        if (updatedParentEntity) {
            message = updatedParentEntity.type + ' ' + updatedParentEntity.title +
                ' has been changed by another user and current ' + currentEntityType + ' ' +
                currentEntity.title + ' had been reloaded';
        }

        notifyService.info(message);
    }

    function getUpdatedParentEntity(currentEntity, updatedEntity) {
        var parentEntity = {
            projectId: currentEntity.projectId,
            type: 'Project'
        };
        var isNotebook = updatedEntity.notebookId;
        var entityObject;

        if (isNotebook) {
            var notebookFullId = currentEntity.projectId + '-' + currentEntity.notebookId;
            entityObject = entityTreeService.getNotebookByFullId(notebookFullId);
            parentEntity.notebookId = currentEntity.notebookId;
            parentEntity.type = 'Notebook';
        } else {
            entityObject = entityTreeService.getProjectById(currentEntity.projectId);
        }

        parentEntity.title = _.get(entityObject, 'name', null);

        if (isEqualEntities(updatedEntity, parentEntity)) {
            return parentEntity;
        }

        return null;
    }

    /**
     * Checks if entities are equal comparing their Ids.
     * Notebook or experiment Ids may be undefined for projects and notebooks.
     * @param entity1
     * @param entity2
     */
    function isEqualEntities(entity1, entity2) {
        return entity1.projectId === entity2.projectId &&
            entity1.notebookId === entity2.notebookId &&
            entity1.experimentId === entity2.experimentId;
    }
}

module.exports = entityHelper;
