var template = require('./simple-node.html');

function simpleNode() {
    return {
        replace: true,
        transclude: true,
        template: template
    };
}

module.exports = simpleNode;

