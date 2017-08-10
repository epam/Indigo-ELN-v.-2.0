angular
    .module('indigoeln')
    .factory('componentsUtils', componentsUtilsFactory);

/* @ngInject */
function componentsUtilsFactory(Principal, Components) {
    return {
        initComponents: initComponents
    };

    function initComponents(components, templates) {
        initExperimentDescription(components, isExistInTemplate(templates, 'experimentDescription'));
        initConceptDetails(components, isExistInTemplate(templates, 'conceptDetails'));
        initPreferredCompoundSummary(components, isExistInTemplate(templates, 'preferredCompoundSummary'));
        initProductBatchSummary(components, isExistInTemplate(templates, 'productBatchSummary'));
        initStoichTable(components, isExistInTemplate(templates, 'stoichTable'));
        initReactionDetails(components, isExistInTemplate(templates, 'reactionDetails'));
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
            _.defaultsDeep(components.experimentDescription, {description: ''});
        }
    }

    function initConceptDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(
                components, {
                    conceptDetails: {experimentCreator: {name: Principal.getIdentity().fullName}}
                }
            );
        }
    }

    function initPreferredCompoundSummary(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {preferredCompoundSummary: {compounds: []}});
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
                    reactionDetails: {batchOwner: [], experimentCreator: {name: Principal.getIdentity().fullName}}
                });
        }
    }
}
