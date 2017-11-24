describe('service: configService', function() {
    var configService;

    beforeEach(angular.mock.module('indigoeln.common.services'));

    beforeEach(angular.mock.inject(function(_configService_) {
        configService = _configService_;
    }));

    it('should be defined', function() {
        expect(configService).toBeDefined();
    });
});
