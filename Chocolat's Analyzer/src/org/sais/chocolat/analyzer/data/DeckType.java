package org.sais.chocolat.analyzer.data;

import java.util.ArrayList;

public class DeckType {

    public String user;
    public String charstring;
    public String[] chars;

    public DeckType(String user, String tooltext) {
        super();
        this.user = user;
        charstring = tooltext;
        chars = tooltext.split("-");
    }

    public String[] getDisplayChars() {
        String[] ret = new String[4];
        for (int i = 0; i < 4; ++i) {
            String[] ss = chars[i].split(" ");
            ret[i] = ((ss.length == 2) ? ss[1] : chars[i]);
            if (ret[i].equals("ルナサ") || ret[i].equals("メルラン") || ret[i].equals("リリカ")) {
                ret[i] = "プリズムリバー";
            }
        }
        return ret;
    }

    public String getDisplayName() {
        try {
            if (chars.length < 4) {
                if (chars.length == 1) {
                    return chars[0];
                }
                return "";
            }
            String ret = "L";
            String temp = "";
            ArrayList<String> done = new ArrayList<String>();
            for (int k = 0; k < 4; ++k) {
                int cnt = 1;
                if (done.contains(chars[k])) {
                    continue;
                }
                temp = chars[k];
                done.add(temp);
                for (int i = k + 1; i < 4; ++i) {
                    if (chars[i].equals(temp)) {
                        ++cnt;
                    }
                }
                ret += ((temp.split(" ").length == 2) ? temp.split(" ")[1] : temp) + cnt + "-";
            }
            ret = ret.substring(0, ret.length() - 1);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
