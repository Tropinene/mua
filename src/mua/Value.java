package mua;

/**
 * @description：值类
 * @ClassName：Value.java
 * @author Ya-Ou
 * @Date   2022-1-3 2:47:32
 * @version 1.00
 */
public class Value {
    private int type;
    private String val;

    /**
     * @param val the content of varables.
     * @param type the type of varables.
     */
    public Value(String val, int type) {
        this.type = type;
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public int getType() {
        return type;
    }
}
