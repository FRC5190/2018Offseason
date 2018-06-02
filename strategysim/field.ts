import {Rectangle, Alliance, FieldObject} from "./fieldobject";
import {Balance} from "./balance";
import {Exchange, VaultCommand, VaultCommandQueue} from "./exchange";
import Cube from "./cube";
import Robot from "./robot";
import { LinearStrategy } from "./strategies/linear";

export default class Field extends FieldObject {
    balances: Array<Balance>;
    exchanges: Array<Exchange>;
    cubes: Array<Cube>;
    robots: Array<Robot>;
    cmdQueue: VaultCommandQueue = new VaultCommandQueue();
    strategy: LinearStrategy;

    constructor() {
        super(0, new Rectangle(0, 0, 54, 27.0), Alliance.NONE);

        // generate a random gamedata sequence
        var g1 = Math.floor(Math.random() * 2)
        var g2 = Math.floor(Math.random() * 2)

        // strategy
        this.strategy = new LinearStrategy(this);
        
        this.balances = [
            new Balance(0, 11.75, 7, 4.5, 13.0, Alliance.BLUE, g1 == 0 ? Alliance.BLUE : Alliance.RED, this),
            new Balance(2, 37.75, 7, 4.5, 13.0, Alliance.RED, g1 == 0 ? Alliance.BLUE : Alliance.RED, this),
            new Balance(4, 25.0, 6, 4.0, 15.0, Alliance.NONE, g2 == 0 ? Alliance.BLUE : Alliance.RED, this)
        ];

        // exchanges
        this.exchanges = [
            new Exchange(6, 0, 8.5, Alliance.BLUE, this, this.strategy),
            new Exchange(7, 51, 14.5, Alliance.RED, this, this.strategy)
        ];

        // blue pile
        this.cubes = [
            new Cube(0, 10.5, 12.0, Alliance.BLUE, this),
            new Cube(1, 10.5, 13.0, Alliance.BLUE, this),
            new Cube(2, 10.5, 14.0, Alliance.BLUE, this),
            new Cube(3, 10.5, 12.5, Alliance.BLUE, this),
            new Cube(4, 10.5, 13.5, Alliance.BLUE, this),
            new Cube(5, 10.5, 13.0, Alliance.BLUE, this),
            new Cube(6, 9.5, 12.5, Alliance.BLUE, this),
            new Cube(7, 9.5, 13.5, Alliance.BLUE, this),
            new Cube(8, 9.5, 13.0, Alliance.BLUE, this),
            new Cube(9, 8.5, 13.0, Alliance.BLUE, this)
        ];

        // red pile
        this.cubes = this.cubes.concat([
            new Cube(10, 42.5, 12.0, Alliance.RED, this),
            new Cube(11, 42.5, 13.0, Alliance.RED, this),
            new Cube(12, 42.5, 14.0, Alliance.RED, this),
            new Cube(13, 42.5, 12.5, Alliance.RED, this),
            new Cube(14, 42.5, 13.5, Alliance.RED, this),
            new Cube(15, 42.5, 13.0, Alliance.RED, this),
            new Cube(16, 43.5, 12.5, Alliance.RED, this),
            new Cube(17, 43.5, 13.5, Alliance.RED, this),
            new Cube(18, 43.5, 13.0, Alliance.RED, this),
            new Cube(19, 44.5, 13.0, Alliance.RED, this)
        ]);

        // cubes behind the switch
        this.cubes = this.cubes.concat([
            new Cube(20, 16.5, 7.0, Alliance.NONE, this),
            new Cube(21, 16.5, 9.4, Alliance.NONE, this),
            new Cube(22, 16.5, 11.8, Alliance.NONE, this),
            new Cube(23, 16.5, 14.2, Alliance.NONE, this),
            new Cube(24, 16.5, 16.6, Alliance.NONE, this),
            new Cube(25, 16.5, 19.0, Alliance.NONE, this),
        ]);

        this.cubes = this.cubes.concat([
            new Cube(26, 36.5, 7.0, Alliance.NONE, this),
            new Cube(27, 36.5, 9.4, Alliance.NONE, this),
            new Cube(28, 36.5, 11.8, Alliance.NONE, this),
            new Cube(29, 36.5, 14.2, Alliance.NONE, this),
            new Cube(30, 36.5, 16.6, Alliance.NONE, this),
            new Cube(31, 36.5, 19.0, Alliance.NONE, this),
        ]);

        // portal cubes
        for (var i = 0; i < 7; i++) {
            this.cubes = this.cubes.concat([
                new Cube(32 + i, 0.5, 0.5, Alliance.RED, this),
            ]);
        }

        for (var i = 0; i < 7; i++) {
            this.cubes = this.cubes.concat([
                new Cube(39 + i, 0.5, 25.5, Alliance.RED, this),
            ]);
        }

        for (var i = 0; i < 7; i++) {
            this.cubes = this.cubes.concat([
                new Cube(46 + i, 52.5, 0.5, Alliance.BLUE, this),
            ]);
        }

        for (var i = 0; i < 7; i++) {
            this.cubes = this.cubes.concat([
                new Cube(53 + i, 52.5, 25.5, Alliance.BLUE, this)
            ]);
        }

        // robots
        this.robots = [
            new Robot(0, 0, 3, Alliance.BLUE, this, this.strategy),
            new Robot(1, 0, 13, Alliance.BLUE, this, this.strategy),
            new Robot(2, 0, 24 - 31/12.0, Alliance.BLUE, this, this.strategy),
            new Robot(3, 54 - 37/12.0, 3, Alliance.RED, this, this.strategy),
            new Robot(4, 54 - 37/12.0, 14 - 31/12.0, Alliance.RED, this, this.strategy),
            new Robot(5, 54 - 37/12.0, 24 - 31/12.0, Alliance.RED, this, this.strategy),
        ];

        this.children = this.children.concat(this.balances);
        this.children = this.children.concat(this.exchanges);
        this.children = this.children.concat(this.cubes);
        this.children = this.children.concat(this.robots);
    }

    // override start periodic to generate times
    start(time: number) {
        this.time = time;
        super.start(this.time);
    }

    periodic(time: number) {
        if (this.time <= 0) {
            return;
        }
        
        this.time--;
        super.periodic(this.time);
    }
}