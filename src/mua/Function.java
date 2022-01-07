package mua;

import java.util.HashMap;

public class Function {
    public HashMap localVarTable;
    private String funcName;
    private Value funcBody;
    private Value paraList;

    public Function(String funcName, Value paraList, Value funcBody) {
        this.funcName = funcName;
        this.paraList = paraList;
        this.funcBody = funcBody;
        localVarTable = new HashMap<String, Value>();
    }

    public HashMap getLocalVarTable() {return localVarTable;}
    public void setLocalVarTable(HashMap h) {localVarTable = h;}
    public String getFuncName() {return funcName;}
    public Value getParaList() {return paraList;}
    public Value getFuncBody() {return funcBody;}
    public String setValue() {
        String val = "";
        val = "[" + paraList.getVal() + funcBody.getVal() + "]";
        return val;
    }
}
