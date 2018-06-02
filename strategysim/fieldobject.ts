import Field from "./field";

// top-left corner, width, and height define a rectangle
export class Rectangle {
    constructor(public x: number, public y: number, public w: number, public h: number) {        
    }
}

export enum Alliance {
    BLUE = 0,
    RED = 1,
    NONE = 2,
}

// to simplify driving
class Zone {
    constructor(public id = 0, public wx = 0, public wy = 0) {
    }
}

// scoring - year specific
export class Score {
    autorun = 0;
    ownership = 0;
    vault = 0;
    endgame = 0;
    penalty = 0;
    total = 0;

    add(child: Score) {
        this.autorun += child.autorun;
        this.ownership += child.ownership;
        this.vault += child.vault;
        this.endgame += child.endgame;
        this.penalty += child.penalty;
    }
}

// commands - year specific
export enum Command {
    NONE = 0,
    PICKUP = 1,
    PLACE = 2,
    FORCE = 3,
    LEVITATE = 4,
    BOOST = 5,
    PLAY_FORCE = 6,
    PLAY_LEVITATE = 7,
    PLAY_BOOST = 8
}

export class FieldObject {
    play: Boolean = true;    
    ownership = -1;                             // being carried or contained by another field object
    children: Array<FieldObject> = [];
    scores: Array<Score> = [new Score(), new Score()];
    time = 0;

    constructor(public id: number, public rect: Rectangle, public alliance: Alliance) {
    }

    in(another: FieldObject): Boolean {
        var bx1 = another.rect.x;
        var bx2 = another.rect.x + another.rect.w;
        var by1 = another.rect.y;
        var by2 = another.rect.y + another.rect.h;

        return (this.rect.x <= bx1 && this.rect.x + this.rect.w >= bx2 && this.rect.y <= by1 && this.rect.y + this.rect.h >= by2);
    }

    touches(another: FieldObject): Boolean {
        var bx1 = another.rect.x;
        var bx2 = another.rect.x + another.rect.w;
        var by1 = another.rect.y;
        var by2 = another.rect.y + another.rect.h;

        return (this.rect.x + this.rect.w > bx1 && this.rect.x < bx2 && this.rect.y + this.rect.h > by1 && this.rect.y < by2);
    }

    displace(x_disp: number, y_disp: number) {
        this.rect.x += x_disp;
        this.rect.y += y_disp;

        // caller needs to explicitly displace children and another holding object
    }

    // step towards the target while carrying another object
    stepTo(target: FieldObject, speed: number, carry?: FieldObject) : Boolean {
        if (!carry && this.touches(target)) {
            // we will assume that it is good enough for this object to touch target to complete the goal
            return true;
        }
        else if (carry && target.in(carry)) {
            // we will assume that the carried object has to be fully inside the target object to complete the goal
            return true;
        }
        else {
            // move towards the other object
            var targetzone = target.zone();
            var sourcezone;
            if (carry) {
                sourcezone = carry.zone();
            }
            else {
                sourcezone = this.zone();
            }

            if (Math.abs(targetzone.id - sourcezone.id) <= 1) {
                this.stepToHelper(target.rect.x, target.rect.y, speed, carry);
            }
            else {                
                if (!carry && this.rect.y != targetzone.wy) {
                    this.stepToHelper(sourcezone.wx, sourcezone.wy, speed, carry);
                }
                else if (carry && carry.rect.y != targetzone.wy) {
                    this.stepToHelper(sourcezone.wx, sourcezone.wy, speed, carry);
                }
                else {
                    this.stepToHelper(targetzone.wx, targetzone.wy, speed, carry);
                }
            }
            return false;
        }
    }

    private stepToHelper(x: number, y: number, speed: number, carry?: FieldObject) {
        var xd, yd;
        if (carry) {
            xd = x - carry.rect.x;
            yd = y - carry.rect.y;
        } 
        else {
            xd = x - this.rect.x;
            yd = y - this.rect.y;
        }

        var distance = Math.sqrt(xd*xd + yd*yd);
        var time = distance / speed;
        if (time < 1) {
            this.displace(xd, yd);            
            if (carry) {
                carry.displace(xd, yd);
            }
        }
        else {
            this.displace(xd / time, yd / time);
            if (carry) {
                carry.displace(xd / time, yd / time);
            }
        }
    }

    start(time: number) {
        this.time = time;
        this.children.forEach(it => {
            it.start(time);
        });
    }

    // called every second
    periodic(time: number) {
        // just pass on the same time to all children. we will assume that caller of the field decrements the time
        this.time = time;
        
        this.scores[0].vault = 0;
        this.scores[1].vault = 0;
        this.scores[0].endgame = 0;
        this.scores[1].endgame = 0;
        this.scores[0].total = 0;
        this.scores[1].total = 0;

        this.children.forEach(it => {
            it.periodic(time);
            this.scores[0].add(it.scores[0]);
            this.scores[1].add(it.scores[1]);
        });

        this.scores.forEach(it => {
            it.total = it.autorun + it.ownership + it.vault + it.endgame + it.penalty;
        })
    }

    stop() {
        this.children.forEach(it => {
            it.stop();
        });
    }

    // driving assistance - year specific
    zone(): Zone {
        var y = 3;

        if (this.rect.x >= 0 && this.rect.x < 11) {
            return new Zone(1, 5, y);
        }

        if (this.rect.x >= 11 && this.rect.x < 16) {
            return new Zone(2, 13, y);
        }

        if (this.rect.x >= 16 && this.rect.x < 24) {
            return new Zone(3, 20, y);
        }
        
        if (this.rect.x >= 24 && this.rect.x < 29) {
            return new Zone(4, 27, y);
        }

        if (this.rect.x >= 29 && this.rect.x < 37) {
            return new Zone(5, 33, y);
        }
        
        if (this.rect.x >= 37 && this.rect.x < 42) {
            return new Zone(6, 39, y);
        }

        if (this.rect.x >= 42 && this.rect.x <= 54) {
            return new Zone(7, 48, y);
        }

        return new Zone(0, 0, 0);
    }
}