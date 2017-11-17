var template = require('./simple-node.html');

function simpleNode() {
    return {
        transclude: true,
        template: template
    };
}

module.exports = simpleNode;

