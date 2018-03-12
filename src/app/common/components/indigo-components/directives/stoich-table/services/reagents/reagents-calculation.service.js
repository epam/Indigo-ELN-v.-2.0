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

var fieldTypes = require('../../../../services/calculation/field-types');
var mathCalculation = require('../../../../services/calculation/math-calculation');

/* @ngInject */
function reagentsCalculation(calculationHelper) {
    var rows;

    return {
        calculate: calculate
    };

    function calculate(reagentsData) {
        rows = calculationHelper.clone(reagentsData.rows);
        var changedRow = calculationHelper.findChangedRow(rows, reagentsData.idOfChangedRow);

        recalculate(changedRow, reagentsData.changedField);

        return rows;
    }

    function recalculate(row, fieldId) {
        // Handle necessary recalculations here
        if (!fieldId) {
            onAddNewRow(row);
        }

        recalculateNumberFields(row, fieldId);

        if (fieldId === fieldTypes.rxnRole) {
            onRxnRoleChanged(row);
        }

        if (fieldId === fieldTypes.saltCode || fieldId === fieldTypes.saltEq) {
            onSaltChanged(row);
        }

        if (fieldId === fieldTypes.compoundId || fieldId === fieldTypes.fullNbkBatch) {
            onNbkBatchOrCompoundIdChanged(row);
        }
    }

    function recalculateNumberFields(row, fieldId) {
        if (fieldId === fieldTypes.molWeight) {
            row.setEntered(fieldId);
            onMolWeightChanged(row);

            return;
        }
        if (fieldId === fieldTypes.weight) {
            row.setEntered(fieldId);
            onWeightChanged(row);

            return;
        }
        if (fieldId === fieldTypes.mol) {
            row.setEntered(fieldId);
            onMolChanged(row);

            return;
        }
        if (fieldId === fieldTypes.eq) {
            row.setEntered(fieldId);
            onEqChanged(row);

            return;
        }
        if (fieldId === fieldTypes.volume) {
            row.setEntered(fieldId);
            onVolumeChanged(row);

            return;
        }
        if (fieldId === fieldTypes.molarity) {
            row.setEntered(fieldId);
            onMolarityChanged(row);

            return;
        }
        if (fieldId === fieldTypes.stoicPurity) {
            row.setEntered(fieldId);
            onPurityChanged(row);

            return;
        }
        if (fieldId === fieldTypes.density) {
            row.setEntered(fieldId);
            onDensityChanged(row);
        }
    }

    function onMolWeightChanged(row) {
        row.molWeight.originalValue = row.molWeight.value;

        if (!row.isMolWeightPresent()) {
            return;
        }
        if (row.isLimiting() && row.isWeightPresent()) {
            var mol = mathCalculation.computeMol(row.weight.value, row.molWeight.value, row.stoicPurity.value);
            updateMol(row, mol);

            return;
        }
        if (row.isMolPresent()) {
            var weight = mathCalculation.computeWeight(row.mol.value, row.molWeight.value, row.stoicPurity.value);
            row.setComputedWeight(weight, onWeightChanged);
        }
    }

    function onWeightChanged(row) {
        if (row.isWeightPresent() && row.isMolWeightPresent()) {
            var mol = mathCalculation.computeMol(row.weight.value, row.molWeight.value, row.stoicPurity.value);
            row.setComputedMol(mol);

            if (row.isLimiting()) {
                updateRows();
            }
        }

        if (!row.isWeightPresent()) {
            row.resetFields(row.getResetFieldsForWeight());
        }

        updateDependencies(row);
    }

    function onMolChanged(row) {
        if (row.isMolWeightPresent() && row.isMolPresent()) {
            var weight = mathCalculation.computeWeight(row.mol.value, row.molWeight.value, row.stoicPurity.value);
            row.setComputedWeight(weight);
        }

        if (row.isLimiting()) {
            updateRows();
        }

        if (!row.isMolPresent()) {
            row.resetFields(row.getResetFieldsForMol());
        }

        updateDependencies(row);
    }

    function onEqChangedCanResetVolume(row) {
        return row.isVolumePresent() && !row.isDensityPresent() && !row.isMolarityPresent();
    }

    function onEqChanged(row) {
        if (!row.eq.value) {
            row.eq.value = row.eq.prevValue;

            return;
        }

        // reset volume
        if (onEqChangedCanResetVolume(row)) {
            row.resetFields([fieldTypes.volume]);
            setMolDependingOfLimiting(row, getLimitingRow());

            return;
        }

        if (row.isLimiting()) {
            if (row.isMolManuallyEntered() && row.isWeightPresent()) {
                var limitingWeight = mathCalculation.computeWeightByEq(
                    row.weight.value, row.eq.value, row.eq.prevValue
                );
                row.setComputedWeight(limitingWeight);
            }

            if (row.isMolPresent()) {
                updateRows();
            }
        } else {
            if (row.isWeightPresent()) {
                var weight = mathCalculation.computeWeightByEq(row.weight.value, row.eq.value, row.eq.prevValue);
                row.setComputedWeight(weight);
            }

            if (row.isMolPresent()) {
                var mol = mathCalculation.computeMolByEq(row.mol.value, row.eq.value, row.eq.prevValue);
                row.setComputedMol(mol);
            }

            row.updateVolume();
        }

        row.eq.prevValue = row.eq.value;
    }

    function onRxnRoleChanged(row) {
        var fieldsToReset = row.getResetFieldsForSolvent();

        if (row.isSolventRow()) {
            var nextRow = getRowAfterLimiting();
            var isLimiting = row.isLimiting();

            row.resetFields(fieldsToReset);
            row.setReadonly(fieldsToReset, true);

            if (isLimiting) {
                row.limiting.value = false;
            }

            if (isLimiting && nextRow) {
                nextRow.limiting.value = true;
            }
        } else if (!row.isSolventRow() && row.prevRxnRole.name === 'SOLVENT') {
            row.resetFields([fieldTypes.volume]);
            row.setReadonly(fieldsToReset, false);

            if (isLimitingRowExist()) {
                row.setComputedMol(getLimitingRow().mol.value, onMolChanged);
            }
        }

        row.prevRxnRole.name = row.rxnRole.name;
    }

    function resetVolumeCanChangeDensity(row) {
        return row.isMolPresent() && row.isWeightPresent() && row.isDensityPresent();
    }

    function resetVolume(row) {
        row.resetFields([fieldTypes.volume]);

        if (resetVolumeCanChangeDensity(row)) {
            // re-calculate volume based on mol, weight and density
            onDensityChanged(row);

            return;
        }

        if (row.isMolarityPresent() && !row.isMolManuallyEntered()) {
            row.resetFields([fieldTypes.mol], onMolChanged);

            return;
        }

        if (row.isDensityPresent() && !row.isWeightManuallyEntered()) {
            row.resetFields([fieldTypes.weight], onWeightChanged);

            return;
        }

        if (!row.isSolventRow() && !row.isWeightManuallyEntered() && !row.isMolManuallyEntered()) {
            setMolDependingOfLimiting(row, getLimitingRow());
        }
    }

    function onVolumeChanged(row) {
        if (!row.isVolumePresent()) {
            resetVolume(row);

            return;
        }

        if (row.isMolarityPresent()) {
            var mol = mathCalculation.computeDissolvedMol(row.molarity.value, row.volume.value);
            row.setComputedMol(mol, onMolChanged);

            return;
        }

        if (row.isDensityPresent()) {
            var weight = mathCalculation.computeWeightByDensity(row.volume.value, row.density.value);
            row.setComputedWeight(weight, onWeightChanged);

            return;
        }

        if (!row.isSolventRow()) {
            row.resetFields([fieldTypes.weight, fieldTypes.mol, fieldTypes.eq]);

            if (row.isLimiting() && !canBeLimiting(row)) {
                var nextRow = getRowAfterLimiting();
                if (nextRow) {
                    nextRow.limiting.value = true;
                }
                row.limiting.value = false;
            }
        }
    }

    function onMolarityChanged(row) {
        if (!row.isMolarityPresent()) {
            var fieldToReset = row.getResetFieldForMolarity();
            var callback = fieldToReset === fieldTypes.mol ? onMolChanged : null;
            row.resetFields([fieldToReset, fieldTypes.molarity], callback);
            setMolFromLimitingRow(row, row.isMolPresent());

            return;
        }

        if (row.isVolumePresent()) {
            var mol = mathCalculation.computeDissolvedMol(row.molarity.value, row.volume.value);
            row.setComputedMol(mol, onMolChanged);

            if (!isLimitingRowExist()) {
                row.limiting.value = true;
            }

            return;
        }

        if (!row.isVolumePresent() && row.isMolPresent()) {
            var volume = mathCalculation.computeVolumeByMolarity(row.mol.value, row.molarity.value);
            row.setComputedVolume(volume);
        }
    }

    function onPurityChanged(row) {
        var prevPurity = row.stoicPurity.prevValue;
        var currentPurity = row.stoicPurity.value;

        if (!row.isPurityPresent()) {
            row.stoicPurity.value = row.stoicPurity.prevValue;

            return;
        }

        row.stoicPurity.prevValue = row.stoicPurity.value;

        // Mol and Weight Empty
        if (!row.isMolPresent() && !row.isWeightPresent()) {
            return;
        }

        // should Update Mol
        if (row.isWeightManuallyEntered() && row.isMolPresent()) {
            var mol = mathCalculation.computeMolByPurity(row.mol.value, currentPurity, prevPurity);
            row.setComputedMol(mol, updateDependencies);

            if (row.isLimiting()) {
                updateRows();
            }

            return;
        }

        // should Update Weight
        var shouldUpdateWeight = row.isMolManuallyEntered() ? row.isWeightPresent() : !row.isWeightManuallyEntered();
        if (shouldUpdateWeight) {
            var weight = mathCalculation.computeWeightByPurity(row.weight.value, currentPurity, prevPurity);
            row.setComputedWeight(weight, updateDependencies);
        }
    }

    function onSaltChanged(row) {
        if (!row.molWeight.originalValue) {
            return;
        }

        if (row.saltCode.regValue === '00') {
            row.setDefaultValues([fieldTypes.saltEq]);
        }

        var molWeight = mathCalculation.computeMolWeightBySalt(
            row.molWeight.originalValue, row.saltCode.regValue, row.saltEq.value
        );

        var formula = row.formula.baseValue
            ? calculationHelper.getFormula(row)
            : null;

        row.setComputedMolWeight(molWeight, row.molWeight.originalValue, onMolWeightChanged);
        row.setFormula(formula);
    }

    function onDensityChanged(row) {
        if (!row.isDensityPresent()) {
            var fieldToReset = row.getResetFieldForDensity();
            var callback = fieldToReset === fieldTypes.weight ? onWeightChanged : null;
            row.resetFields([fieldToReset, fieldTypes.density], callback);
            setMolFromLimitingRow(row, row.isWeightPresent());

            return;
        }

        if (row.isVolumePresent()) {
            var weight = mathCalculation.computeWeightByDensity(row.volume.value, row.density.value);
            row.setComputedWeight(weight, onWeightChanged);

            return;
        }

        if (row.isWeightPresent()) {
            var volume = mathCalculation.computeVolumeByDensity(row.weight.value, row.density.value);
            row.setComputedVolume(volume);
        }
    }

    function onNbkBatchOrCompoundIdChanged(row) {
        if (!isLimitingRowExist()) {
            row.limiting.value = true;

            return;
        }

        setMolDependingOfLimiting(row, getLimitingRow());
    }

    function onAddNewRow(newRow) {
        if (newRow.isSolventRow()) {
            newRow.prevRxnRole.name = 'SOLVENT';
            newRow.setReadonly(newRow.getResetFieldsForSolvent(), true);

            return;
        }
        if (isLimitingRowExist()) {
            setMolDependingOfLimiting(newRow, getLimitingRow());

            return;
        }

        newRow.limiting.value = true;
    }

    function canBeLimiting(row) {
        var canCalcMol = row.volume.value > 0 && (row.molarity.value > 0 || row.density.value > 0);
        var hasDimension = row.mol.value > 0 || canCalcMol;

        return !row.isSolventRow() && row.eq.value === 1 && hasDimension;
    }

    function getRowAfterLimiting() {
        var limitingRowIndex = _.findIndex(rows, function(r) {
            return r.isLimiting();
        });

        var i;
        for (i = limitingRowIndex + 1; i < rows.length; i++) {
            if (canBeLimiting(rows[i])) {
                return rows[i];
            }
        }

        for (i = 0; i < limitingRowIndex; i++) {
            if (canBeLimiting(rows[i])) {
                return rows[i];
            }
        }

        return null;
    }

    function updateRows() {
        if (!isLimitingRowExist()) {
            return;
        }

        var limitingRow = getLimitingRow();
        _.forEach(rows, function(row) {
            var canUpdate = !row.isLimiting() && !row.isSolventRow();
            var isManuallyEnteredExist =
                row.isWeightManuallyEntered() || row.isVolumeManuallyEntered() || row.isMolManuallyEntered();
            var shouldUpdateRowWithNewMol = !isManuallyEnteredExist && canUpdate;
            var shouldUpdateOnlyEq = isManuallyEnteredExist && canUpdate;

            if (shouldUpdateRowWithNewMol) {
                setMolDependingOfLimiting(row, limitingRow);
            }

            if (shouldUpdateOnlyEq) {
                row.updateEq(limitingRow);
            }
        });
    }

    function setMolFromLimitingRow(row, isMolOrWeightPresent) {
        if (!isMolOrWeightPresent && !row.isVolumePresent()) {
            setMolDependingOfLimiting(row, getLimitingRow());
        }
    }

    function updateDependencies(row) {
        row.updateVolume();

        if (!row.isLimiting()) {
            row.updateEq(getLimitingRow());
        }
        row.setMolWeight();
    }

    function updateMol(row, mol) {
        if (row.isWeightManuallyEntered()) {
            row.setComputedMol(mol, updateDependencies);
            updateRows();
        } else {
            row.setComputedMol(mol, onMolChanged);
        }
    }

    function setMolDependingOfLimiting(row, limitingRow) {
        if (limitingRow && limitingRow.isMolPresent()) {
            var mol = mathCalculation.computeNonLimitingMolByEq(
                limitingRow.mol.value, row.eq.value, limitingRow.eq.value
            );
            row.setComputedMol(mol, onMolChanged);
        }
    }

    function isLimitingRowExist() {
        return _.some(rows, ['limiting.value', true]);
    }

    function getLimitingRow() {
        return _.find(rows, ['limiting.value', true]);
    }
}

module.exports = reagentsCalculation;
