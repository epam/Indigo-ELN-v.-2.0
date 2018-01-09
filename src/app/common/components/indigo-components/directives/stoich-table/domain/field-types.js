function fieldTypes() {

}

fieldTypes.compoundId = 'compoundId';
fieldTypes.chemicalName = 'chemicalName';
fieldTypes.fullNbkBatch = 'fullNbkBatch';
fieldTypes.fullNbkImmutablePart = 'fullNbkImmutablePart';
fieldTypes.molWeight = 'molWeight';
fieldTypes.weight = 'weight';
fieldTypes.theoWeight = 'theoWeight';
fieldTypes.theoMoles = 'theoMoles';
fieldTypes.volume = 'volume';
fieldTypes.mol = 'mol';
fieldTypes.eq = 'eq';
fieldTypes.limiting = 'limiting';
fieldTypes.rxnRole = 'rxnRole';
fieldTypes.prevRxnRole = 'prevRxnRole';
fieldTypes.density = 'density';
fieldTypes.molarity = 'molarity';
fieldTypes.stoicPurity = 'stoicPurity';
fieldTypes.formula = 'formula';
fieldTypes.saltCode = 'saltCode';
fieldTypes.saltEq = 'saltEq';
fieldTypes.loadFactor = 'loadFactor';
fieldTypes.hazardComments = 'hazardComments';
fieldTypes.comments = 'comments';
fieldTypes.structure = 'structure';
fieldTypes.structureComments = 'structureComments';

fieldTypes.isMolWeight = isMolWeight;
fieldTypes.isEq = isEq;
fieldTypes.isStoicPurity = isStoicPurity;
fieldTypes.isRxnRole = isRxnRole;
fieldTypes.isReagentField = isReagentField;
fieldTypes.isLimiting = isLimiting;


function isMolWeight(key) {
    return key === fieldTypes.molWeight;
}

function isEq(key) {
    return key === fieldTypes.eq;
}

function isStoicPurity(key) {
    return key === fieldTypes.stoicPurity;
}

function isRxnRole(key) {
    return key === fieldTypes.rxnRole;
}

function isReagentField(key) {
    return key === fieldTypes.weight || key === fieldTypes.volume
        || key === fieldTypes.mol || key === fieldTypes.density
        || key === fieldTypes.molarity;
}

function isLimiting(key) {
    return key === fieldTypes.limiting;
}

module.exports = fieldTypes;
