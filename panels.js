var blessed = require('blessed');

const createLeftFM = (screen) => {
    let leftFm = blessed.filemanager({
        parent: screen,
        border: 'line',
        style: {
          fg: 'white',
          bg: 'blue',
          border: {
            fg: 'white',
            bg: 'blue'
          },
          selected: {
            fg: 'black', 
            bg: 'white'
          }
        },
        top: '3%',
        left: 'left',
        width: '50%',
        height: '100%-3',
        label: ' {white-fg}%path{/white-fg} ',
        cwd: process.env.HOME,
        keys: true,
        vi: true,
        scrollbar: {
          bg: 'yellow',
          ch: ' '
        }
      });
      return leftFm
}

const createRightFm = (screen) =>{
    let rightFm = blessed.filemanager({
        parent: screen,
        border: 'line',
        style: {
          fg: 'white',
          bg: 'blue',
          border: {
            fg: 'white',
            bg: 'blue'
          },
          selected: {
            bg: 'white'
          }
        },
        top: '3%',
        right: '0',
        width: '50%',
        height: '100%-3',
        label: ' {white-fg}%path{/white-fg} ',
        cwd: process.env.HOME,
        keys: true,
        vi: true,
        scrollbar: {
          bg: 'yellow',
          ch: ' '
        }
      });
      return rightFm
}

const disableSelector = (filemanager) => {
    filemanager.style.selected = {
        bg: 'blue',
        fg: 'white'
      }
}

const enableSelector = (filemanager) => {
    filemanager.style.selected = {
        bg: 'white',
        fg: 'black'
      }
}

exports.createLeftFM = createLeftFM;
exports.createRightFm = createRightFm;
exports.disableSelector = disableSelector;
exports.enableSelector = enableSelector;