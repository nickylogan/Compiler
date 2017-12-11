package statement;

import javafx.util.Pair;
import main.*;

import java.util.ArrayList;

@SuppressWarnings("Duplicates")
public class IfNode extends StatementNode {
    private Token operator;
    private MultipleStatementNode trueChild;
    private MultipleStatementNode falseChild;

    public IfNode(String line, int lineNumber) {
        setLine(line);
        setLineNumber(lineNumber);
        trueChild = new MultipleStatementNode();
        trueChild.setParent(this);
    }

    @Override
    public ArrayList<InstructionOffset> parse() {
        String s = getLine();
        String[] ss = s.split("(?=if)|(?<=if)|(?=[><!()]|(?<![<>=!])=)|(?<=[()=<>!])(?!=)");
        Boolean aBoolean = null;
        Instruction helper1 = null;
        Instruction helper2 = null;
        Instruction main = null;
        Boolean deterministic = false;
        Immediate ifLabel = new Immediate(0);
        Immediate endLabel = new Immediate(0);
        Instruction exit = new Instruction(Operator.JMP, endLabel);
        if (ss.length == 6) {
            operator = Token.getToken(ss[3]);
            Operator op = null;
            switch (operator) {
                case EQ:
                case NEQ:
                    op = Operator.JE;
                    break;
                case GT:
                case LE:
                    op = Operator.JGT;
                    break;
                case LT:
                case GE:
                    op = Operator.JLT;
                    break;
            }

            if (ss[2].matches("[0-9]+") && ss[4].matches("[0-9]+")) {
                deterministic = true;
                int l = Integer.parseInt(ss[2]);
                int r = Integer.parseInt(ss[4]);
                switch (operator) {
                    case EQ:
                        aBoolean = l == r;
                        break;
                    case NEQ:
                        aBoolean = l != r;
                        break;
                    case GE:
                        aBoolean = l >= r;
                        break;
                    case GT:
                        aBoolean = l > r;
                        break;
                    case LE:
                        aBoolean = l <= r;
                        break;
                    case LT:
                        aBoolean = l < r;
                        break;
                    default:
                        aBoolean = false;
                }
            } else if (Register.isRegister(ss[2]) != -1 && Register.isRegister(ss[4]) != -1) {
                Register r1 = Register.valueOf(ss[2]);
                Register r2 = Register.valueOf(ss[4]);
                if (Register.isReserved(r1) || Register.isReserved(r2))
                    throw new ParserException("Use of reserved register at line " + (getLineNumber() + 1));
                main = new Instruction(op, r1, r2, ifLabel);
            } else if (Register.isRegister(ss[2]) != -1 && ss[4].matches("[0-9]+")) {
                Register r1 = Register.valueOf(ss[2]);
                if (Register.isReserved(r1))
                    throw new ParserException("Use of reserved register at line " + (getLineNumber() + 1));
                helper1 = new Instruction(Operator.MOVI, Register.R14, new Immediate(Integer.parseInt(ss[4])));
                main = new Instruction(op, r1, Register.R14, ifLabel);
            } else if (Register.isRegister(ss[2]) != -1 && ss[4].matches("[A-Za-z][A-Za-z0-9]*")) {
                Register r1 = Register.valueOf(ss[2]);
                if (Register.isReserved(r1))
                    throw new ParserException("Use of reserved register at line " + (getLineNumber() + 1));
                VariableLocation varLocation = Parser.initVariable(ss[4]);
                Memory mem = new Memory(Register.R15, new Immediate(varLocation));
                helper1 = new Instruction(Operator.MOVM, Register.R14, mem);
                main = new Instruction(op, r1, Register.R14, ifLabel);
            } else if (ss[2].matches("[A-Za-z][A-Za-z0-9]*") && Register.isRegister(ss[4]) != -1) {
                Register r1 = Register.valueOf(ss[4]);
                if (Register.isReserved(r1))
                    throw new ParserException("Use of reserved register at line " + (getLineNumber() + 1));
                VariableLocation varLocation = Parser.initVariable(ss[2]);
                Memory mem = new Memory(Register.R15, new Immediate(varLocation));
                helper1 = new Instruction(Operator.MOVM, Register.R14, mem);
                main = new Instruction(op, Register.R14, r1, ifLabel);
            } else if (ss[2].matches("[A-Za-z][A-Za-z0-9]*") && ss[4].matches("[0-9]+")) {
                VariableLocation varLocation = Parser.initVariable(ss[2]);
                Memory mem = new Memory(Register.R15, new Immediate(varLocation));
                helper1 = new Instruction(Operator.MOVM, Register.R13, mem);
                helper2 = new Instruction(Operator.MOVI, Register.R14, new Immediate(Integer.parseInt(ss[4])));
                main = new Instruction(op, Register.R13, Register.R14, ifLabel);
            } else if (ss[2].matches("[A-Za-z][A-Za-z0-9]*") && ss[4].matches("[A-Za-z][A-Za-z0-9]*")) {
                VariableLocation varLocation1 = Parser.initVariable(ss[2]);
                Memory mem1 = new Memory(Register.R15, new Immediate(varLocation1));
                VariableLocation varLocation2 = Parser.initVariable(ss[4]);
                Memory mem2 = new Memory(Register.R15, new Immediate(varLocation2));
                helper1 = new Instruction(Operator.MOVM, Register.R13, mem1);
                helper2 = new Instruction(Operator.MOVM, Register.R14, mem2);
                main = new Instruction(op, Register.R13, Register.R14, ifLabel);
            } else if (ss[2].matches("[0-9]+") && Register.isRegister(ss[4]) != -1) {
                Register r1 = Register.valueOf(ss[4]);
                if (Register.isReserved(r1))
                    throw new ParserException("Use of reserved register at line " + (getLineNumber() + 1));
                helper1 = new Instruction(Operator.MOVI, Register.R14, new Immediate(Integer.parseInt(ss[2])));
                main = new Instruction(op, Register.R14, r1, ifLabel);
            } else if (ss[2].matches("[0-9]+") && ss[4].matches("[A-Za-z][A-Za-z0-9]*")) {
                VariableLocation varLocation = Parser.initVariable(ss[4]);
                Memory mem = new Memory(Register.R15, new Immediate(varLocation));
                helper1 = new Instruction(Operator.MOVI, Register.R13, new Immediate(Integer.parseInt(ss[4])));
                helper2 = new Instruction(Operator.MOVM, Register.R14, mem);
                main = new Instruction(op, Register.R13, Register.R14, ifLabel);
            } else throw new ParserException("invalid conditional statement at line " + (getLineNumber() + 1));
        } else if (ss.length == 4) {
            if (!ss[2].equals("true") && !ss[2].equals("false"))
                throw new ParserException("invalid conditional statement at line " + (getLineNumber() + 1));
            aBoolean = Boolean.valueOf(ss[2]);
            deterministic = true;
        } else
            throw new ParserException("invalid if statement at line " + (getLineNumber() + 1));
        int index = 0;
        ArrayList<InstructionOffset> res = new ArrayList<>();
        if (deterministic) {
            if (aBoolean) {
                ArrayList<InstructionOffset> trueArr = trueChild.parse();
                for (InstructionOffset instructionOffset : trueArr) {
                    instructionOffset.setOffset(instructionOffset.getOffset() + index);
                }
                index += trueArr.size();
                res.addAll(trueArr);
            } else {
                ArrayList<InstructionOffset> falseArr = falseChild.parse();
                for (InstructionOffset instructionOffset : falseArr) {
                    instructionOffset.setOffset(instructionOffset.getOffset() + index);
                }
                index += falseArr.size();
                res.addAll(falseArr);
            }
        } else {
            if (helper1 != null) res.add(new InstructionOffset(helper1, index++));
            if (helper2 != null) res.add(new InstructionOffset(helper2, index++));
            res.add(new InstructionOffset(main, index++));
            if (operator == Token.GE || operator == Token.LE || operator == Token.NEQ) {
                ArrayList<InstructionOffset> trueArr = trueChild.parse();
                for (InstructionOffset instructionOffset : trueArr) {
                    instructionOffset.setOffset(instructionOffset.getOffset() + index);
                }
                index += trueArr.size();
                res.addAll(trueArr);
                res.add(new InstructionOffset(exit, index++));
                ifLabel.setValue(index);
                ArrayList<InstructionOffset> falseArr = falseChild.parse();
                for (InstructionOffset instructionOffset : falseArr) {
                    instructionOffset.setOffset(instructionOffset.getOffset() + index);
                }
                index += falseArr.size();
                res.addAll(falseArr);
                endLabel.setValue(index);
            } else {
                ArrayList<InstructionOffset> falseArr = falseChild.parse();
                for (InstructionOffset instructionOffset : falseArr) {
                    instructionOffset.setOffset(instructionOffset.getOffset() + index);
                }
                index += falseArr.size();
                res.addAll(falseArr);
                res.add(new InstructionOffset(exit, index++));
                ifLabel.setValue(index);
                ArrayList<InstructionOffset> trueArr = trueChild.parse();
                for (InstructionOffset instructionOffset : trueArr) {
                    instructionOffset.setOffset(instructionOffset.getOffset() + index);
                }
                index += trueArr.size();
                res.addAll(trueArr);
                endLabel.setValue(index);
            }
        }
        return res;
    }

    public MultipleStatementNode createFalseChild() {
        falseChild = new MultipleStatementNode();
        falseChild.setParent(this);
        return falseChild;
    }

    public MultipleStatementNode getFalseChild() {
        return falseChild;
    }

    public MultipleStatementNode getTrueChild() {
        return trueChild;
    }
}
