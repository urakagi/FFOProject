/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.card;

import java.awt.Color;
import java.io.Serializable;
import org.sais.fantasyfesta.enums.ECardType;
import java.util.ArrayList;
import org.sais.fantasyfesta.autos.AutoMech;
import org.sais.fantasyfesta.core.GameCore;
import org.sais.fantasyfesta.core.Player;
import org.sais.fantasyfesta.district.District;
import org.sais.fantasyfesta.district.NullDistrict;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public abstract class CardInfo implements Serializable {

    static public String[] CHAR_NAMES = new String[]{"霊夢", "魔理沙", "咲夜", "妖夢", "紫", "アリス",
        "レミリア", "幽々子", "フランドール", "パチュリー", "美鈴", "輝夜", "永琳", "鈴仙",
        "藍", "橙", "プリズムリバー", "妹紅", "慧音", "萃香", "諏訪子", "神奈子", "早苗", "文",
        "小町", "にとり", "天子", "衣玖", "空", "燐", "こいし", "さとり", "白蓮", "星",
        "ぬえ", "小傘", "ナズーリン", "水蜜"};
    // Original cards handling
    public static final int HINA = 5000;
    public static final int BYAKUREN = 4200;
    public static final int ID_COWORK = 90;
    protected int mNo = 0;
    protected String mName = "";
    protected int mSpellPointRequirement = 0;
    protected String mCharacterRequirement = "";
    protected String mRuleText = "";
    protected ArrayList<AutoMech> mAutoMechs = new ArrayList<AutoMech>();
    protected ArrayList<ChoiceEffect> mChoices = new ArrayList<ChoiceEffect>();

    public static CardInfo createCardInfo(int no, String name, int type) {
        CardInfo ret;
        switch (type) {
            case CharacterCardInfo.TEXT_TYPE_INDEX:
                ret = new CharacterCardInfo();
                break;
            case SpellCardInfo.TEXT_TYPE_INDEX:
                ret = new SpellCardInfo();
                break;
            case SupportCardInfo.TEXT_TYPE_INDEX:
                ret = new SupportCardInfo();
                break;
            case EventCardInfo.TEXT_TYPE_INDEX:
                ret = new EventCardInfo();
                break;
            default:
                return null;
        }
        ret.mNo = no;
        ret.mName = name;
        return ret;
    }

    public static String getCharNameById(int charid) {
        return CHAR_NAMES[charid - 1];
    }

    public abstract void setFifthLine(String pp);

    public void setCharacterRequirement(String pp) {
        int len = pp.length();
        if (len > 0) {
            char last = pp.charAt(len - 1);
            if (last >= '1' && last <= '4') {
                int lv = last - '0';
                String c = pp.substring(0, len - 1);
                pp = c;
                for (int i = 1; i < lv; ++i) {
                    pp += " " + c;
                }
            }
        }
        mCharacterRequirement = pp;
    }

    public void setSPRequirement(String pp) {
        mSpellPointRequirement = FTool.safeParseInt(pp);
    }

    public void addRuleText(String pp) {
        mRuleText += pp;
    }

    public void addAutoMech(AutoMech mech) {
        mAutoMechs.add(mech);
    }

    public void addChoiceEffect(ChoiceEffect eff) {
        mChoices.add(eff);
    }

    public int getCardNo() {
        return mNo;
    }

    public String getName() {
        return mName;
    }

    abstract public ECardType getCardType();

    public ArrayList<AutoMech> getAutoMechs() {
        return mAutoMechs;
    }

    public boolean isNo(int no) {
        return no == mNo;
    }

    public boolean isOwnedBy(int charid) {
        // Cowork
        if (mNo > 9000) {
            return mCharacterRequirement.contains(getCharNameById(charid));
        }
        return charid == mNo / 100;
    }

    public boolean isCardType(ECardType type) {
        return type == getCardType();
    }

    public boolean isCharacterCard() {
        return getCardType() == ECardType.CHARACTER;
    }

    public String getRuleText() {
        return mRuleText;
    }

    public ArrayList<ChoiceEffect> getChoiceEffects() {
        return mChoices;
    }

    public ChoiceEffect getChoiceEffect(int index) {
        for (ChoiceEffect e : mChoices) {
            if (e.getEffectIndex() == index) {
                return e;
            }
        }
        throw new IllegalArgumentException("Invalid choice effect index.");
    }

    public String getCharacterRequirement() {
        return mCharacterRequirement;
    }

    public int getSpellPointRequirement() {
        return mSpellPointRequirement;
    }

    public int getLevelRequirement() {
        if (mCharacterRequirement.length() < 1) {
            return 0;
        } else {
            return mCharacterRequirement.split(" ").length;
        }
    }

    public void payCost(Player player, GameCore core) {
        player.consume(getSpellPointRequirement());
    }

    public String getTextWithoutColorSymbolAndNewLine() {
        return mRuleText.replace("@", "").replace("$", "").replace("%", "").replace("^", "").replace("&", "").replace("\r", "").replace("\n", "");
    }

    abstract public String dump(String[] loc);

    abstract public Color getBackgroundColor();

    public Card createCard() {
        return createCard(EPlayer.ICH, EPlayer.ICH, new NullDistrict());
    }

    abstract public Card createCard(EPlayer controller, EPlayer owner, District district);

    abstract public String getDefaultIconFilePrefix();

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        return this.mNo == ((CardInfo) obj).mNo;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.mNo;
        hash = 97 * hash + (this.mName != null ? this.mName.hashCode() : 0);
        return hash;
    }

    public int getCharId() {
        if (mNo / 100 == 80) {
            return 17;
        } else if (mNo / 100 == 81) {
            return 19;
        } else if (mNo / 100 == 90 || mNo / 100 == 91) {
            return ID_COWORK;
        } else {
            return mNo / 100;
        }
    }

    public String getCharacterName() {
        return CHAR_NAMES[getCharId()];
    }

    public int getVersion() {
        int ret = 0;
        int group = -1;
        int charid = getCharId();
        if (charid >= 1 && charid <= 8) {
            group = 1;
        } else if (charid >= 9 && charid <= 14) {
            group = 2;
        } else if (charid == 17) {
            group = 17;
        } else if (charid >= 15 && charid <= 20) {
            group = 3;
        } else if (charid >= 21 && charid <= 26) {
            group = 5;
        } else if (charid >= 27 && charid <= 30) {
            group = 6;
        } else if (charid >= 31 && charid <= 34) {
            group = 7;
        } else if (charid >= 35 && charid <= 38) {
            group = 8;
        } else if (charid == 0) {
            group = 0;
        } else if (charid == ID_COWORK) {
            group = ID_COWORK;
        } else {
            //Original Cards
            group = 100;
        }
        int id = mNo % 100;
        switch (group) {
            case 0:
                if (id <= 12) {
                    ret = 1;
                } else if (id <= 18) {
                    ret = 2;
                } else if (id <= 24) {
                    ret = 3;
                } else if (id <= 33) {
                    ret = 4;
                } else if (id <= 45) {
                    ret = 5;
                } else if (id <= 57) {
                    ret = 6;
                } else if (id <= 65) {
                    ret = 7;
                } else if (id <= 73) {
                    ret = 8;
                }
                break;
            case 1:
                if (id <= 14) {
                    ret = 1;
                } else if (id <= 17) {
                    ret = 2;
                } else if (id <= 18) {
                    ret = 3;
                } else if (id <= 19) {
                    ret = 4;
                } else if (id <= 20) {
                    ret = 5;
                } else {
                    ret = 6;
                }
                break;
            case 2:
                if (id <= 15) {
                    ret = 2;
                } else if (id <= 17) {
                    ret = 3;
                } else if (id <= 18) {
                    ret = 4;
                } else if (id <= 19) {
                    ret = 5;
                } else {
                    ret = 6;
                }
                break;
            case 3:
                if (id <= 15) {
                    ret = 3;
                } else if (id <= 17) {
                    ret = 4;
                } else if (id <= 18) {
                    ret = 5;
                } else {
                    ret = 6;
                }
                break;
            case 5:
                if (id <= 15) {
                    ret = 5;
                } else if (id <= 17) {
                    ret = 6;
                } else if (id <= 18) {
                    ret = 7;
                } else if (id <= 19) {
                    ret = 8;
                }
                break;
            case 6:
                if (id <= 15) {
                    ret = 6;
                } else if (id <= 17) {
                    ret = 7;
                } else if (id <= 18) {
                    ret = 8;
                }
                break;
            case 7:
                if (id <= 16) {
                    ret = 7;
                } else if (id <= 18) {
                    ret = 8;
                }
                break;
            case 8:
                if (id <= 16) {
                    ret = 8;
                }
                break;
            case 17:
                if (id <= 15) {
                    ret = 3;
                } else if (id <= 19) {
                    ret = 4;
                } else if (id <= 20) {
                    ret = 5;
                } else {
                    ret = 6;
                }
                break;
            case ID_COWORK:
                id = mNo % 1000;
                if (id <= 12) {
                    ret = 1;
                } else if (id <= 24) {
                    ret = 2;
                } else if (id <= 42) {
                    ret = 3;
                } else if (id <= 66) {
                    ret = 4;
                } else if (id <= 84) {
                    ret = 5;
                } else if (id <= 102) {
                    ret = 6;
                } else if (id <= 116) {
                    ret = 7;
                } else if (id <= 130) {
                    ret = 8;
                }
                break;
            case 100:
                ret = 100;
                break;
        }
        return ret;
    }

    public boolean isNull() {
        return mNo == 0;
    }

    static public CardInfo newNull() {
        return new Null();
    };

    public static class Null extends CardInfo {

        @Override
        public void setFifthLine(String pp) {

        }

        @Override
        public ECardType getCardType() {
            return ECardType.NULL;
        }

        @Override
        public String dump(String[] loc) {
            return "";
        }

        @Override
        public Color getBackgroundColor() {
            return Color.WHITE;
        }

        @Override
        public Card createCard(EPlayer controller, EPlayer owner, District district) {
            return Card.newNull();
        }

        @Override
        public String getDefaultIconFilePrefix() {
            return "";
        }

    }

}
