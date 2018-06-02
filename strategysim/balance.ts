import {Rectangle, Score, Command, Alliance, FieldObject} from "./fieldobject";
import Field from "./field";
import {VaultCommand} from "./exchange";

export class Plate extends FieldObject {
    constructor(id = 0, x: number, y: number, alliance: Alliance) {
        super(id, new Rectangle(x, y, 4, 3), alliance);
    }
}

export class Balance extends FieldObject {

    plates: Plate[];
    cubeCount: number[] = [0, 0];
 
    constructor(id: number, x: number, y: number, w: number, h: number, alliance: Alliance, private leftPlate: Alliance, private field: Field) {
        super(id, new Rectangle(x, y, w, h), alliance)

        if (this.alliance == Alliance.BLUE || this.alliance == Alliance.RED) { // switch
            if (this.leftPlate == Alliance.BLUE) {
                this.plates = [
                    new Plate(id, x+0.25, y+0.25, Alliance.BLUE),
                    new Plate(id + 1, x+0.25, y+h-3.25, Alliance.RED)
                ]
            }
            else {
                this.plates = [
                    new Plate(id, x+0.25, y+h-3.25, Alliance.BLUE),
                    new Plate(id + 1, x+0.25, y+0.25, Alliance.RED)
                ]                
            }
        }
        else {
            if (this.leftPlate == Alliance.BLUE) {
                this.plates = [
                    new Plate(id, x, y, Alliance.BLUE),
                    new Plate(id + 1, x, y+h-3, Alliance.RED)
                ]    
            }
            else {
                this.plates = [
                    new Plate(id, x, y+h-3, Alliance.BLUE),
                    new Plate(id + 1, x, y, Alliance.RED)
                ]
            }
        }

        this.children = this.children.concat(this.plates);
    }

    periodic(time: number) {
        super.periodic(time);
        
        this.cubeCount = [0, 0];

        this.field.cubes.forEach(it => {
            this.plates.forEach(pt => {
                if (pt.in(it)) {
                    it.ownership = pt.id;
                    this.cubeCount[pt.alliance]++;
                }                    
            })
        });

        if (this.cubeCount[0] > this.cubeCount[1]) {
            this.scores[0].ownership = 1;
            this.scores[1].ownership = 0;
        }
        else if (this.cubeCount[1] > this.cubeCount[0]) {
            this.scores[0].ownership = 0;
            this.scores[1].ownership = 1;
        }
        else {
            this.scores[0].ownership = 0;
            this.scores[1].ownership = 0;            
        }

        // ownership may be overwritten because of force play in vault
        var nc = this.field.cmdQueue.vaultCommandAtPlay;
        if (nc.command == Command.NONE) {
            return;
        }

        if (nc.command == Command.PLAY_FORCE) {
            if (((nc.numCubes == 1 || nc.numCubes == 3) && (this.alliance == Alliance.BLUE || this.alliance == Alliance.RED) && (nc.alliance == this.alliance))  // 1 or 3 cubes in force & this balance is a switch
                || ((nc.numCubes == 2 || nc.numCubes == 3) && (this.alliance == Alliance.NONE))) // 2 or 3 cubes in force & this balance is a scale {
            {
                this.scores[nc.alliance].ownership = 1;
                this.scores[1 - nc.alliance].ownership = 0;
            }
        }
        else if (nc.command == Command.PLAY_BOOST) {
            if (((nc.numCubes == 1 || nc.numCubes == 3) && (this.alliance == Alliance.BLUE || this.alliance == Alliance.RED) && (nc.alliance == this.alliance)) 
                || ((nc.numCubes == 2 || nc.numCubes == 3) && (this.alliance == Alliance.NONE))) {
                if (this.scores[nc.alliance].ownership == 1) {
                    this.scores[nc.alliance].ownership = (nc.numCubes == 3 ? 3 : 2);
                }
            }
        }
    }
}