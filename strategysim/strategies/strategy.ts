import Field from "../field";
import Robot from "../robot";
import {Alliance, Command, FieldObject, Rectangle} from "../fieldobject"

export class StrategyCommand {
    computedTime: number = 0;
    recommendedTime: number = 0;
    command: Command = Command.NONE;
    commandParameter: number = 0;
    dependent: Boolean = false;
    message: string = "";
    object: FieldObject | undefined;
}

export class Strategy {

    constructor(protected field: Field) {        
    }

    suggest(obj: FieldObject, time: number): StrategyCommand {
        return new StrategyCommand();
    }
}