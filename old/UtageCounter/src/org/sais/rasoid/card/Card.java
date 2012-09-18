package org.sais.rasoid.card;

import java.util.ArrayList;

import org.sais.rasoid.dummy.AutoMech;
import org.sais.rasoid.dummy.ChoiceEffect;

import android.content.Context;

public class Card {

   public static final int CARDTYPE_CHAR = 0;
   public static final int CARDTYPE_SPELL = 1;
   public static final int CARDTYPE_SUPPORT = 2;
   public static final int CARDTYPE_EVENT = 3;

   public int cardnum;
   public String cardname;
   public int cardtype; //(0:Character 1:Spell 2:Support 3:Event)
   public CardText_GSU cardtext;
   public ArrayList<AutoMech> automechs = new ArrayList<AutoMech>();
   public ArrayList<ChoiceEffect> choices = new ArrayList<ChoiceEffect>();

   public String dump(Context ctx) {
      return cardtext.dump(ctx);
   }
}
