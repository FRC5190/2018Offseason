import { Command } from "./fieldobject";
import Field from "./field";
import { app, Menu, BrowserWindow, ipcMain } from 'electron';

const url = require('url');
const path = require('path');

var play: Boolean = false;
var field: Field;
var stopTimeout: NodeJS.Timer, periodicTimeout: NodeJS.Timer;
let mainWindow: Electron.BrowserWindow;

app.on('ready', function () {
    // initialize the main window
    mainWindow = new BrowserWindow({
        width: 1200,
        height: 1000
    });
    mainWindow.loadURL(url.format({
        pathname: path.join(__dirname, 'powerup.html'),
        protocol: 'file:',
        slashes: true
    }));
    mainWindow.on('closed', function () {
        stopGame();
        app.quit();
    })
    mainWindow.webContents.on('did-finish-load', function () {
        updateDisplay('newgame', new Field());
    })

    const mainMenu = Menu.buildFromTemplate(mainMenuTemplate);
    Menu.setApplicationMenu(mainMenu);
});

ipcMain.on('command:start', function (e: Event, command: string) {
    if (play) {
        if (command.charAt(0) == 'w') {
            field.robots[0].startCommand(Command.PICKUP, parseInt(command.substr(1)));
        }
        else if (command.charAt(0) == 'a') {
            field.robots[0].startCommand(Command.PLACE, parseInt(command.substr(1)));
        }
        else if (command.charAt(0) == 'l') {
            field.exchanges[0].startCommand(Command.LEVITATE);
        }
        else if (command.charAt(0) == 'f') {
            field.exchanges[0].startCommand(Command.FORCE);
        }
        else if (command.charAt(0) == 'b') {
            field.exchanges[0].startCommand(Command.BOOST);
        }
        else if (command.charAt(0) == 'v' && command.charAt(1) == 'l') {
            field.exchanges[0].startCommand(Command.PLAY_LEVITATE);
        }
        else if (command.charAt(0) == 'v' && command.charAt(1) == 'f') {
            field.exchanges[0].startCommand(Command.PLAY_FORCE);
        }
        else if (command.charAt(0) == 'v' && command.charAt(1) == 'b') {
            field.exchanges[0].startCommand(Command.PLAY_BOOST);
        }
    }
});

ipcMain.on('command:cancel', function (e: Event, command: string) {
    if (play) {
        field.robots[0].cancelCommand();
    }
});

function startGame() {
    // stop previous game if it is in progress
    stopGame();

    field = new Field();
    field.start(120);
    updateDisplay('newgame', field);

    // start the periodic loop    
    periodicTimeout = setInterval(function () {
        field.periodic(0);
        updateDisplay('periodic', field);
    }, 1000);

    // set the clock for timeout
    stopTimeout = setTimeout(function () {
        field.stop();
        updateDisplay('finishgame', field);
        stopGame();
    }, 240000);

    play = true;
}

function stopGame() {
    if (play) {
        clearTimeout(stopTimeout);
        clearInterval(periodicTimeout);
        play = false;
    }
}

function updateDisplay(command: string, field: Field) {
    mainWindow.webContents.send(command, field);
}

const mainMenuTemplate = [
    {
        label: 'File',
        submenu: [
            {
                label: "Start Game",
                accelerator: process.platform == 'darwin' ? 'Command+N' : 'Ctrl+N',
                click() {
                    startGame();
                }
            },
            {
                label: 'Quit',
                accelerator: process.platform == 'darwin' ? 'Command+Q' : 'Ctrl+Q',
                click() {
                    app.quit();
                }
            }
        ]
    },
    {
        label: 'Developer',
        submenu: [
            {
                label: "Toggle",
                accelerator: process.platform == 'darwin' ? 'Command+T' : 'Ctrl+T',
                click() {
                    mainWindow.webContents.toggleDevTools();
                }
            },
            {
                role: 'reload'
            }
        ]
    }
];
