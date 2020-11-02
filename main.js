const blessed = require('blessed');
const buttonsAPI = require('./buttons');
const panelsAPI = require('./panels');
const fsAPI = require('./filesystem');
 
// Create a screen object.
var screen = blessed.screen({
  smartCSR: true,
  autoPadding : true
});
 
var activeFm, inactiveFm;

screen.title = 'Midday Commander';
 
let background = blessed.box({
  top: '2%',
  left: 'center',
  width: '100%',
  height: '96%',
  style: {
    fg: 'white',
    bg: 'blue',
  }
});

let leftFm = panelsAPI.createLeftFM(screen)
let rightFm = panelsAPI.createRightFm(screen)

let buttons = buttonsAPI.createButtons();
buttons.forEach(button => { 
  screen.append(button);
});

screen.append(background);

screen.append(leftFm)
screen.append(rightFm)

// Quit on Escape, q, or Control-C.
screen.key(['q', 'C-c'], function(ch, key) {
  return process.exit(0);
});

screen.key(['tab'], function(ch, key) {
  var tempFm = activeFm
  activeFm = inactiveFm
  inactiveFm = tempFm
  activeFm.focus()
  panelsAPI.enableSelector(activeFm)
  panelsAPI.disableSelector(inactiveFm)
  activeFm.refresh()
  inactiveFm.refresh()
});

// Focus our element.
activeFm = leftFm;
inactiveFm = rightFm;

activeFm.focus()

panelsAPI.disableSelector(inactiveFm)
activeFm.refresh()
inactiveFm.refresh()

// Render the screen.
screen.render();
