angular
    .module('indigoeln')
    .factory('componentsUtils', componentsUtilsFactory);

/* @ngInject */
function componentsUtilsFactory(Principal, Components) {
    return {
        initComponents: initComponents,
        getUsersById: getUsersById
    };

    function initComponents(components, templates) {
        initExperimentDescription(components, isExistInTemplate(templates, Components.experimentDescription.field));
        initConceptDetails(components, isExistInTemplate(templates, Components.conceptDetails.field));
        initPreferredCompoundSummary(components, isExistInTemplate(templates, Components.preferredCompoundSummary.field));
        initPreferredCompoundDetails(components, isExistInTemplate(templates, Components.preferredCompoundDetails.field));
        initProductBatchDetails(components, isExistInTemplate(templates, Components.productBatchDetails.field));
        initProductBatchSummary(components, isExistInTemplate(templates, Components.productBatchSummary.field));
        initStoichTable(components, isExistInTemplate(templates, Components.stoichTable.field));
        initReactionDetails(components, isExistInTemplate(templates, Components.reactionDetails.field));
        initReaction(components, isExistInTemplate(templates, Components.reaction.field));
    }

    function isExistInTemplate(templateContent, componentName) {
        return _.some(templateContent, function(content) {
            return _.find(content.components, function(component) {
                return Components[componentName].id === component.id;
            });
        });
    }

    function initExperimentDescription(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {
                experimentDescription: {description: ''}
            });
        }
    }

    function initConceptDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(
                components, {
                    conceptDetails: {experimentCreator: Principal.getIdentity().id}
                }
            );
        }
    }

    function initPreferredCompoundSummary(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {preferredCompoundSummary: {compounds: []}});
        }
    }

    function initPreferredCompoundDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {preferredCompoundDetails: null});
        }
    }

    function initProductBatchDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {productBatchDetails: null});
        }
    }

    function initProductBatchSummary(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {productBatchSummary: {batches: []}});
        }
    }

    function initStoichTable(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {stoichTable: {products: [], reactants: []}});
        }
    }

    function initReactionDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(
                components, {
                    reactionDetails: {
                        batchOwner: [],
                        coAuthors: [],
                        experimentCreator: Principal.getIdentity().id
                    }
                });
        }
    }

    function initReaction(components, isExist) {
        if (isExist) {
            _.defaultsDeep(
                components, {
                    reaction: {
                        molfile: null,
                        image: null
                    }
                });
        }
    }

    function getUsersById(ids, AllUsers) {
        var users = [];
        var tempIds = _.keyBy(ids);

        _.every(AllUsers, function(user) {
            if (tempIds[user.id]) {
                users.push(user);
            }
            return users.length !== ids.length;
        });

        return users;
    }
}
