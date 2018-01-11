package statement;

import main.*;

import java.util.ArrayList;

@SuppressWarnings("Duplicates")
public class WhileNode extends StatementNode {
    private Token operator;
    private MultipleStatementNode children;

    public WhileNode(String line, int lineNumber) {
        setLine(line);
        setLineNumber(lineNumber);
        children = new MultipleStatementNode();
        children.setParent(this);
    }

    @Override
    public ArrayList<InstructionOffset> parse() throws ParserException {
        String s = getLine();
        String[] ss = s.split("(?=while)|(?<=while)|(?=[><!()]|(?<![<>=!])=)|(?<=[()=<>!])(?!=)");
        Boolean aBoolean = null;
        Instruction helper1 = null;
        Instruction helper2 = null;
        Instruction main = null;
        Boolean deterministic = false;
        Immediate whileLabel = new Immediate(0);
        Immediate whileStartLabel = new Immediate(0);
        Immediate endLabel = new Immediate(0);
        Instruction exit = new Instruction(Operator.JMP, endLabel);
        Instruction repeat = new Instruction(Operator.JMP, whileStartLabel);
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

            boolean normal = !(operator == Token.NEQ || operator == Token.GE || operator == Token.LE);
//            System.out.println("normal = " + normal);

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
//                System.out.println(ss[2]+ " "+ss[4]);
                Register r1 = Register.getValue(ss[2]);
                Register r2 = Register.getValue(ss[4]);
                if (Register.isReserved(r1) || Register.isReserved(r2))
                    throw new ParserException("Use of reserved register at line " + (getLineNumber()));
                main = new Instruction(op, r1, r2, normal ? whileLabel : endLabel);
            } else if (Register.isRegister(ss[2]) != -1 && ss[4].matches("[0-9]+")) {
                Register r1 = Register.getValue(ss[2]);
                if (Register.isReserved(r1))
                    throw new ParserException("Use of reserved register at line " + (getLineNumber()));
                helper1 = new Instruction(Operator.MOVI, Register.R14, new Immediate(Integer.parseInt(ss[4])));
                main = new Instruction(op, r1, Register.R14, normal ? whileLabel : endLabel);
            } else if (Register.isRegister(ss[2]) != -1 && ss[4].matches("[A-Za-z][A-Za-z0-9]*")) {
                Register r1 = Register.getValue(ss[2]);
                if (Register.isReserved(r1))
                    throw new ParserException("Use of reserved register at line " + (getLineNumber()));
                VariableLocation varLocation = Parser.initVariable(ss[4]);
                Memory mem = new Memory(Register.R15, new Immediate(varLocation));
                helper1 = new Instruction(Operator.MOVM, Register.R14, mem);
                main = new Instruction(op, r1, Register.R14, normal ? whileLabel : endLabel);
            } else if (ss[2].matches("[A-Za-z][A-Za-z0-9]*") && Register.isRegister(ss[4]) != -1) {
                Register r1 = Register.getValue(ss[4]);
                if (Register.isReserved(r1))
                    throw new ParserException("Use of reserved register at line " + (getLineNumber()));
                VariableLocation varLocation = Parser.initVariable(ss[2]);
                Memory mem = new Memory(Register.R15, new Immediate(varLocation));
                helper1 = new Instruction(Operator.MOVM, Register.R14, mem);
                main = new Instruction(op, Register.R14, r1, normal ? whileLabel : endLabel);
            } else if (ss[2].matches("[A-Za-z][A-Za-z0-9]*") && ss[4].matches("[0-9]+")) {
                VariableLocation varLocation = Parser.initVariable(ss[2]);
                Memory mem = new Memory(Register.R15, new Immediate(varLocation));
                helper1 = new Instruction(Operator.MOVM, Register.R13, mem);
                helper2 = new Instruction(Operator.MOVI, Register.R14, new Immediate(Integer.parseInt(ss[4])));
                main = new Instruction(op, Register.R13, Register.R14, normal ? whileLabel : endLabel);
            } else if (ss[2].matches("[A-Za-z][A-Za-z0-9]*") && ss[4].matches("[A-Za-z][A-Za-z0-9]*")) {
                VariableLocation varLocation1 = Parser.initVariable(ss[2]);
                Memory mem1 = new Memory(Register.R15, new Immediate(varLocation1));
                VariableLocation varLocation2 = Parser.initVariable(ss[4]);
                Memory mem2 = new Memory(Register.R15, new Immediate(varLocation2));
                helper1 = new Instruction(Operator.MOVM, Register.R13, mem1);
                helper2 = new Instruction(Operator.MOVM, Register.R14, mem2);
                main = new Instruction(op, Register.R13, Register.R14, normal ? whileLabel : endLabel);
            } else if (ss[2].matches("[0-9]+") && Register.isRegister(ss[4]) != -1) {
                Register r1 = Register.getValue(ss[4]);
                if (Register.isReserved(r1))
                    throw new ParserException("Use of reserved register at line " + (getLineNumber()));
                helper1 = new Instruction(Operator.MOVI, Register.R14, new Immediate(Integer.parseInt(ss[2])));
                main = new Instruction(op, Register.R14, r1, normal ? whileLabel : endLabel);
            } else if (ss[2].matches("[0-9]+") && ss[4].matches("[A-Za-z][A-Za-z0-9]*")) {
                VariableLocation varLocation = Parser.initVariable(ss[4]);
                Memory mem = new Memory(Register.R15, new Immediate(varLocation));
                helper1 = new Instruction(Operator.MOVI, Register.R13, new Immediate(Integer.parseInt(ss[4])));
                helper2 = new Instruction(Operator.MOVM, Register.R14, mem);
                main = new Instruction(op, Register.R13, Register.R14, normal ? whileLabel : endLabel);
            } else throw new ParserException("invalid conditional statement at line " + (getLineNumber()));
        } else if (ss.length == 4) {
            if (!ss[2].equals("true") && !ss[2].equals("false"))
                throw new ParserException("invalid conditional statement at line " + (getLineNumber()));
            aBoolean = Boolean.valueOf(ss[2]);
            deterministic = true;
        } else
            throw new ParserException("invalid if statement at line " + (getLineNumber()));
        int index = 0;
        ArrayList<InstructionOffset> res = new ArrayList<>();
        if (deterministic) {
            if(aBoolean) {
                ArrayList<InstructionOffset> childArr = children.parse();
                ArrayList<Immediate> end = new ArrayList<>();
                ArrayList<Immediate> start = new ArrayList<>();
                for (InstructionOffset instructionOffset : childArr) {
                    if (instructionOffset.getLabel().equals("break")) {
                        ArrayList<Operand> ops = instructionOffset.getInstruction().getOperands();
                        end.add((Immediate) ops.get(ops.size() - 1));
                        instructionOffset.setLabel("");
                    } else if (instructionOffset.getLabel().equals("continue")) {
                        ArrayList<Operand> ops = instructionOffset.getInstruction().getOperands();
                        start.add((Immediate) ops.get(ops.size() - 1));
                        instructionOffset.setLabel("");
                    }
                    instructionOffset.setOffset(instructionOffset.getOffset() + index);
                }
                index += childArr.size();
                whileStartLabel.setValue(0);
                res.addAll(childArr);
                res.add(new InstructionOffset(repeat, index++));
                endLabel.setValue(index);
                for (Immediate i : start) i.setValue(whileLabel.getIntValue());
                for (Immediate i : end) i.setValue(endLabel.getIntValue());
            }
        } else {
            if (helper1 != null) res.add(new InstructionOffset(helper1, index++));
            if (helper2 != null) res.add(new InstructionOffset(helper2, index++));
            res.add(new InstructionOffset(main, index++));
            whileStartLabel.setValue(0);
            if (operator == Token.GE || operator == Token.LE || operator == Token.NEQ) {
                whileLabel.setValue(index);
                ArrayList<InstructionOffset> childArr = children.parse();
                ArrayList<Immediate> end = new ArrayList<>();
                ArrayList<Immediate> start = new ArrayList<>();
                for (InstructionOffset instructionOffset : childArr) {
                    if(instructionOffset.getLabel().equals("break")) {
                        ArrayList<Operand> ops = instructionOffset.getInstruction().getOperands();
                        end.add((Immediate)ops.get(ops.size()-1));
                        instructionOffset.setLabel("");
                    } else if(instructionOffset.getLabel().equals("continue")) {
                        ArrayList<Operand> ops = instructionOffset.getInstruction().getOperands();
                        start.add((Immediate)ops.get(ops.size()-1));
                        instructionOffset.setLabel("");
                    }
                    instructionOffset.setOffset(instructionOffset.getOffset() + index);
                }
                index += childArr.size();
                res.addAll(childArr);
                res.add(new InstructionOffset(repeat, index++));
                endLabel.setValue(index);
                for(Immediate i : start) i.setValue(whileStartLabel.getIntValue());
                for(Immediate i : end) i.setValue(endLabel.getIntValue());
            } else {
                whileLabel.setValue(index+1);
                ArrayList<InstructionOffset> childArr = children.parse();
                ArrayList<Immediate> end = new ArrayList<>();
                ArrayList<Immediate> start = new ArrayList<>();
                res.add(new InstructionOffset(exit, index++));
                for (InstructionOffset instructionOffset : childArr) {
                    if(instructionOffset.getLabel().equals("break")) {
                        ArrayList<Operand> ops = instructionOffset.getInstruction().getOperands();
                        end.add((Immediate)ops.get(ops.size()-1));
                        instructionOffset.setLabel("");
                    } else if(instructionOffset.getLabel().equals("continue")) {
                        ArrayList<Operand> ops = instructionOffset.getInstruction().getOperands();
                        start.add((Immediate)ops.get(ops.size()-1));
                        instructionOffset.setLabel("");
                    }
                    instructionOffset.setOffset(instructionOffset.getOffset() + index);
                }
                index += childArr.size();
                res.addAll(childArr);
                res.add(new InstructionOffset(repeat, index++));
                endLabel.setValue(index);
                for(Immediate i : start) i.setValue(whileStartLabel.getIntValue());
                for(Immediate i : end) i.setValue(endLabel.getIntValue());
            }
        }
        return res;
    }

    public MultipleStatementNode getChildren() {
        return children;
    }
}
