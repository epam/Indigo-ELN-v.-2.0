/* @ngInject */
function entityHelper(confirmationModal, notifyService, entityTreeService) {
    var isConflictConfirmOpen = false;

    return {
        onEntityUpdate: onEntityUpdate
    };

    function onEntityUpdate(currentEntity, updatedEntity, hasUnsavedChanges, refreshCallback, rejectCallback) {
        var hasChangeInTheEntity = isEqual(currentEntity, updatedEntity);

        var updatedParentEntity = hasChangeInTheEntity
            ? null
            : getUpdatedParentEntity(currentEntity, updatedEntity);

        // If nothing changed in current entity or its parents do nothing
        if ((!updatedParentEntity && !hasChangeInTheEntity) || updatedEntity.version <= currentEntity.version) {
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
            entityObject = entityTreeService.getNotebookById(currentEntity.projectId, currentEntity.notebookId);
            parentEntity.notebookId = currentEntity.notebookId;
            parentEntity.type = 'Notebook';
        } else {
            entityObject = entityTreeService.getProjectById(currentEntity.projectId);
        }

        parentEntity.title = _.get(entityObject, 'name', null);

        if (isEqual(updatedEntity, parentEntity)) {
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
    function isEqual(entity1, entity2) {
        return entity1.projectId === entity2.projectId &&
            entity1.notebookId === entity2.notebookId &&
            entity1.experimentId === entity2.experimentId;
    }
}

module.exports = entityHelper;
