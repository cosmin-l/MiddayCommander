var blessed = require('blessed');

const createLeftPanel = () =>{
    let leftPanel = blessed.box({
        top: '3%',
        left: 'left',
        width: '50%',
        height: '100%-3',
        tags: true,
        border: {
          type: 'line'
        },
        style: {
          fg: 'white',
          bg: 'lightblue',
          border: {
            fg: '#f0f0f0',
            bg: 12
          }
        }
      });
    return leftPanel;
}

const createLeftNamePanel = (content) => {
    let leftNamePanel = blessed.box({
        top: '5%',
        left: 'left+1',
        width: '30%',
        height: '100%-5',
        content: "{center}{bold}Name{/bold}{/center}",
        tags: true,
        style: {
          fg: 'white',
          bg: 'green',
        }
      });
    return leftNamePanel;
}


const createLeftSizePanel = (content) => {
    let leftSizePanel = blessed.box({
        top: '5%',
        left: '30%',
        width: '10%',
        height: '100%-5',
        content: "{center}{bold}Size{/bold}{/center}",
        tags: true,
        style: {
          fg: 'white',
          bg: 'lightgreen',
        }
      });
    return leftSizePanel;
}

const createLeftModifiedPanel = (content) => {
    let leftModifiedPanel = blessed.box({
        top: '5%',
        left: '40%',
        width: '10%',
        height: '100%-5',
        content: "{center}{bold}Modified{/bold}{/center}",
        tags: true,
        style: {
          fg: 'white',
          bg: 'blue',
        }
      });
    return leftModifiedPanel;
}

const createRightPanel = () =>{
    let rightPanel = blessed.box({
        top: '3%',
        right: '0',
        width: '50%',
        height: '100%-3',
        tags: true,
        border: {
          type: 'line'
        },
        style: {
          fg: 'white',
          bg: 'lightblue',
          border: {
            fg: '#f0f0f0',
            bg: 12
          }
        }
      });
    return rightPanel;
}

const createRightNamePanel = (content) => {
    let rightNamePanel = blessed.box({
        top: '5%',
        left: '50%+2',
        width: '30%',
        height: '100%-5',
        content: "{center}{bold}Name{/bold}{/center}",
        tags: true,
        style: {
          fg: 'white',
          bg: 'green',
        }
      });
    return rightNamePanel;
}


const createRightSizePanel = (content) => {
    let rightSizePanel = blessed.box({
        top: '5%',
        left: '80%+1',
        width: '10%',
        height: '100%-5',
        content: "{center}{bold}Size{/bold}{/center}",
        tags: true,
        style: {
          fg: 'white',
          bg: 'lightgreen',
        }
      });
    return rightSizePanel;
}

const createRightModifiedPanel = (content) => {
    let rightModifiedPanel = blessed.box({
        top: '5%',
        left: '90%',
        width: '10%',
        height: '100%-5',
        content: "{center}{bold}Modified{/bold}{/center}",
        tags: true,
        style: {
          fg: 'white',
          bg: 'blue',
        }
      });
    return rightModifiedPanel;
}

exports.createLeftPanel = createLeftPanel;
exports.createLeftNamePanel = createLeftNamePanel;
exports.createLeftSizePanel = createLeftSizePanel;
exports.createLeftModifiedPanel = createLeftModifiedPanel;

exports.createRightPanel = createRightPanel;
exports.createRightNamePanel = createRightNamePanel;
exports.createRightSizePanel = createRightSizePanel;
exports.createRightModifiedPanel = createRightModifiedPanel;

