package org.sais.fantasyfesta.tool;

public class MethodInfo {
    static final StackTraceElement NULL_STACKTRACEELEMENT
        = new StackTraceElement("", "", "", 0);
    StackTraceElement[] stack;
    int startIndex;
    /**
     * メソッド情報オブジェクトを生成する。
     */
    public MethodInfo() {
        stack = new Throwable().getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            if ("<init>".equals(stack[i].getMethodName())) {
                startIndex = i + 1;
                break;
            }
        }
        assert stack != null;
    }
    /**
     * 現在のメソッド名を取得する。
     * @return メソッド名。取得できない場合は空文字列
     */
    public String getCurrentName() {
        assert stack != null;
        return getStack(startIndex).getMethodName();
    }
    /**
     * 現在のメソッドを呼び出したメソッド名を取得する。
     * @return メソッド名。取得できない場合は空文字列
     */
    public String getCallerName() {
        assert stack != null;
        return getStack(startIndex + 1).getMethodName();
    }
    /**
     * 現在のメソッドを実装したクラス名を取得する。
     * @return クラス名。取得できない場合は空文字列
     */
    public String getCurrentClassName() {
        assert stack != null;
        return extractClassName(getStack(startIndex)
            .getClassName());
    }
    /**
     * 現在のメソッドを呼び出したクラスの名前を取得する。
     * @return クラス名。取得できない場合は空文字列
     */
    public String getCallerClassName() {
        assert stack != null;
        return extractClassName(getStack(startIndex + 1)
            .getClassName());
    }
    StackTraceElement getStack(int index) {
        return (stack.length > index) ?
            stack[index] : NULL_STACKTRACEELEMENT;
    }
    static String extractClassName(String name) {
        int n = name.lastIndexOf('.');
        if (n < 0) {
            return name;
        }
        return name.substring(n + 1);
    }
}
