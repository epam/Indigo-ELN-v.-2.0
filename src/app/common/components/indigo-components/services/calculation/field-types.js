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

function fieldTypes() {

}

fieldTypes.id = 'id';
fieldTypes.compoundId = 'compoundId';
fieldTypes.chemicalName = 'chemicalName';
fieldTypes.fullNbkBatch = 'fullNbkBatch';
fieldTypes.fullNbkImmutablePart = 'fullNbkImmutablePart';
fieldTypes.molWeight = 'molWeight';
fieldTypes.weight = 'weight';
fieldTypes.theoWeight = 'theoWeight';
fieldTypes.theoMoles = 'theoMoles';
fieldTypes.totalWeight = 'totalWeight';
fieldTypes.totalMoles = 'totalMoles';
fieldTypes.volume = 'volume';
fieldTypes.totalVolume = 'totalVolume';
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

fieldTypes.isId = isId;
fieldTypes.isMolWeight = isMolWeight;
fieldTypes.isEq = isEq;
fieldTypes.isStoicPurity = isStoicPurity;
fieldTypes.isRxnRole = isRxnRole;
fieldTypes.isReagentField = isReagentField;
fieldTypes.isLimiting = isLimiting;
fieldTypes.isFormula = isFormula;

function isId(key) {
    return key === fieldTypes.id;
}

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

function isFormula(key) {
    return key === fieldTypes.formula;
}

module.exports = fieldTypes;
