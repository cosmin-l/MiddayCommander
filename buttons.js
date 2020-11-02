var blessed = require('blessed');

const createButtons = () => {
    let buttonStylePrefix = '{black-fg}{white-bg}{bold}';
    let buttonStyleSuffix = '{/bold}{/white-bg}{/black-fg}';
    let buttons = [];

    let f1Button = addFooterButton(buttonStylePrefix + 'F1' + buttonStyleSuffix + ' Help      ', 1);
    let f2Button = addFooterButton(buttonStylePrefix + 'F2' + buttonStyleSuffix + ' Menu        ', 10);
    let f3Button = addFooterButton(buttonStylePrefix + 'F3' + buttonStyleSuffix + ' View        ', 20);
    let f4Button = addFooterButton(buttonStylePrefix + 'F4' + buttonStyleSuffix + ' Edit       ', 30);
    let f5Button = addFooterButton(buttonStylePrefix + 'F5' + buttonStyleSuffix + ' Copy        ', 40);
    let f6Button = addFooterButton(buttonStylePrefix + 'F6' + buttonStyleSuffix + ' Move        ', 50);
    let f7Button = addFooterButton(buttonStylePrefix + 'F7' + buttonStyleSuffix + ' Mkdir      ', 60);
    let f8Button = addFooterButton(buttonStylePrefix + 'F8' + buttonStyleSuffix + ' Delete      ', 70);
    let f9Button = addFooterButton(buttonStylePrefix + 'F9' + buttonStyleSuffix + ' PullDn      ', 80);
    let f10Button = addFooterButton(buttonStylePrefix + 'F10' + buttonStyleSuffix + ' Quit       ', 90);

    buttons.push(f1Button);
    buttons.push(f2Button);
    buttons.push(f3Button);
    buttons.push(f4Button);
    buttons.push(f5Button);
    buttons.push(f6Button);
    buttons.push(f7Button);
    buttons.push(f8Button);
    buttons.push(f9Button);
    buttons.push(f10Button);

    return buttons;
}

const addFooterButton = (text, pos) => {
    let footerText = blessed.text({
      content: text,
      top: '100%-1',
      height: '1',
      left:`${pos}%`,
      tags: true,
      style: {
        fg: 'white',
        bg: 'green',
        border: {
          fg: '#f0f0f0'
        },
      }
    
    })
    return footerText;
}

exports.createButtons = createButtons;