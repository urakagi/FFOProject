package org.sais.rasoid.card;

import org.sais.rasoid.R;

import android.content.Context;

public abstract class CardText_GSU {

    public static final int BULLET_SPREAD = 0;
    public static final int BULLET_CONCENTRATE = 1;
    public static final int BULLET_NORMAL = 2;
    public static final int TIMING_FILL = 0;
    public static final int TIMING_BATTLE = 1;
    public static final int TIMING_ACTIVATE = 2;
    public static final int ATTACHMENT_LEADER = 0;
    public static final int ATTACHMENT_SPELL = 1;
    public static final int ATTACHMENT_SCENE = 2;
    public String attribute;
    public int hp;
    public int dodge;
    public int kesshi;
    public int mp;
    public String condition;
    public int attack;
    public int intercept;
    public int hit;
    public int bullet;
    public int attachment;
    public int timing;
    public String ruleText;

    public String dump(Context ctx) {
        return "";
    }

    public int getRequireLV() {
        if (condition.length() < 1) {
            return 0;
        } else {
            return condition.split(" ").length;
        }
    }

    public String getTextWithoutColorSymbolAndNewLine() {
        String ret = ruleText;
        ret = ret.replace("@", "");
        ret = ret.replace("$", "");
        ret = ret.replace("%", "");
        ret = ret.replace("^", "");
        ret = ret.replace("&", "");
        ret = ret.replace("\r", "");
        ret = ret.replace("\n", "");
        return ret;
    }

    public static class CardText_Character_GSU extends CardText_GSU {

        @Override
        public String dump(Context ctx) {
        	String format = ctx.getResources().getString(R.string.char_card_dump);
            return String.format(format, hp, dodge, kesshi);
        }
    }

    public static class CardText_Spell_GSU extends CardText_GSU {

        @Override
        public String dump(Context ctx) {
        	String format = ctx.getResources().getString(R.string.spell_card_dump);
        	String bulletString = "";
        	switch (bullet) {
        	case BULLET_SPREAD:
        		bulletString = ctx.getResources().getString(R.string.spread);
        		break;
        	case BULLET_CONCENTRATE:
        		bulletString = ctx.getResources().getString(R.string.concentrate);
        		break;
        	case BULLET_NORMAL:
        		bulletString = ctx.getResources().getString(R.string.normal);
        		break;
        	}
        	return String.format(format, mp, condition, attack, intercept, hit, bulletString);
        }
    }

    public static class CardText_Support_GSU extends CardText_GSU {

        @Override
        public String dump(Context ctx) {
        	String format = ctx.getResources().getString(R.string.support_card_dump);
        	String attachString = "";
        	switch (attachment) {
        	case ATTACHMENT_LEADER:
        		attachString = ctx.getResources().getString(R.string.leader);
        		break;
        	case ATTACHMENT_SCENE:
        		attachString = ctx.getResources().getString(R.string.scene);
        		break;
        	case ATTACHMENT_SPELL:
        		attachString = ctx.getResources().getString(R.string.spell);
        		break;
        	}
        	return String.format(format, mp, condition, attachString);
        }
    }

    public static class CardText_Event_GSU extends CardText_GSU {

        @Override
        public String dump(Context ctx) {
        	String format = ctx.getResources().getString(R.string.event_card_dump);
        	String timingString = "";
        	switch (timing) {
        	case TIMING_ACTIVATE:
        		timingString = ctx.getResources().getString(R.string.activate);
        		break;
        	case TIMING_BATTLE:
        		timingString = ctx.getResources().getString(R.string.battle);
        		break;
        	}
        	return String.format(format, mp, condition, timingString);
        }
    }
}
