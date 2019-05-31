package IRGenerator;

import IR.Label;
import IR.Operand;
import IR.SharedLabel;
import java.util.Stack;

/**
 * @author agrmv
 * */

class IRGenVisitorContext {
    private Operand retVal = null;
    private Label falseLabel = null;
    Stack<SharedLabel> breakLabels = new Stack<>();

    void setRetVal(Operand retVal) {
        this.retVal = retVal;

    }

    Operand getRetVal() {
        if (retVal == null) {
            return null; // silence error
        } else {
            Operand localRetVal = retVal;
            retVal = null;
            return localRetVal;
        }
    }

    void setFalseLabel(Label falseLabel) {

        this.falseLabel = falseLabel;

    }

    Label getFalseLabel() {
        if (falseLabel == null) {

            return null; // silence error
        } else {
            Label localFalseLabel = falseLabel;
            falseLabel = null;
            return localFalseLabel;
        }
    }
}
