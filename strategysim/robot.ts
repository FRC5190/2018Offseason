import {Score, Alliance, Command, FieldObject, Rectangle} from "./fieldobject";
import Field from "./field";
import { Strategy } from "./strategies/strategy";

export default class Robot extends FieldObject {
    currentCommand: Command = Command.NONE;
    currentCommandInput = 0;
    holdingCube = -1;
    speed = 6;

    constructor(id: number, x: number, y: number, alliance: Alliance, private field: Field, private strategy: Strategy) {
        super(id, new Rectangle(x, y, 37/12.0, 31/12.0), alliance);
    }

    startCommand(command:Command, commandInput = 0) {
        if (this.currentCommand != Command.NONE) {
            return;
        }

        this.currentCommand = command;
        this.currentCommandInput = commandInput;
    }

    cancelCommand() {
        this.currentCommand = Command.NONE;
    }

    pickCube(id: number) {
        if (id < 0 || id > this.field.cubes.length) {
            this.cancelCommand();
            return;
        }

        var cube = this.field.cubes[id];
        if (cube.ownership != -1) {
            this.cancelCommand();
            return;
        }
        
        var gotit = this.stepTo(cube, this.speed, undefined);
        if (gotit) {
            cube.ownership = this.id;
            this.holdingCube = cube.id;
            this.currentCommand = Command.NONE;
        }
    }

    placeCube(id: number) {
        if (id < 0 || id > 7) {
            this.cancelCommand();
            return;
        }

        if (this.holdingCube == -1) {
            this.cancelCommand();
            return;
        }

        var cube = this.field.cubes[this.holdingCube];
        var another: FieldObject;
        
        if (id >= 0 && id <= 5) {
            another = this.field.balances[Math.floor(id/2)].plates[id%2];
        }
        else {
            another = this.field.exchanges[id-6];
        }

        var gotit = this.stepTo(another, this.speed, cube);
        if (gotit) {
            this.holdingCube = -1;
            this.currentCommand = Command.NONE;
            cube.ownership = another.id;
        }
    }

    periodic(time: number) {
        super.periodic(time);

        if (this.id == 3 && this.currentCommand == Command.NONE) {
            // execute next command from strategy
            var cmd = this.strategy.suggest(this, time);
            if (!cmd.dependent && cmd.command != Command.NONE) {
                console.log(cmd.message);
            }
            this.currentCommand = cmd.command;
            this.currentCommandInput = cmd.commandParameter;
        } 
        
        if (this.currentCommand == Command.PICKUP) {
            this.pickCube(this.currentCommandInput);
        }
        else if (this.currentCommand == Command.PLACE) {
            this.placeCube(this.currentCommandInput);
        }
    }
}