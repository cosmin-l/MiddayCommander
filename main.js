var blessed = require('blessed');
 
// Create a screen object.
var screen = blessed.screen({
  smartCSR: true,
  autoPadding : true
});
 
screen.title = 'Iron Commander';
 
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


let buttonStylePrefix = '{black-fg}{white-bg}{bold}';
let buttonStyleSuffix = '{/bold}{/white-bg}{/black-fg}';


let f1Button = addFooterButton(buttonStylePrefix + 'F1' + buttonStyleSuffix + ' Help      ', 1);
screen.append(f1Button);

let f2Button = addFooterButton(buttonStylePrefix + 'F2' + buttonStyleSuffix + ' Menu        ', 10);
screen.append(f2Button);

let f3Button = addFooterButton(buttonStylePrefix + 'F3' + buttonStyleSuffix + ' View        ', 20);
screen.append(f3Button);

let f4Button = addFooterButton(buttonStylePrefix + 'F4' + buttonStyleSuffix + ' Edit       ', 30);
screen.append(f4Button);

let f5Button = addFooterButton(buttonStylePrefix + 'F5' + buttonStyleSuffix + ' Copy        ', 40);
screen.append(f5Button);

let f6Button = addFooterButton(buttonStylePrefix + 'F6' + buttonStyleSuffix + ' Move        ', 50);
screen.append(f6Button);

let f7Button = addFooterButton(buttonStylePrefix + 'F7' + buttonStyleSuffix + ' Mkdir      ', 60);
screen.append(f7Button);

let f8Button = addFooterButton(buttonStylePrefix + 'F8' + buttonStyleSuffix + ' Delete      ', 70);
screen.append(f8Button);

let f9Button = addFooterButton(buttonStylePrefix + 'F9' + buttonStyleSuffix + ' PullDn      ', 80);
screen.append(f9Button);

let f10Button = addFooterButton(buttonStylePrefix + 'F10' + buttonStyleSuffix + ' Quit       ', 90);
screen.append(f10Button);

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


function addFooterButton(text, pos){
  let footerText = blessed.text({
    content: text,
    top: '100%-1',
    height: '1',
    left:`${pos}%`,
    tags: true,
    style: {
      fg: 'white',
      bg: 'lightblue',
      border: {
        fg: '#f0f0f0'
      },
    }
  
  })
  return footerText;
}