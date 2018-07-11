/*
 * FRC Team 5190
 * Green Hope Falcons
 */

import {Rectangle, Score, Command, Alliance, FieldObject} from "./fieldobject";
import Field from "./field";
import Cube from "./cube";
import { Strategy } from "./strategies/strategy";


export class VaultCommand {
    command: Command = Command.NONE;
    alliance: Alliance = Alliance.NONE;
    timeQueued = 0;
    timeHandled = 0;
    numCubes = 0;
}

export class VaultCommandQueue {
    pendingCommands: Array<VaultCommand> = [];
    finishedCommands: Array<VaultCommand> = [];
    vaultCommandAtPlay: VaultCommand = new VaultCommand();

    alreadyPlayed(command: Command, alliance: Alliance) : Boolean {
        this.pendingCommands.forEach(it => {
            if (it.command == command && it.alliance == alliance) {
                return true;
            }
        })

        this.finishedCommands.forEach(it => {
            if (it.command == command && it.alliance == alliance) {
                return true;
            }
        })

        return false;
    }

    add(vc: VaultCommand) {
        if (!this.alreadyPlayed(vc.command, vc.alliance)) {
            this.pendingCommands.push(vc);
        }
    }

    handle(alliance: Alliance, time: number) {
        if (this.vaultCommandAtPlay.command == Command.NONE && this.pendingCommands.length > 0) {            
            var nc = this.pendingCommands[0];
            if (nc.alliance == alliance) {
                // move the command into finished
                nc.timeHandled = time;
                this.pendingCommands.shift();
                this.finishedCommands.push(nc);
                this.vaultCommandAtPlay = nc;
            }
        } else if (this.vaultCommandAtPlay.command != Command.NONE && this.vaultCommandAtPlay.alliance == alliance && this.vaultCommandAtPlay.timeHandled - time >= 10) {
            // we are done with 10s of this command
            this.vaultCommandAtPlay = new VaultCommand();
        }
    }
}

export class Exchange extends FieldObject {
    cubes: Array<Cube> = [];
    forceCubes = 0;
    levitateCubes = 0;
    boostCubes = 0;

    levitatePlayed = false;
    boostPlayed = false;
    forcePlayed = false;

    constructor(id: number, x: number, y: number, alliance: Alliance, private field: Field, private strategy: Strategy) {
        super(id, new Rectangle(x, y, 3, 4), alliance)
    }

    startCommand(command:Command) {
        if (command == Command.FORCE && this.forceCubes < 3) {
            var remove = this.cubes.pop();
            if (remove) {
                this.forceCubes++;
                remove.displace(0, -30);
            }
        }
        else if (command == Command.LEVITATE && this.levitateCubes < 3) {
            var remove = this.cubes.pop();
            if (remove) {
                this.levitateCubes++;
                remove.displace(0, -30);
            }
        }
        else if (command == Command.BOOST && this.boostCubes < 3) {
            var remove = this.cubes.pop();
            if (remove) {
                this.boostCubes++;
                remove.displace(0, -30);
            }
        }
        else if (command == Command.PLAY_FORCE || command == Command.PLAY_BOOST) {
            // each command can only be played once
            var vc = new VaultCommand();
            vc.alliance = this.alliance;
            vc.timeQueued = this.time;
            vc.command = command;
            if (command == Command.PLAY_FORCE) {
                vc.numCubes = this.forceCubes;
            } else if (command == Command.PLAY_BOOST) {
                vc.numCubes = this.boostCubes;
            }

            if (command == Command.PLAY_BOOST) {
                this.boostPlayed = true;
            }
            else {
                this.forcePlayed = true;
            }

            this.field.cmdQueue.add(vc);
        }
        else if (command == Command.PLAY_LEVITATE) {
            if (this.levitateCubes == 3) {
                this.levitatePlayed = true;
            }
        }
    }

    cubeCount() : number {
        return this.cubes.length + this.forceCubes + this.levitateCubes + this.boostCubes;
    }

    periodic(time: number) {
        super.periodic(time);

        this.cubes = [];    
        this.field.cubes.forEach(it => {
            if (this.in(it)) {
                it.ownership = this.id;
                this.cubes.push(it);
            }
        });

        // check if there is any command to execute from strategy
        var cmd = this.strategy.suggest(this, time);
        if (!cmd.dependent && cmd.command != Command.NONE) {
            console.log(cmd.message);
            this.startCommand(cmd.command);
        }

        // handle pending commands queue
        this.field.cmdQueue.handle(this.alliance, this.time);

        // score
        this.scores[this.alliance].vault = this.forceCubes * 5;
        this.scores[this.alliance].vault += this.boostCubes * 5;
        this.scores[this.alliance].vault += this.levitateCubes * 5;

        if (time < 15 && this.levitatePlayed) {
            this.scores[this.alliance].endgame = 30;
        }
    }
}