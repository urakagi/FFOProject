/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

import org.sais.fantasyfesta.district.ActivatedDistrict;
import org.sais.fantasyfesta.district.BattleDistrict;
import org.sais.fantasyfesta.district.District;
import org.sais.fantasyfesta.district.LeaderAttachmentDistrict;
import org.sais.fantasyfesta.district.DiscardPileDistrict;
import org.sais.fantasyfesta.district.EventDistrict;
import org.sais.fantasyfesta.district.ReservedDistrict;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import org.sais.fantasyfesta.card.Card;
import org.sais.fantasyfesta.card.CardDatabase;
import org.sais.fantasyfesta.card.CardSet;
import org.sais.fantasyfesta.card.CharacterCard;
import org.sais.fantasyfesta.card.EventCard;
import org.sais.fantasyfesta.card.SpellCard;
import org.sais.fantasyfesta.card.SpellCard.NotAttachedException;
import org.sais.fantasyfesta.card.SupportCard;
import org.sais.fantasyfesta.district.CharactersDistrict;
import org.sais.fantasyfesta.district.HandDistrict;
import org.sais.fantasyfesta.district.LibraryDistrict;
import org.sais.fantasyfesta.enums.ECardType;
import org.sais.fantasyfesta.enums.EPlayer;
import org.sais.fantasyfesta.enums.ERegion;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author Romulus
 */
public class PlayField {

    private GameInformation gi;
    private CharactersDistrict charactersDistrict = new CharactersDistrict();
    private HandDistrict handDistrict = new HandDistrict();
    private LibraryDistrict libraryDistrict = new LibraryDistrict();
    private DiscardPileDistrict discardPileDistrict = new DiscardPileDistrict();
    private LeaderAttachmentDistrict leaderAttachmentDistrict = new LeaderAttachmentDistrict();
    private ReservedDistrict reservedDistrict = new ReservedDistrict();
    private ActivatedDistrict activatedDistrict = new ActivatedDistrict();
    private BattleDistrict battleCardDistrict = new BattleDistrict();
    private EventDistrict eventCardDistrict = new EventDistrict();

    public PlayField(GameInformation gi) {
        this.gi = gi;
    }

    @SuppressWarnings("unchecked")
    public CardSet<SpellCard> getActivated() {
        return activatedDistrict.getSet();
    }

    public SpellCard getBattleCard() {
        return battleCardDistrict.getCard();
    }

    public SpellCard getLastBattleCard() {
        return battleCardDistrict.getLastCard();
    }

    public void setBattleCard(SpellCard card) {
        battleCardDistrict.setCard(card);
    }

    public ArrayList<CharacterCard> getCharsExceptLeader() {
        return charactersDistrict.getCharsExceptLeader();
    }

    public ArrayList<CharacterCard> getChars() {
        return charactersDistrict.getChars();
    }

    public void setLeaderAndChars(EPlayer ep, int lc, int[] ic) {
        boolean leaderSet = false;
        ArrayList<CharacterCard> chars = new ArrayList<CharacterCard>();
        for (int c : ic) {
            CharacterCard card = CardDatabase.getCharacterInfo(c).createCard(ep, ep, charactersDistrict);
            chars.add(card);
            if (!leaderSet && c == lc) {
                charactersDistrict.setLeader(card);
                leaderSet = true;
            }
        }
        Collections.sort(chars);
        charactersDistrict.setChars(chars);
    }

    public void setChars(ArrayList<CharacterCard> chars) {
        charactersDistrict.setChars(chars);
    }

    public EventCard getEventCard() {
        return eventCardDistrict.getCard();
    }

    @SuppressWarnings("unchecked")
    public CardSet<Card> getDiscardPile() {
        return discardPileDistrict.getSet();
    }

    @SuppressWarnings("unchecked")
    public CardSet<Card> getHand() {
        return handDistrict.getSet();
    }

    public CharacterCard getLeader() {
        return charactersDistrict.getLeader();
    }

    public void setLeader(CharacterCard leader) {
        charactersDistrict.setLeader(leader);
    }

    @SuppressWarnings("unchecked")
    public CardSet<SupportCard> getLeaderAttachment() {
        return leaderAttachmentDistrict.getSet();
    }

    @SuppressWarnings("unchecked")
    public CardSet<Card> getLibrary() {
        return libraryDistrict.getSet();
    }

    @SuppressWarnings("unchecked")
    public CardSet<SpellCard> getReserved() {
        return reservedDistrict.getSet();
    }

    @SuppressWarnings("unchecked")
    public ActivatedDistrict getActivatedDistrict() {
        return activatedDistrict;
    }

    public BattleDistrict getBattleCardDistrict() {
        return battleCardDistrict;
    }

    public DiscardPileDistrict getDiscardPileDistrict() {
        return discardPileDistrict;
    }

    public EventDistrict getEventCardDistrict() {
        return eventCardDistrict;
    }

    public HandDistrict getHandDistrict() {
        return handDistrict;
    }

    public LeaderAttachmentDistrict getLeaderAttachmentDistrict() {
        return leaderAttachmentDistrict;
    }

    public LibraryDistrict getLibraryDistrict() {
        return libraryDistrict;
    }

    public ReservedDistrict getReservedDistrict() {
        return reservedDistrict;
    }

    public CharactersDistrict getCharactersDistrict() {
        return charactersDistrict;
    }

    public void clear() {
        handDistrict.clear();
        libraryDistrict.clear();
        discardPileDistrict.clear();
        reservedDistrict.clear();
        activatedDistrict.clear();
        leaderAttachmentDistrict.clear();
        battleCardDistrict.clear();
        eventCardDistrict.clear();
    }

    public int getCharCount() {
        ArrayList<Integer> c = new ArrayList<Integer>(4);
        for (int i = 0; i < 4; ++i) {
            c.add(new Integer(charactersDistrict.getChars().get(i).isNo(8101) ? 8100 : charactersDistrict.getChars().get(i).getInfo().getCardNo()));
        }

        HashSet<Integer> v = new HashSet<Integer>(4);
        for (Integer i : c) {
            v.add(i);
        }

        return v.size();
    }

    public boolean isLeaderContainsAttribute(String attr) {
        String[] attrs;
        // ぬえ
        if (charactersDistrict.getLeader().getInfo().isNo(3500)) {
            attrs = gi.getOppositeField(this).charactersDistrict.getLeader().getInfo().getDesignations().split(" ");
        } else {
            attrs = charactersDistrict.getLeader().getInfo().getDesignations().split(" ");
        }
        for (String s : attrs) {
            if (s.equals(attr)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSpellInDiscardPile() {
        return getDiscardPile().countType(ECardType.SPELL) > 0;
    }

    public boolean hasSupportInDiscardPile() {
        return getDiscardPile().countType(ECardType.SUPPORT) > 0;
    }

    /**
     * Count support on field. -1 for any support.
     *
     * @param cardNo
     * @return
     */
    public int countSupport(int cardNo) {
        int cnt = 0;

        CardSet<SpellCard> spells = new CardSet<SpellCard>();
        spells.addAll(getReserved());
        spells.addAll(getActivated());
        spells.add(getBattleCard());

        for (Card c : spells) {
            SpellCard sc = (SpellCard) c;
            if (sc.isAttached() && (sc.getAttachedDontThrow().isNo(cardNo) || cardNo < 0)) {
                ++cnt;
            }
        }

        cnt += getLeaderAttachment().counts(cardNo);

        return cnt;
    }

    public int countCharSupport(int charid, boolean onlyCountLeaderSupport) {
        int cnt = 0;

        for (Card c : getLeaderAttachment()) {
            if (c.getInfo().isOwnedBy(charid)) {
                ++cnt;
            }
        }

        if (onlyCountLeaderSupport) {
            return cnt;
        }

        try {
            if (getBattleCard().getAttached().getInfo().isOwnedBy(charid)) {
                ++cnt;
            }
        } catch (NotAttachedException ex) {
            // Do nothing
        }

        for (Card c : getReserved()) {
            SpellCard sc = (SpellCard) c;
            try {
                if (sc.getAttached().getInfo().isOwnedBy(charid)) {
                    ++cnt;
                }
            } catch (NotAttachedException ex) {
            }
        }

        for (Card c : getActivated()) {
            SpellCard sc = (SpellCard) c;
            try {
                if (sc.getAttached().getInfo().isOwnedBy(charid)) {
                    ++cnt;
                }
            } catch (NotAttachedException ex) {
            }
        }

        return cnt;
    }

    /**
     * Count spell amount on a player's field.
     *
     * @param acter the player
     * @param cardNo the card to count. -1 to count all spells.
     * @return the total amount of cardlabels
     */
    public int countSpellOnField(int cardNo) {
        return countSpell(cardNo, ERegion.RESERVED) + countSpell(cardNo, ERegion.ACTIVATED) + countSpell(cardNo, ERegion.BATTLE);
    }

    /**
     * Count spell amount at a player's specified region.
     *
     * @param acter
     * @param cardNo cardNo the card to count. -1 to count all spells.
     * @param region region to count
     * @return the total amount of cardlabels
     */
    public int countSpell(int cardNo, ERegion region) {
        int ret = 0;
        switch (region) {
            case ACTIVATED:
                return getActivated().counts(cardNo);
            case RESERVED:
                return getReserved().counts(cardNo);
            case BATTLE:
                if (getBattleCard().isNo(cardNo) || cardNo < 0) {
                    return 1;
                } else {
                    return 0;
                }
            case DISCARD_PILE:
                return getDiscardPile().countSpell(cardNo);
        }
        return ret;
    }

    public int countDoll() {
        int ret = 0;
        for (Card c : getLeaderAttachment()) {
            if (c.getInfo().getRuleText().contains(FTool.getLocale(170))) {
                ret++;
            }
        }
        return ret;
    }

    public int countIntrusment() {
        int ret = 0;
        for (Card c : getLeaderAttachment()) {
            if (c.getInfo().getRuleText().contains(FTool.getLocale(224))) {
                ret++;
            }
        }
        return ret;
    }

    public boolean isLeaderSpellBattling() {
        return getBattleCard().getInfo().isOwnedBy(charactersDistrict.getLeader().getInfo().getCharId());
    }

    /**
     * Get the level of indicated player and character.
     *
     * @param acter The indicated player.
     * @param character Indicated character's card's number. Prismriver is 800x
     * and Keine is 810x.
     * @return The level of indicated character.
     */
    public int getCharacterLevel(int charId) {
        int ret = 0;
        for (int i = 0; i < 4; ++i) {
            if (charactersDistrict.getChars().get(i).getInfo().getCharId() == charId) {
                ret += 1;
            }
        }
        return ret;
    }

    public int getLeaderLevel() {
        return getCharacterLevel(charactersDistrict.getLeader().getInfo().getCharId());
    }

    public void clear(Player player, GameCore core) {
        if (player.getDeck() == null) {
            return;
        }
        handDistrict.clear();
        libraryDistrict.clear();
        discardPileDistrict.clear();
        activatedDistrict.clear();
        reservedDistrict.clear();
        eventCardDistrict.clear();
        battleCardDistrict.clear();
        leaderAttachmentDistrict.clear();
        if (!getLeader().isNull()) {
            libraryDistrict.loadDeck(player.getDeck(), core);
            shuffle();
            for (int i = 0; i < 6; ++i) {
                draw(core);
            }
            getLeader().updateLabel(core);
            getLeader().getLabel().setPopupMenu(charactersDistrict.getChars());
        }
    }

    public void shuffle() {
        libraryDistrict.shuffle();
        FTool.playSound("shuffle.wav");
    }

    /**
     * Draw from the TAIL of library array
     */
    public void draw(GameCore core) {
        if (getLibrary().size() <= 0) {
            return;
        }
        Card card = libraryDistrict.simplePop();
        handDistrict.add(card);
        card.updateLabel(core);
    }

    public District getDistrcit(ERegion region) {
        switch (region) {
            case ACTIVATED:
                return activatedDistrict;
            case BATTLE:
                return battleCardDistrict;
            case RESERVED:
                return reservedDistrict;
            case EVENT:
                return eventCardDistrict;
            case LEADER_ATTACHMENTS:
                return leaderAttachmentDistrict;
            case SCENE:
                return gi.getSceneDistrict();
            case HAND:
                return handDistrict;
            case DISCARD_PILE:
                return discardPileDistrict;
            case LIBRARY:
                return libraryDistrict;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public void setEventCard(EventCard card) {
        eventCardDistrict.setCard(card);
    }
}
