package mua;

import java.util.*;

/**
 * @description：解释器类
 * @ClassName：Interpreter.java
 * @author Ya-Ou
 * @Date   2022-1-2 21:16:12
 * @version 1.00
 */
public class Interpreter {
    /** The value type of Value.class */
    public static final int BOOl_= 0;
    public static final int NUMBER_= 1;
    public static final int WORD_= 2;
    public static final int LIST_= 3;
    public static final int ERROR_= 4;
    public static final int FUNCTION_= 5;

    /** Main Scanner */
    private Scanner scanPerWord;
    /** Used to store variable tables.
     * In the process of function switching, use this to realize variable table switching.
     * The peek is current para table.
     */
    private Stack paraTableStack;
    /** Store data about function. */
    private HashMap funcTable;

    public void run() {
        scanPerWord = new Scanner(System.in);
        paraTableStack = new Stack<HashMap>();
        startShow();

        // create the global para table.
        HashMap curParaTable = new HashMap<String, Value>();
        paraTableStack.push(curParaTable);
        // the core of interpreter.
        while(scanPerWord.hasNext()) {
            String op = scanPerWord.next();
            Value res = selOprand(op);
        }
    }

    /** Output some information before the program runs */
    void startShow() {
        System.out.println(".___  ___.  __    __       ___");
        System.out.println("|   \\/   | |  |  |  |     /   \\");
        System.out.println("|  \\  /  | |  |  |  |    /  ^  \\");
        System.out.println("|  |\\/|  | |  |  |  |   /  /_\\  \\");
        System.out.println("|  |  |  | |  `--'  |  /  _____  \\");
        System.out.println("|__|  |__|  \\______/  /__/     \\__\\");
        System.out.println();
        System.out.println();
        System.out.println("The [MUA] interpreter start...");
    }

    /**
     * Handle the case of program operation error.
     * @param msg throw error message
     * @return the return value after error instruction.
     */
    public Value errorThrow(String msg) {
        System.out.println("[ERROR] : " + msg);
        System.exit(1);
        return new Value("[ERROR] " + msg, ERROR_);
    }

    /**
     * A command selector,
     * @param oprand the content of instruction, which type is [String].
     * @return the return value after the instruction is executed.
     */
    private Value selOprand(String oprand) {
        HashMap curParaTable = (HashMap) paraTableStack.peek();

        if(oprand.charAt(0) == '"') {
            return new Value(oprand.substring(1), WORD_);
        }
        else if(isNumeric(oprand)) {
            return new Value(oprand, NUMBER_);
        }
        else if(oprand.equals("true") || oprand.equals("false")) {
            return new Value(oprand, BOOl_);
        }
        else if(oprand.equals("thing")) {
            Value paraName = selOprand(scanPerWord.next());
            Value res = null;

            if(curParaTable.containsKey(paraName.getVal())) {
                res = (Value) curParaTable.get(paraName.getVal());
            }
            else {
                errorThrow("The variable ["+paraName.getVal()+"] cannot be found in table.");
            }

            return res;
        }
        else if(oprand.charAt(0) == ':') {
            String paraName = oprand.substring(1);
            Value res = null;

            if(curParaTable.containsKey(paraName)) {
                res = (Value) curParaTable.get(paraName);
            }
            else {
                errorThrow("The variable ["+paraName+"] cannot be found in table.");
            }

            return res;
        }
        else if(oprand.equals("read")) {
            String tmp = scanPerWord.next();
            return new Value(tmp, WORD_);
        }
        else if(oprand.equals("add")) {
            return muaCalculate(1);
        }
        else if(oprand.equals("sub")) {
            return muaCalculate(2);
        }
        else if(oprand.equals("mul")) {
            return muaCalculate(3);
        }
        else if(oprand.equals("div")) {
            return muaCalculate(4);
        }
        else if(oprand.equals("mod")) {
            return muaCalculate(5);
        }
        else if(oprand.equals("make")) { /** - make <name> <value> */
            return muaMake();
        }
        else if(oprand.equals("print")) { /** - print <value> */
            Value para = selOprand(scanPerWord.next());

            if(para.getType() == LIST_) {
                String val = para.getVal();
                val = val.substring(1, val.length()-1).trim();
                System.out.println(val);
            }
            else
                System.out.println(para.getVal());

            return para;
        }
        else if(oprand.equals("exit")) {
            System.out.println("end the program.");
            System.exit(1);
        }
        else {
            errorThrow("Unknown instruction: " + oprand);
        }
        return null;
    }

    /**
     * The function to realize [make].
     * - make <name> <value>
     * @return the return value which is bound to the [name].
     */
    Value muaMake() {
        Value paraName = selOprand(scanPerWord.next());
        Value para = selOprand(scanPerWord.next());

        if(para.getType() == FUNCTION_) {
//            funcNameTable.add(paraName.getVal());
//            analysisFunc(paraName.getVal(), para2);
        }
        else {
            HashMap curParaTable = (HashMap) paraTableStack.pop();
            curParaTable.put(paraName.getVal(), para);
            paraTableStack.push(curParaTable);
        }

        return para;
    }

    /**
     * Use to realize the calculate the two values.
     * - add/sub/mul/div/mod <number> <number>
     * @param state to distinguish the type of calculate.
     *              1 -> add    2 -> sub    3 -> mul    4 -> div    5 -> mod
     * @return the result after calculating.
     */
    Value muaCalculate(int state) {
        Value para1 = selOprand(scanPerWord.next());
        double num1 = Double.parseDouble(para1.getVal());
        Value para2 = selOprand(scanPerWord.next());
        double num2 = Double.parseDouble(para2.getVal());

        if(state == 1) {
            return new Value(String.valueOf(num1 + num2), NUMBER_);
        }
        else if(state == 2) {
            return new Value(String.valueOf(num1 - num2), NUMBER_);
        }
        else if(state == 3) {
            return new Value(String.valueOf(num1 * num2), NUMBER_);
        }
        else if(state == 4) {
            if(num2 == 0) {
                errorThrow("The division cannot be 0.");
            }
            return new Value(String.valueOf(num1 / num2), NUMBER_);
        }
        else if(state == 5) {
            if(num2 == 0) {
                errorThrow("The division connot be 0.");
            }
            return new Value(String.valueOf(num1 % num2), NUMBER_);
        }
        else {
            errorThrow("There is no such calculate option.");
        }
        return null;
    }

    /**
     * Used to judge if the input is a number.
     * @param s the input wait to test.
     * @return the result whether the input is a number.
     */
    public boolean isNumeric(String s) {
        int len = s.length();
        int i = 0;

        if(s.charAt(i) == '0' || s.charAt(i) =='-') {
            i++;
        }
        for(; i < len; ++i) {
            if (!Character.isDigit(s.charAt(i)) && s.charAt(i) != '.') {
                return false;
            }
        }
        return true;
    }
}