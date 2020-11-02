const blessed = require('blessed');
const buttonsAPI = require('./buttons');
 
 
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

let leftPanel = blessed.box({
  top: '3%',
  left: 'left',
  width: '50%',
  height: '100%-3',
  content: 'Hello {bold}world{/bold}!',
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

let rightPanel = blessed.box({
  top: '3%',
  right: '0',
  width: '50%',
  height: '100%-3',
  content: 'Hello {bold}world{/bold}!',
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

let buttons = buttonsAPI.createButtons();
buttons.forEach(button => { 
  screen.append(button);
});

screen.append(background);
screen.append(leftPanel);
screen.append(rightPanel);


// Quit on Escape, q, or Control-C.
screen.key(['q', 'C-c'], function(ch, key) {
  return process.exit(0);
});
 
// Focus our element.
leftPanel.focus();
 
// Render the screen.
screen.render();
