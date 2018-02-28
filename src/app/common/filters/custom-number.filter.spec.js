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

var cnumber = require('./custom-number.filter');

describe('filter: customNumber', function() {
    var TOO_LONG = 'Too Long';
    var TEST = 'anything';

    var filter;

    beforeEach(function() {
        filter = cnumber();
    });

    it('should return empty string for undefined, null', function() {
        expect(filter()).toBe('');
        expect(filter(null)).toBe('');
    });

    it('should return 0 string for zero', function() {
        expect(filter(0)).toBe('0');
    });

    it('should return empty string for too small value', function() {
        expect(filter(0.995e-300)).toBe('');
        expect(filter(Number.MIN_VALUE)).toBe('');
    });

    it('should return \'Too large\' for too big value', function() {
        expect(filter(1e1000)).toBe(TOO_LONG);
        expect(filter(1.1e300)).toBe(TOO_LONG);
        expect(filter(Number.MAX_VALUE)).toBe(TOO_LONG);
    });

    it('should return decimals for normal numbers', function() {
        expect(filter(1)).toBe('1');
        expect(filter(1.1)).toBe('1.1');
        expect(filter(12.122)).toBe('12.122');
        expect(filter(12.1235)).toBe('12.124');
        expect(filter(12.10)).toBe('12.1');
        expect(filter(121.1235)).toBe('121.12');
        expect(filter(1234.12)).toBe('1234.1');
        expect(filter(99999)).toBe('99999');
        expect(filter(100000.123)).toBe('100000');

        expect(filter(0.1)).toBe('0.1');
        expect(filter(0.123456)).toBe('0.1235');
        expect(filter(0.0123456)).toBe('0.0123');
        expect(filter(0.0012345)).toBe('0.0012');
    });

    it('should return exponential for big numbers', function() {
        expect(filter(10000000)).toBe('1.00e+7');
        expect(filter(1.234e+7)).toBe('1.23e+7');
        expect(filter(1.234e+10)).toBe('1.2e+10');
        expect(filter(1.234e+100)).toBe('1.2e+100');
        expect(filter(0.9499e+300)).toBe('9.5e+299');
    });

    it('should return exponential for small numbers', function() {
        expect(filter(0.00001)).toBe('1.00e-5');
        expect(filter(1.234e-7)).toBe('1.23e-7');
        expect(filter(1.234e-10)).toBe('1.2e-10');
        expect(filter(9.234e-5)).toBe('9.23e-5');
        expect(filter(1.111e-300)).toBe('1.1e-300');
    });

    it('should return passed empty value if value is missing', function() {
        expect(filter(0, TEST)).toBe('0');
        expect(filter(1e-400, TEST)).toBe('0');
        expect(filter(1e-301, TEST)).toBe('0');
        expect(filter(null, TEST)).toBe(TEST);
        expect(filter(undefined, TEST)).toBe(TEST);
    });

    it('should return decimals for normal numbers uses maxSymbols param', function() {
        var maxSymbols = 3;
        expect(filter(1, null, maxSymbols)).toBe('1');
        expect(filter(1.1, null, maxSymbols)).toBe('1.1');
        expect(filter(12.122, null, maxSymbols)).toBe('12');
        expect(filter(12.1235, null, maxSymbols)).toBe('12');
        expect(filter(12.10, null, maxSymbols)).toBe('12');
        expect(filter(121.1235, null, maxSymbols)).toBe('121');
        expect(filter(1234.12, null, maxSymbols)).toBe('1.2e+3');
        expect(filter(99999, null, maxSymbols)).toBe('1.0e+5');
        expect(filter(100000.123, null, maxSymbols)).toBe('1.0e+5');

        expect(filter(0.1, null, maxSymbols)).toBe('0.1');
        expect(filter(0.123456, null, maxSymbols)).toBe('0.1');
        expect(filter(0.0123456, null, maxSymbols)).toBe('1.2e-2');
        expect(filter(0.0012345, null, maxSymbols)).toBe('1.2e-3');
    });
});
