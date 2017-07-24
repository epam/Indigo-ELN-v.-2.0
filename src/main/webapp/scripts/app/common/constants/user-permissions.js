angular
    .module('indigoeln')
    .constant('userPermissions', {
        VIEWER: {
            id: 'VIEWER', name: 'VIEWER (read notebook)'
        },
        USER: {
            id: 'USER', name: 'USER (read notebook, create experiments)'
        },
        OWNER: {
            id: 'OWNER', name: 'OWNER (read/update notebook, create experiments)'
        }
    });
