const blessed = require('blessed');
const buttonsAPI = require('./buttons');
const panelsAPI = require('./panels');
 
// Create a screen object.
var screen = blessed.screen({
  smartCSR: true,
  autoPadding : true
});
 
screen.title = 'Midday Commander';
 
let background = blessed.box({
  top: '2%',
  left: 'center',
  width: '100%',
  height: '96%',
  style: {
    fg: 'white',
    bg: 'lightblue',
  }
});

let leftPanel = panelsAPI.createLeftPanel();
let rightPanel = panelsAPI.createRightPanel();
let leftNamePanel = panelsAPI.createLeftNamePanel();
let leftSizePanel = panelsAPI.createLeftSizePanel();
let leftModifiedPanel = panelsAPI.createLeftModifiedPanel();

let buttons = buttonsAPI.createButtons();
buttons.forEach(button => { 
  screen.append(button);
});

screen.append(background);
screen.append(leftPanel);
screen.append(leftNamePanel);
screen.append(leftSizePanel);
screen.append(leftModifiedPanel);

screen.append(rightPanel);


// Quit on Escape, q, or Control-C.
screen.key(['q', 'C-c'], function(ch, key) {
  return process.exit(0);
});
// Focus our element.
leftPanel.focus();
// Render the screen.
screen.render();
