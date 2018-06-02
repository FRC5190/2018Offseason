import Field from "../field";
import Cube from "../cube";
import Robot from "../robot";
import {Alliance, Command, FieldObject} from "../fieldobject"
import {StrategyCommand, Strategy} from "./strategy"
import { Exchange } from "../exchange";

export class LinearStrategy extends Strategy {

    pendingCommands: Array<StrategyCommand> = [];
    finishedCommands: Array<StrategyCommand> = [];

    constructor(field: Field) {
        super(field);
    }

    private createStrategyCommand(command: Command, commandParameter: number, object: FieldObject, time: number): StrategyCommand {
        var c = new StrategyCommand();
        c.command = command;
        c.commandParameter = commandParameter;
        c.computedTime = time;
        c.object = object;
        if (c.command == Command.PLACE) {
            c.dependent = true;
        }
        else {
            c.dependent = false;
        }

        return c;
    }

    suggest(obj: FieldObject, time: number): StrategyCommand {
        // if there is something in the recommendations already, then give it
        if (this.pendingCommands.length > 0 && this.pendingCommands[0].object == obj) {
            var c = this.pendingCommands.shift();
            if (c) {
                c.recommendedTime = time;
                this.finishedCommands.push(c);
                return c;
            }
        }

        if (obj instanceof Robot) {
            // goal 1 - own the switch
            if (this.field.balances[obj.alliance].cubeCount[obj.alliance] - this.field.balances[obj.alliance].cubeCount[1 - obj.alliance] < 1) {
                var nc = this.selectCube(obj);
                if (nc) {
                    var nc1 = this.createStrategyCommand(Command.PICKUP, nc.id, obj, time);
                    nc1.message = "Goal 1: Cube <" + nc.id + "> -> Switch <" + this.field.balances[obj.alliance].plates[obj.alliance].id + ">";
                    this.finishedCommands.push(nc1);

                    var nc2 = this.createStrategyCommand(Command.PLACE, this.field.balances[obj.alliance].plates[obj.alliance].id, obj, time);
                    this.pendingCommands.push(nc2);

                    return nc1;
                }
            }

            // goal 2 - own the scale
            if (this.field.balances[2].cubeCount[obj.alliance] - this.field.balances[2].cubeCount[1 - obj.alliance] < 1) {
                var nc = this.selectCube(obj);
                if (nc) {
                    var nc1 = this.createStrategyCommand(Command.PICKUP, nc.id, obj, time);
                    nc1.message = "Goal 2: Cube <" + nc.id + "> -> Scale <" + this.field.balances[2].plates[obj.alliance].id + ">";
                    this.finishedCommands.push(nc1);

                    var nc2 = this.createStrategyCommand(Command.PLACE, this.field.balances[2].plates[obj.alliance].id, obj, time);
                    this.pendingCommands.push(nc2);

                    return nc1;
                }
            }        

            // goal 3 - protect the switch
            if (this.field.balances[obj.alliance].cubeCount[obj.alliance] - this.field.balances[obj.alliance].cubeCount[1 - obj.alliance] < 2) {
                var nc = this.selectCube(obj);
                if (nc) {
                    var nc1 = this.createStrategyCommand(Command.PICKUP, nc.id, obj, time);
                    nc1.message = "Goal 3: Cube <" + nc.id + "> -> Switch <" + this.field.balances[obj.alliance].plates[obj.alliance].id + ">";
                    this.finishedCommands.push(nc1);

                    var nc2 = this.createStrategyCommand(Command.PLACE, this.field.balances[obj.alliance].plates[obj.alliance].id, obj, time);
                    this.pendingCommands.push(nc2);

                    return nc1;
                }
            }

            // goal 4.1 - boost w3
            if (this.field.exchanges[obj.alliance].cubeCount() < 3) {
                var nc = this.selectCube(obj);
                if (nc) {
                    var nc1 = this.createStrategyCommand(Command.PICKUP, nc.id, obj, time);
                    nc1.message = "Goal 4: Cube <" + nc.id + "> -> Exchange <" + this.field.exchanges[obj.alliance].id + ">";
                    this.finishedCommands.push(nc1);

                    var nc2 = this.createStrategyCommand(Command.PLACE, this.field.exchanges[obj.alliance].id, obj, time);
                    this.pendingCommands.push(nc2);

                    return nc1;
                }
            }

            // goal 5.1 - levitate w3
            if (this.field.exchanges[obj.alliance].cubeCount() < 6) {
                var nc = this.selectCube(obj);
                if (nc) {
                    var nc1 = this.createStrategyCommand(Command.PICKUP, nc.id, obj, time);
                    nc1.message = "Goal 5: Cube <" + nc.id + "> -> Exchange <" + this.field.exchanges[obj.alliance].id + ">";
                    this.finishedCommands.push(nc1);

                    var nc2 = this.createStrategyCommand(Command.PLACE, this.field.exchanges[obj.alliance].id, obj, time);
                    this.pendingCommands.push(nc2);

                    return nc1;
                }
            }
        }

        if (obj instanceof Exchange) {
            // goal 4.2 - boost w3
            if ((this.field.exchanges[obj.alliance].cubeCount() >= 3) &&
                (this.field.exchanges[obj.alliance].boostCubes < 3)) {
                var nc1 = this.createStrategyCommand(Command.BOOST, 0, obj, time);
                nc1.message = "Goal 4: Load Boost";
                this.finishedCommands.push(nc1);
                return nc1;
            }

            // goal 4.3 - boost w3
            if ((this.field.exchanges[obj.alliance].cubeCount() >= 3) &&
                (!this.field.exchanges[obj.alliance].boostPlayed)) {
                var nc1 = this.createStrategyCommand(Command.PLAY_BOOST, 0, obj, time);
                nc1.message = "Goal 4: Play Boost";
                this.finishedCommands.push(nc1);
                return nc1;
            }

            // goal 5.2 - levitate w3
            if ((this.field.exchanges[obj.alliance].cubeCount() >= 6) &&
                (this.field.exchanges[obj.alliance].levitateCubes < 3)) {
                var nc1 = this.createStrategyCommand(Command.LEVITATE, 0, obj, time);
                nc1.message = "Goal 5: Load Levitate";
                this.finishedCommands.push(nc1);
                return nc1;
            }

            // goal 5.3 - levitate w3
            if ((this.field.exchanges[obj.alliance].cubeCount() >= 6) &&
                (!this.field.exchanges[obj.alliance].levitatePlayed)) {
                var nc1 = this.createStrategyCommand(Command.PLAY_LEVITATE, 0, obj, time);
                nc1.message = "Goal 6: Play Levitate";
                this.finishedCommands.push(nc1);
                return nc1;
            }
        }

        return new StrategyCommand();
    }

    private selectCube(robot: Robot) : Cube | undefined {
        // find a cube that is not in my alliance
        var cubes = this.field.cubes.filter(it => {
            return (it.alliance == Alliance.NONE || it.alliance == robot.alliance) && it.ownership == -1;
        }).sort(function(first, second): number {
            var d1 = Math.abs(first.zone().id - robot.zone().id);
            var d2 = Math.abs(second.zone().id - robot.zone().id);
            if (d1 < d2) return -1;
            if (d1 > d2) return 1;
            return 0;
        });

        if (cubes.length > 0) {
            return cubes.shift();
        }
    }
}