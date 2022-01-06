package mua;

import java.io.File;
import java.io.FileNotFoundException;
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
    private Stack <HashMap<String, Value>> paraTableStack;
    /** Store data about function. */
    private HashMap funcTable;

    public void run() {
        scanPerWord = new Scanner(System.in);
        paraTableStack = new Stack<>();
        startShow();

        // create the global para table.
        HashMap<String, Value> curParaTable = new HashMap<>();
        paraTableStack.push(curParaTable);
        // add const variable.
        addConstant();
        // the core of interpreter.
        while(scanPerWord.hasNext()) {
            String op = scanPerWord.next();
            Value res = selOprand(op);
            if(res.getVal().compareTo("false") == 0)
                break;
        }
        System.out.println("end the program.");
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
     * Add constant to global para table.
     */
    void addConstant() {
        HashMap<String, Value> curParaTable = paraTableStack.pop();

        Value constant = new Value("3.14159", NUMBER_);
        String constantName = "pi";
        curParaTable.put(constantName, constant);

        paraTableStack.push(curParaTable);
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
        HashMap<String, Value> curParaTable = paraTableStack.peek();

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
                res =  curParaTable.get(paraName.getVal());
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
                res = curParaTable.get(paraName);
            }
            else {
                errorThrow("The variable ["+paraName+"] cannot be found in table.");
            }

            return res;
        }
        else if(oprand.charAt(0) == '[') {
            return readList(oprand);
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
        else if(oprand.equals("make")) { /* - make <name> <value> */
            return muaMake();
        }
        else if(oprand.equals("print")) { /* - print <value> */
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
        else if(oprand.equals("erase")) {
            String tmp = scanPerWord.next();
            Value res = curParaTable.get(tmp.substring(1));
            res.setVal("");
            curParaTable.put(tmp.substring(1), res);
            return res;
        }
        else if(oprand.equals("random")) { /* - random <number> */
            int num = Integer.parseInt(scanPerWord.next());
            return new Value(String.valueOf(Math.random()*num), NUMBER_);
        }
        else if(oprand.equals("int")) { /* - int <number> */
            int res = Integer.parseInt(scanPerWord.next());
            return new Value(String.valueOf(res), NUMBER_);
        }
        else if(oprand.equals("sqrt")) { /* - sqrt <number> */
            double f = Double.parseDouble(scanPerWord.next());
            f = Math.sqrt(f);
            return new Value(String.valueOf(f), NUMBER_);
        }
        else if(oprand.equals("load")) { /* - load <word> */
            String fileName = scanPerWord.next();
            return muaLoad(fileName);
        }
        else if(oprand.equals("isname")) {
            Value name = selOprand(scanPerWord.next());

            // todo check function name
            Stack <HashMap<String, Value>> tmpStack = paraTableStack;
            while (!tmpStack.isEmpty()) {
                HashMap <String, Value> tmpParaTable = tmpStack.pop();
                if (tmpParaTable.containsKey(name.getVal())) {
                    return new Value("true", BOOl_);
                }
            }
            return new Value("false", BOOl_);
        }
        else if(oprand.equals("isnumber")) {
            return checkType(NUMBER_);
        }
        else if(oprand.equals("isbool")) {
            return checkType(BOOl_);
        }
        else if(oprand.equals("isword")) {
            return checkType(WORD_);
        }
        else if(oprand.equals("islist")) {
            return checkType(LIST_);
        }
        else if(oprand.equals("isempty")) {
            Value para = selOprand(scanPerWord.next());
            if(para.getType() == WORD_) {
                return emptyCheck(para.getVal().trim());
            }
            else if(para.getType() == LIST_) {
                String val = para.getVal();
                val = val.substring(1, val.length()-1);
                return emptyCheck(val.trim());
            }
            else {
                errorThrow("Type of [isempty] should be [WORD] or [LIST].");
            }
        }
        else if(oprand.equals("and")) {
            return muaLogic(1);
        }
        else if(oprand.equals("or")) {
            return muaLogic(2);
        }
        else if(oprand.equals("not")) {
            Value para = selOprand(scanPerWord.next());

            if(para.getType() == BOOl_) {
                if(para.getVal().compareTo("true") == 0) {
                    return new Value("false", BOOl_);
                }
                else {
                    return new Value("true", BOOl_);
                }
            }
            else {
                errorThrow("Logic operation type is not BOOL");
            }
        }
        else if(oprand.equals("run")) {
            Value list = selOprand(scanPerWord.next());
            return runList(list.getVal());
        }
        else if(oprand.equals("if")) {
            Value opFlag = selOprand(scanPerWord.next());

            if(opFlag.getType() != BOOl_) {
                errorThrow("The type after IF is not a BOOL");
            }

            Value list1 = selOprand(scanPerWord.next());
            Value list2 = selOprand(scanPerWord.next());
            if(opFlag.getVal().compareTo("true") == 0) {
                return runList(list1.getVal());
            }
            else {
                return runList(list2.getVal());
            }
        }
        else if(oprand.equals("eq")) {
            return valCompare(1);
        }
        else if(oprand.equals("gt")) {
            return valCompare(2);
        }
        else if(oprand.equals("lt")) {
            return valCompare(3);
        }
        else if(oprand.equals("first")) {
            return getElement(1);
        }
        else if(oprand.equals("last")) {
            return getElement(2);
        }
        else if(oprand.equals("butfirst")) {
            return getElement(3);
        }
        else if(oprand.equals("butlast")) {
            return getElement(4);
        }
        else if(oprand.equals("exit")) {
            return new Value("false", BOOl_);
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
            HashMap<String, Value> curParaTable = paraTableStack.pop();
            curParaTable.put(paraName.getVal(), para);
            paraTableStack.push(curParaTable);
        }

        return para;
    }

    /**
     * Read list body.
     * @param oprand oprand.
     * @return the String of list body.
     */
    Value readList(String oprand) {
        StringBuilder body = new StringBuilder(oprand);

        /*
         * Use loop to get the content of list.
         * If the number of '[' does not equal to ']', it means we need scan more.
         */
        int cnt = 0;
        while (true) {
            for(int i=0; i<body.length(); i++) {
                if(body.charAt(i) == '[')
                    cnt++;
                if(body.charAt(i) == ']')
                    cnt--;
            }

            if(cnt == 0)
                break;
            else {
                // if there is not enough ']' to match '[', it means there is still input.
                String tmp = scanPerWord.next();
                body.append(" ").append(tmp);
            }
            cnt = 0;
        }

        /*
         * Traverse the body and check if the body is composed of two lists.
         * If it is, it is a function.
         * Otherwise, it is a normal list.
         */
        int numOfList = 0;
        cnt = 0;
        for(int i=1; i<body.length()-1; i++) {
            if(body.charAt(i) == '[')
                cnt++;
            if(body.charAt(i) == ']') {
                cnt--;
                if(cnt == 0)
                    numOfList++;
            }
        }
        String tmp = body.substring(1, body.length()-1).trim();

        boolean funcFlag;
        if(tmp.compareTo("") == 0)
            funcFlag = false;
        else
            funcFlag = tmp.charAt(0) == '[' && tmp.charAt(tmp.length() - 1) == ']';

        if(numOfList == 2 && funcFlag) {
            return new Value(body.toString(), FUNCTION_);
        }
        else
            return new Value(body.toString(), LIST_);
    }

    /**
     * to run the content of list.
     * - run <list>
     * @param list the content of list which type in Java is String.
     * @return <value>
     */
    Value runList(String list) {
        if(list.charAt(0) != '[')
            errorThrow("This is not a LIST!");

        Scanner mainScanner = scanPerWord;
        scanPerWord = new Scanner(list.substring(1,list.length()-1));

        Value res = null;
        while (scanPerWord.hasNext()) {
            res = selOprand(scanPerWord.next());
//            if(funcReturn)
//                break;
        }
        scanPerWord = mainScanner;

        return res;
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
        Value para2 = selOprand(scanPerWord.next());

        /* add type check 22/01/04 */
        if(para1.getType() != NUMBER_ || para2.getType() != NUMBER_) {
            errorThrow("Arithmetic operation type is not a NUMBER.");
        }

        double num1 = Double.parseDouble(para1.getVal());
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

    /**
     * Execute all the code in the file named word.
     * @param fileName the file name.
     * @return always return true.
     */
    Value muaLoad(String fileName) {
        File file = new File(fileName);

        Scanner mainScanner = scanPerWord;
        try {
            scanPerWord = new Scanner(file);
        } catch (FileNotFoundException e) {
            return errorThrow(e.getMessage());
        }

        Value res = null;
        while (scanPerWord.hasNext()) {
            res = selOprand(scanPerWord.next());
        }
        scanPerWord = mainScanner;
        return res;
    }

    /**
     * Used to check whether [Value] is the selected type.
     * - isxxxx <value>
     * @param wait2Check the type we hope to be.
     * @return true or false.
     */
    Value checkType(int wait2Check) {
        Value para = selOprand(scanPerWord.next());

        if(para.getType() == wait2Check) {
            return new Value("true", BOOl_);
        }
        else {
            return new Value("false", BOOl_);
        }
    }

    /**
     * Logic calculate for two values.
     * - and/or <value> <value>
     * @param type 1 -> and     2 -> or
     * @return true or false.
     */
    Value muaLogic(int type) {
        Value para1 = selOprand(scanPerWord.next());
        Value para2 = selOprand(scanPerWord.next());

        if(para1.getType() != BOOl_ || para2.getType() != BOOl_) {
            errorThrow("Logic operation type is not BOOL");
        }

        if(type == 1) {
            if(para1.getVal().compareTo("true") == 0
                    && para2.getVal().compareTo("true") == 0) {
                return new Value("true", BOOl_);
            }
            else {
                return new Value("false", BOOl_);
            }
        }
        else if(type == 2) {
            if(para1.getVal().compareTo("true") == 0
                    || para2.getVal().compareTo("true") == 0) {
                return new Value("true", BOOl_);
            }
            else
                return new Value("true", BOOl_);
        }
        else
            errorThrow("???");
        return null;
    }

    /**
     * value compare
     * @param type 1 -> eq      2 -> gt     3 -> lt
     * @return  true or false
     */
    Value valCompare(int type) {
        Value para1 = selOprand(scanPerWord.next());
        Value para2 = selOprand(scanPerWord.next());

        int res = para1.getVal().compareTo(para2.getVal());
        if(type == 1 && res == 0) {
            return new Value("true", BOOl_);
        }
        if(type == 2 && res > 0) {
            return new Value("true", BOOl_);
        }
        if(type == 3 && res < 0) {
            return new Value("true", BOOl_);
        }

        return new Value("false", BOOl_);
    }

    /**
     * to check the string is empty or not.
     * @param s the string wait to check
     * @return true or false.
     */
    Value emptyCheck(String s) {
        if(s.compareTo("") == 0)
            return new Value("true", BOOl_);
        else
            return new Value("false", BOOl_);
    }

    /**
     * Separate the elements in the list into a stack.
     * @param s the content of list.
     * @return the stack filled with elements.
     */
    Stack<String> splitByBlank(String s) {
        s = s.trim();
        Stack<String> str = new Stack<String>();
        int cnt=0;
        StringBuilder unit = new StringBuilder();
        for(int i=0; i<s.length(); i++) {
            if(s.charAt(i) == ' ' && cnt == 0) {
                unit = new StringBuilder(unit.toString().trim());
                str.push(unit.toString());
                unit = new StringBuilder();
                continue;
            }
            if(s.charAt(i) == '[')
                cnt++;
            else if(s.charAt(i) == ']')
                cnt--;
            unit.append(s.charAt(i));
        }
        str.push(unit.toString());

        return str;
    }

    /**
     * reverse the stack
     * @param s stack
     * @return the stack after reversing
     */
    Stack<String> reverseStack(Stack<String> s) {
        Stack<String> res = new Stack<String>();
        int size = s.size();
        for(int i=0; i<size; i++) {
            String tmp = s.pop();
            res.push(tmp);
        }
        return res;
    }

    /**
     * return the element in the list.
     * @param type 1 -> first       2 -> last
     * @return the value of element
     */
    Value getElement(int type) {
        Value para = selOprand(scanPerWord.next());
        if(para.getType() == WORD_) {
            String res = "";
            if(type == 1)
                res += para.getVal().charAt(0);
            else if(type == 2) {
                res += para.getVal().charAt(para.getVal().length() - 1);
            }
            else if(type == 3){
                res  = para.getVal().substring(1);
            }
            else {
                res = para.getVal().substring(0, para.getVal().length()-1);
            }
            return new Value(res, WORD_);
        }
        else if(para.getType() == LIST_) {
            String content = para.getVal().substring(1,para.getVal().length()-1).trim();
            Stack<String> elementStack = splitByBlank(content);
            if(elementStack.size() == 0)
                return new Value("[]", LIST_);

            if(type != 2) {
                elementStack = reverseStack(elementStack);
            }

            if(type <= 2) {
                String res = (String) elementStack.peek();
                if (res.charAt(0) == '[')
                    return new Value(res, LIST_);
                else
                    return new Value(res, WORD_);
            }
            else {
                String res = "";
                int size=0;
                if(type == 3) {
                    elementStack.pop();
                    size = elementStack.size();
                }
                else {
                     size = elementStack.size() - 1;
                }
                for (int i = 0; i < size; i++) {
                    res += elementStack.pop();
                    res += " ";
                }
                res = res.trim();

                return new Value("["+res+"]", LIST_);
            }
        }
        else {
            errorThrow("The type of [first] should be LIST or WORD");
        }
        return null;
    }
}