function BaseBatchRow() {
}

BaseBatchRow.prototype = {
    setTheoMoles: setTheoMoles,
    setTheoWeight: setTheoWeight,
    setMolWeight: setMolWeight,
    setFormula: setFormula,
    constructor: BaseBatchRow
};

function setTheoMoles(value) {
    this.theoMoles.value = value;
}

function setTheoWeight(value) {
    this.theoWeight.value = value;
}

function setMolWeight(value) {
    this.molWeight.value = value;
}

function setFormula(value) {
    this.formula.value = value;
}

module.exports = BaseBatchRow;
