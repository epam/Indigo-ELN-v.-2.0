angular.module('indigoeln')
    .filter('round', function () {

        return function (value, isEntered) {
            var trimmedToSignificantFigures;
            if (isEntered) {
                return value;
            }
            var original = value;
            var digits;
            var mantissa = -1;
            var sign = true;
            var isZero = false;

            var INITIAL = 0;
            var LEADZEROS = 1;
            var MIDZEROS = 2;
            var DIGITS = 3;
            var LEADZEROSDOT = 4;
            var DIGITSDOT = 5;
            var MANTISSA = 6;
            var MANTISSADIGIT = 7;

            var parse = function (number) {
                var stringNumber = number.toString();
                // var length = number.length;
                // digits = new StringBuffer(length);
                var length = stringNumber.length;
                digits = '';
                var state = INITIAL;
                var mantissaStart = -1;
                var foundMantissaDigit = false;
                // sometimes we don't know if a zero will be significant or not when it is encountered.
                // keep track of the number of them so that the all can be made significant if we find out that they are.
                var zeroCount = 0;
                var leadZeroCount = 0;

                for (let i = 0; i < length; i++) {
                    var c = stringNumber.charAt(i);
                    switch (c) {
                        case '.':
                        {
                            switch (state) {
                                case INITIAL:
                                case LEADZEROS:
                                {
                                    state = LEADZEROSDOT;
                                }
                                    break;
                                case MIDZEROS:
                                {
                                    // we now know that these zeros
                                    // are more than just trailing placeholders.
                                    for (let j = 0; j < zeroCount; j++) {
                                        digits = digits + '0';
                                    }
                                    zeroCount = 0;
                                    state = DIGITSDOT;
                                }
                                    break;
                                case DIGITS:
                                {
                                    state = DIGITSDOT;
                                }
                                    break;
                                default:
                                {
                                    // throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                                }
                            }
                        }
                            break;
                        case '+':
                        {
                            switch (state) {
                                case INITIAL:
                                {
                                    sign = true;
                                    state = LEADZEROS;
                                }
                                    break;
                                case MANTISSA:
                                {
                                    state = MANTISSADIGIT;
                                }
                                    break;
                                default:
                                {
                                    // throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                                }
                            }
                        }
                            break;
                        case '-':
                        {
                            switch (state) {
                                case INITIAL:
                                {
                                    sign = false;
                                    state = LEADZEROS;
                                }
                                    break;
                                case MANTISSA:
                                {
                                    state = MANTISSADIGIT;
                                }
                                    break;
                                default:
                                {
                                    // throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                                }
                            }
                        }
                            break;
                        case '0':
                        {
                            switch (state) {
                                case INITIAL:
                                case LEADZEROS:
                                {
                                    // only significant if number
                                    // is all zeros.
                                    zeroCount++;
                                    leadZeroCount++;
                                    state = LEADZEROS;
                                }
                                    break;
                                case MIDZEROS:
                                case DIGITS:
                                {
                                    // only significant if followed by a decimal point or nonzero digit.
                                    // mantissa++;
                                    // zeroCount++;
                                    // state = MIDZEROS;
                                    // zeroCount = 0;
                                    digits = digits + c;
                                    mantissa++;
                                    state = DIGITS;
                                }
                                    break;
                                case LEADZEROSDOT:
                                {
                                    // only significant if number is all zeros.
                                    mantissa--;
                                    zeroCount++;
                                    state = LEADZEROSDOT;
                                }
                                    break;
                                case DIGITSDOT:
                                {
                                    // non-leading zeros after a decimal point are always significant.
                                    digits = digits + c;
                                }
                                    break;
                                case MANTISSA:
                                case MANTISSADIGIT:
                                {
                                    foundMantissaDigit = true;
                                    state = MANTISSADIGIT;
                                }
                                    break;
                                default:
                                {
                                    // throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                                }
                            }
                        }
                            break;
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        {
                            switch (state) {
                                case INITIAL:
                                case LEADZEROS:
                                case DIGITS:
                                {
                                    zeroCount = 0;
                                    digits = digits + c;
                                    mantissa++;
                                    state = DIGITS;
                                }
                                    break;
                                case MIDZEROS:
                                {
                                    // we now know that these zeros
                                    // are more than just trailing placeholders.
                                    for (let j = 0; j < zeroCount; j++) {
                                        digits = digits + '0';
                                    }
                                    zeroCount = 0;
                                    digits = digits + c;
                                    mantissa++;
                                    state = DIGITS;
                                }
                                    break;
                                case LEADZEROSDOT:
                                case DIGITSDOT:
                                {
                                    zeroCount = 0;
                                    digits = digits + c;
                                    state = DIGITSDOT;
                                }
                                    break;
                                case MANTISSA:
                                case MANTISSADIGIT:
                                {
                                    state = MANTISSADIGIT;
                                    foundMantissaDigit = true;
                                }
                                    break;
                                default:
                                {
                                    // throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                                }
                            }
                        }
                            break;
                        case 'E':
                        case 'e':
                        {
                            switch (state) {
                                case INITIAL:
                                case LEADZEROS:
                                case DIGITS:
                                case LEADZEROSDOT:
                                case DIGITSDOT:
                                {
                                    // record the starting point of the mantissa
                                    // so we can do a substring to get it back later
                                    mantissaStart = i + 1;
                                    state = MANTISSA;
                                }
                                    break;
                                default:
                                {
                                    // throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                                }
                            }
                        }
                            break;
                        default:
                        {
                            // throw new NumberFormatException("Unexpected character '" + c + "' at position " + i);
                        }
                    }
                }
                if (mantissaStart !== -1) {
                    // if we had found an 'E'
                    if (!foundMantissaDigit) {
                        // we didn't actually find a mantissa to go with.
                        // throw new NumberFormatException("No digits in mantissa.");
                    }
                    // parse the mantissa.
                    mantissa += parseInt(stringNumber.substring(mantissaStart));
                }
                if (digits.toString().length === 0) {
                    if (zeroCount > 0) {
                        // if nothing but zeros all zeros are significant.
                        for (let j = 0; j < zeroCount; j++) {
                            digits = digits + '0';
                        }
                        mantissa += leadZeroCount;
                        isZero = true;
                        sign = true;
                    } else {
                        // a hack to catch some cases that we could catch
                        // by adding a ton of extra states. Things like:
                        // "e2" "+e2" "+." "." "+" etc.
                        // throw new NumberFormatException("No digits in number.");
                    }
                }
            };
            String.prototype.replaceAt = function (index, character) {
                return this.substr(0, index) + character + this.substr(index + character.length);
            };
            String.prototype.insert = function (index, string) {
                if (index > 0) {
                    return this.substring(0, index) + string + this.substring(index, this.length);
                } else {
                    return string + this;
                }
            };

            var setNumberSignificantFigures = function (significantFigures) {
                if (digits && digits.toString() !== '0' && digits.toString() !== '0.0') {
                    var length = digits.toString().length;
                    if (length < significantFigures) {
                        // number is not long enough, pad it with zeros.
                        for (let i = length; i < significantFigures; i++) {
                            digits = digits + '0';
                        }
                    } else if (length > significantFigures) {
                        // number is too long chop some of it off with rounding.
                        var addOne; // we need to round up if true.
                        var firstInSig = digits.charAt(significantFigures);
                        if (firstInSig < '5') {
                            // first non-significant digit less than five, round down.
                            addOne = false;
                        } else if (firstInSig === '5') {
                            // first non-significant digit equal to five
                            addOne = false;
                            for (let i = significantFigures + 1; !addOne && i < length; i++) {
                                // if its followed by any non-zero digits, round up.
                                if (digits.charAt(i) !== '0') {
                                    addOne = true;
                                }
                            }
                            if (!addOne) {
                                // if it was not followed by non-zero digits
                                // if the last significant digit is odd round up
                                // if the last significant digit is even round down
                                addOne = (digits.charAt(significantFigures - 1) && 1) === 1;
                            }
                        } else {
                            // first non-significant digit greater than five, round up.
                            addOne = true;
                        }
                        // loop to add one (and carry a one if added to a nine)
                        // to the last significant digit
                        for (let i = significantFigures - 1; addOne && i >= 0; i--) {
                            var digit = digits.charAt(i);
                            if (digit < '9') {
                                digits.replaceAt(i, digit + 1);
                                addOne = false;
                            } else {
                                digits.replaceAt(i, '0');
                            }
                        }
                        if (addOne) {
                            // if the number was all nines
                            digits = digits.insert(0, '1');
                            mantissa++;
                        }
                        // chop it to the correct number of figures.
                        digits = digits.substring(0, significantFigures);
                    }
                }
                return digits;
            };
            var toDisplayResult = function () {
                if (digits === null) {
                    return original;
                }
                var length = digits.length;
                if (mantissa <= -9 || mantissa >= 9 || (isZero && mantissa !== 0)) {
                    // use scientific notation.
                    if (length > 1) {
                        digits = digits.insert(1, '.');
                    }
                    if (mantissa !== 0) {
                        digits = digits + 'E' + mantissa;
                    }
                } else if (mantissa <= -1) {
                    digits = digits.insert(0, '0.');
                    for (let i = mantissa; i < -1; i++) {
                        digits = digits.insert(2, '0');
                    }
                } else if (mantissa < length && (mantissa !== length - 1)) {
                    digits = digits.insert(mantissa + 1, '.');
                } else {
                    for (let i = length; i <= mantissa; i++) {
                        digits = digits + '0';
                    }
                }
                if (!sign) {
                    digits = digits.insert(0, '-');
                }
                return digits.toString();
            };

            parse(value);
            trimmedToSignificantFigures = setNumberSignificantFigures(3);
            return toDisplayResult(trimmedToSignificantFigures);
        };
    });