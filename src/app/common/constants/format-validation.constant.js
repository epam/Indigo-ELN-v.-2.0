var formatValidation = {
    // Regex for compoundId validation
    // Matches 'STR-00000012'
    indigoCompoundId: /^STR-\d{8}$/,

    // Matches 'STR-00000012-01'
    indigoCompoundIdFull: /^STR-\d{8}-\d{2}$/
};

module.exports = formatValidation;
