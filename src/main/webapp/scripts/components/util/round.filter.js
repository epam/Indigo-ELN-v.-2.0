angular.module('indigoeln')
    .filter('round', function () {
        var INITIAL = 0;
        var LEADZEROS = 1;
        var MIDZEROS = 2;
        var DIGITS = 3;
        var LEADZEROSDOT = 4;
        var DIGITSDOT = 5;
        var MANTISSA = 6;
        var MANTISSADIGIT = 7;
        
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

        var parse = function (data) {
            var stringNumber = data.number.toString();
            var length = stringNumber.length;
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
                                    data.digits = data.digits + '0';
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
                        }
                    }
                        break;
                    case '+':
                    {
                        switch (state) {
                            case INITIAL:
                            {
                                data.sign = true;
                                state = LEADZEROS;
                            }
                                break;
                            case MANTISSA:
                            {
                                state = MANTISSADIGIT;
                            }
                                break;
                        }
                    }
                        break;
                    case '-':
                    {
                        switch (state) {
                            case INITIAL:
                            {
                                data.sign = false;
                                state = LEADZEROS;
                            }
                                break;
                            case MANTISSA:
                            {
                                state = MANTISSADIGIT;
                            }
                                break;
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
                                data.digits = data.digits + c;
                                data.mantissa++;
                                state = DIGITS;
                            }
                                break;
                            case LEADZEROSDOT:
                            {
                                // only significant if number is all zeros.
                                data.mantissa--;
                                zeroCount++;
                                state = LEADZEROSDOT;
                            }
                                break;
                            case DIGITSDOT:
                            {
                                // non-leading zeros after a decimal point are always significant.
                                data.digits = data.digits + c;
                            }
                                break;
                            case MANTISSA:
                            case MANTISSADIGIT:
                            {
                                foundMantissaDigit = true;
                                state = MANTISSADIGIT;
                            }
                                break;
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
                                data.digits = data.digits + c;
                                data.mantissa++;
                                state = DIGITS;
                            }
                                break;
                            case MIDZEROS:
                            {
                                // we now know that these zeros
                                // are more than just trailing placeholders.
                                for (let j = 0; j < zeroCount; j++) {
                                    data.digits = data.digits + '0';
                                }
                                zeroCount = 0;
                                data.digits = data.digits + c;
                                data.mantissa++;
                                state = DIGITS;
                            }
                                break;
                            case LEADZEROSDOT:
                            case DIGITSDOT:
                            {
                                zeroCount = 0;
                                data.digits = data.digits + c;
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
                        }
                    }
                        break;
                }
            }
            if (mantissaStart !== -1) {
                // if we had found an 'E'
                if (!foundMantissaDigit) {
                    // we didn't actually find a mantissa to go with.
                    // throw new NumberFormatException("No digits in mantissa.");
                }
                // parse the mantissa.
                data.mantissa += parseInt(stringNumber.substring(mantissaStart));
            }
            if (data.digits.toString().length === 0) {
                if (zeroCount > 0) {
                    // if nothing but zeros all zeros are significant.
                    for (let j = 0; j < zeroCount; j++) {
                        data.digits = data.digits + '0';
                    }
                    data.mantissa += leadZeroCount;
                    data.isZero = true;
                    data.sign = true;
                } else {
                    // a hack to catch some cases that we could catch
                    // by adding a ton of extra states. Things like:
                    // "e2" "+e2" "+." "." "+" etc.
                    // throw new NumberFormatException("No digits in number.");
                }
            }
        };

        var setNumberSignificantFigures = function (data) {
            if (data.digits && data.digits.toString() !== '0' && data.digits.toString() !== '0.0') {
                var length = data.digits.toString().length;
                if (length < data.significantFigures) {
                    // number is not long enough, pad it with zeros.
                    for (let i = length; i < data.significantFigures; i++) {
                        data.digits = data.digits + '0';
                    }
                } else if (length > data.significantFigures) {
                    // number is too long chop some of it off with rounding.
                    var addOne; // we need to round up if true.
                    var firstInSig = data.digits.charAt(data.significantFigures);
                    if (firstInSig < '5') {
                        // first non-significant digit less than five, round down.
                        addOne = false;
                    } else if (firstInSig === '5') {
                        // first non-significant digit equal to five
                        addOne = false;
                        for (let i = data.significantFigures + 1; !addOne && i < length; i++) {
                            // if its followed by any non-zero digits, round up.
                            if (data.digits.charAt(i) !== '0') {
                                addOne = true;
                            }
                        }
                        if (!addOne) {
                            // if it was not followed by non-zero digits
                            // if the last significant digit is odd round up
                            // if the last significant digit is even round down
                            addOne = (data.digits.charAt(data.significantFigures - 1) && 1) === 1;
                        }
                    } else {
                        // first non-significant digit greater than five, round up.
                        addOne = true;
                    }
                    // loop to add one (and carry a one if added to a nine)
                    // to the last significant digit
                    for (let i = data.significantFigures - 1; addOne && i >= 0; i--) {
                        var digit = data.digits.charAt(i);
                        if (digit < '9') {
                            data.digits.replaceAt(i, digit + 1);
                            addOne = false;
                        } else {
                            data.digits.replaceAt(i, '0');
                        }
                    }
                    if (addOne) {
                        // if the number was all nines
                        data.digits = data.digits.insert(0, '1');
                        data.mantissa++;
                    }
                    // chop it to the correct number of figures.
                    data.digits = data.digits.substring(0, data.significantFigures);
                }
            }
            return data.digits;
        };
        var toDisplayResult = function (data) {
            if (data.digits === null) {
                return data.original;
            }
            var length = data.digits.length;
            if (data.mantissa <= -9 || data.mantissa >= 9 || (data.isZero && data.mantissa !== 0)) {
                // use scientific notation.
                if (length > 1) {
                    data.digits = data.digits.insert(1, '.');
                }
                if (data.mantissa !== 0) {
                    data.digits = data.digits + 'E' + data.mantissa;
                }
            } else if (data.mantissa <= -1) {
                data.digits = data.digits.insert(0, '0.');
                for (let i = data.mantissa; i < -1; i++) {
                    data.digits = data.digits.insert(2, '0');
                }
            } else if (data.mantissa < length && (data.mantissa !== length - 1)) {
                data.digits = data.digits.insert(data.mantissa + 1, '.');
            } else {
                for (let i = length; i <= data.mantissa; i++) {
                    data.digits = data.digits + '0';
                }
            }
            if (!data.sign) {
                data.digits = data.digits.insert(0, '-');
            }
            return data.digits.toString();
        };

        return function (value, isEntered, sigDigits) {
            if (isEntered || !value) {
                return value;
            }
            var data = {
                original: value,
                number: value,
                significantFigures: sigDigits || 3,
                digits: '',
                mantissa: -1,
                sign: true,
                isZero: false
            };
            parse(data);
            setNumberSignificantFigures(data);
            return +toDisplayResult(data);
        };
    });