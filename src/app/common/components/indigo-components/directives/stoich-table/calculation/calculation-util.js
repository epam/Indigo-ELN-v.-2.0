var math = require('mathjs');

var ONE_HUNDRED = 100;
var ONE_THOUSAND = 1000;

function calculationUtil() {
}

calculationUtil.computePureMol = computePureMol;
calculationUtil.computeDissolvedMol = computeDissolvedMol;
calculationUtil.computeMolByPurity = computeMolByPurity;
calculationUtil.computeMolByEq = computeMolByEq;
calculationUtil.computeNonLimitingMolByEq = computeNonLimitingMolByEq;
calculationUtil.computeWeight = computeWeight;
calculationUtil.computeWeightByEq = computeWeightByEq;
calculationUtil.computeWeightByPurity = computeWeightByPurity;
calculationUtil.computeEq = computeEq;
calculationUtil.computeMolWeight = computeMolWeight;
calculationUtil.computeMolWeightBySalt = computeMolWeightBySalt;
calculationUtil.computeVolumeByMolarity = computeVolumeByMolarity;
calculationUtil.computeVolumeByDensity = computeVolumeByDensity;
calculationUtil.computeWeightByDensity = computeWeightByDensity;
// calculationUtil.computeYield = computeYield;
calculationUtil.multiply = multiply;
calculationUtil.divide = divide;

/**
 * Compute pure mol: Mol = Weight / Mol weight
 * @param weight
 * @param molWeight
 * @returns {number}
 */
function computePureMol(weight, molWeight) {
    return divide(weight, molWeight);
}

/**
 * Compute dissolved mol: Mol = Molarity * Volume
 * @param molarity
 * @param volume
 * @returns {number}
 */
function computeDissolvedMol(molarity, volume) {
    return multiply(molarity, volume);
}

/**
 * Compute mol by purity: Mol = (Purity / 100) * current Mol
 * @param purity
 * @param currentMol
 * @returns {number}
 */
function computeMolByPurity(purity, currentMol) {
    return math
        .chain(bignumber(purity))
        .divide(bignumber(ONE_HUNDRED))
        .multiply(bignumber(currentMol))
        .done()
        .toNumber();
}

/**
 * Compute mol by eq: Mol = Mol * (currentEq / prevEq)
 * @param mol
 * @param currentEq
 * @param prevEq
 * @returns {number}
 */
function computeMolByEq(mol, currentEq, prevEq) {
    return computeByEq(mol, currentEq, prevEq);
}

/**
 * Compute mol by eq: Mol = (MolOfLimiting * EqOfNonLimiting) / EqOfLimiting
 * @param molOfLimiting
 * @param eqOfNonLimiting
 * @param eqOfLimiting
 * @returns {number}
 */
function computeNonLimitingMolByEq(molOfLimiting, eqOfNonLimiting, eqOfLimiting) {
    return math
        .chain(bignumber(molOfLimiting))
        .multiply(bignumber(eqOfNonLimiting))
        .divide(bignumber(eqOfLimiting))
        .done()
        .toNumber();
}

/**
 * Compute weight: Weight = Mol * Mol Weight
 * @param mol
 * @param molWeight
 * @returns {number}
 */
function computeWeight(mol, molWeight) {
    return multiply(mol, molWeight);
}

/**
 * Compute weight by purity: Weight = (current Weight / Purity) * 100
 * @param purity
 * @param currentWeight
 * @returns {number}
 */
function computeWeightByPurity(purity, currentWeight) {
    return math
        .chain(bignumber(currentWeight))
        .divide(bignumber(purity))
        .multiply(bignumber(ONE_HUNDRED))
        .done()
        .toNumber();
}

/**
 * Compute weight by eq: Weight = Weight * (currentEq / prevEq)
 * @param weight
 * @param currentEq
 * @param prevEq
 * @returns {number}
 */
function computeWeightByEq(weight, currentEq, prevEq) {
    return computeByEq(weight, currentEq, prevEq);
}

/**
 * Compute EQ: EQ = Mol / Limiting Mol
 * @param mol
 * @param molOfLimiting
 * @returns {number}
 */
function computeEq(mol, molOfLimiting) {
    return divide(mol, molOfLimiting);
}

/**
 * Compute MolWeight: MolWeight = Weight / Mol
 * @param weight
 * @param mol
 * @returns {number}
 */
function computeMolWeight(weight, mol) {
    return divide(weight, mol);
}

/**
 * Compute MolWeight by Salt:
 * MolWeight = current MolWeight + SaltCode weight * Salt EQ
 * @param currentMolWeight
 * @param saltCodeWeight
 * @param saltEQ
 * @returns {number}
 */
function computeMolWeightBySalt(currentMolWeight, saltCodeWeight, saltEQ) {
    return math
        .chain(bignumber(saltCodeWeight))
        .multiply(bignumber(saltEQ))
        .add(bignumber(currentMolWeight))
        .done()
        .toNumber();
}

/**
 * Compute volume: Volume = Mol / Molarity
 * @param mol
 * @param molarity
 * @returns {number}
 */
function computeVolumeByMolarity(mol, molarity) {
    return divide(mol, molarity);
}

/**
 * Compute volume by density: Volume = Weight / Density / 1000
 * @param weight
 * @param density
 * @returns {number}
 */
function computeVolumeByDensity(weight, density) {
    return math
        .chain(bignumber(weight))
        .divide(bignumber(density))
        .divide(bignumber(ONE_THOUSAND))
        .done()
        .toNumber();
}

/**
 * Compute weight by density: Weight = Volume * Density * 1000
 * @param volume
 * @param density
 * @returns {number}
 */
function computeWeightByDensity(volume, density) {
    return math
        .chain(bignumber(volume))
        .multiply(bignumber(density))
        .multiply(bignumber(ONE_THOUSAND))
        .done()
        .toNumber();
}

// TODO: need more details
// function computeYield() {
//
// }

function multiply(x, y) {
    return math
        .multiply(bignumber(x), bignumber(y))
        .toNumber();
}

function divide(x, y) {
    return math
        .divide(bignumber(x), bignumber(y))
        .toNumber();
}

function computeByEq(weightOrMol, currentEq, prevEq) {
    return math
        .chain(bignumber(currentEq))
        .divide(bignumber(prevEq))
        .multiply(bignumber(weightOrMol))
        .done()
        .toNumber();
}

function bignumber(value) {
    return math.bignumber(value);
}

module.exports = calculationUtil;
