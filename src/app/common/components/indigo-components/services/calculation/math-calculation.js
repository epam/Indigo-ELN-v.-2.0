var math = require('mathjs');

var ONE_HUNDRED = 100;
var ONE_THOUSAND = 1000;

function mathCalculation() {
}

mathCalculation.computeMol = computeMol;
mathCalculation.computeDissolvedMol = computeDissolvedMol;
mathCalculation.computeMolByPurity = computeMolByPurity;
mathCalculation.computeMolByEq = computeMolByEq;
mathCalculation.computeNonLimitingMolByEq = computeNonLimitingMolByEq;
mathCalculation.computeWeight = computeWeight;
mathCalculation.computeWeightByEq = computeWeightByEq;
mathCalculation.computeWeightByPurity = computeWeightByPurity;
mathCalculation.computeEq = computeEq;
mathCalculation.computeMolWeight = computeMolWeight;
mathCalculation.computeMolWeightBySalt = computeMolWeightBySalt;
mathCalculation.computeCurrentMolWeightBySalt = computeCurrentMolWeightBySalt;
mathCalculation.computeVolumeByMolarity = computeVolumeByMolarity;
mathCalculation.computeVolumeByDensity = computeVolumeByDensity;
mathCalculation.computeWeightByDensity = computeWeightByDensity;
mathCalculation.computeYield = computeYield;
mathCalculation.multiply = multiply;
mathCalculation.divide = divide;

/**
 * Compute mol: Mol = (Weight / Mol weight) * (Purity / 100)
 * @param weight
 * @param molWeight
 * @param purity
 * @returns {number}
 */
function computeMol(weight, molWeight, purity) {
    return math
        .chain(bignumber(weight))
        .divide(bignumber(molWeight))
        .multiply(bignumber(purity))
        .divide(bignumber(ONE_HUNDRED))
        .done()
        .toNumber();
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
 * Compute mol by purity: Mol = (current Mol * Current Purity) / Prev Purity
 * @param currentMol
 * @param currentPurity
 * @param prevPurity
 * @returns {number}
 */
function computeMolByPurity(currentMol, currentPurity, prevPurity) {
    return math
        .chain(bignumber(currentMol))
        .multiply(bignumber(currentPurity))
        .divide(bignumber(prevPurity))
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
 * Compute weight: Weight = (Mol * Mol Weight * 100) / Purity
 * @param mol
 * @param molWeight
 * @param purity
 * @returns {number}
 */
function computeWeight(mol, molWeight, purity) {
    return math
        .chain(bignumber(mol))
        .multiply(bignumber(molWeight))
        .multiply(bignumber(ONE_HUNDRED))
        .divide(bignumber(purity))
        .done()
        .toNumber();
}

/**
 * Compute weight by purity: Weight = (current Weight / Current Purity) * Prev Purity
 * @param currentWeight
 * @param currentPurity
 * @param prevPurity
 * @returns {number}
 */
function computeWeightByPurity(currentWeight, currentPurity, prevPurity) {
    return math
        .chain(bignumber(currentWeight))
        .divide(bignumber(currentPurity))
        .multiply(bignumber(prevPurity))
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
 * Compute EQ: EQ = ( Mol * Limiting Eq) / Limiting Mol
 * @param mol
 * @param eqOfLimiting
 * @param molOfLimiting
 * @returns {number}
 */
function computeEq(mol, eqOfLimiting, molOfLimiting) {
    return math
        .chain(bignumber(mol))
        .multiply(bignumber(eqOfLimiting))
        .divide(bignumber(molOfLimiting))
        .done()
        .toNumber();
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
 * Compute MolWeight by Salt:
 * currentMolWeight = MolWeight - SaltCode weight * Salt EQ
 * @param molWeight
 * @param saltCodeWeight
 * @param saltEQ
 * @returns {number}
 */
function computeCurrentMolWeightBySalt(molWeight, saltCodeWeight, saltEQ) {
    return math
        .chain(bignumber(saltCodeWeight))
        .multiply(bignumber(saltEQ))
        .multiply(-1)
        .add(bignumber(molWeight))
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

/**
 * Compute yield: Yield = (TotalMoles / TheoMoles) * 100
 * @param totalMoles
 * @param theoMoles
 * @returns {number}
 */
function computeYield(totalMoles, theoMoles) {
    var res = math
        .chain(bignumber(totalMoles))
        .divide(bignumber(theoMoles))
        .multiply(bignumber(ONE_HUNDRED))
        .done()
        .toNumber();

    return math.round(res);
}

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

module.exports = mathCalculation;
