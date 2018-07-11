/*
 * FRC Team 5190
 * Green Hope Falcons
 */

import {Rectangle, Score, Alliance, FieldObject} from "./fieldobject";
import Field from "./field";

export default class Cube extends FieldObject {
    constructor(id = 0, x: number, y: number, alliance: Alliance, private field: Field) {
        super(id, new Rectangle(x, y, 1, 1), alliance);
    }
}