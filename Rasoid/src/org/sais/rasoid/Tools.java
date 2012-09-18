package org.sais.rasoid;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sais.rasoid.card.Card;
import org.sais.rasoid.card.CardText_GSU.CardText_Character_GSU;
import org.sais.rasoid.card.CardText_GSU.CardText_Event_GSU;
import org.sais.rasoid.card.CardText_GSU.CardText_Spell_GSU;
import org.sais.rasoid.card.CardText_GSU.CardText_Support_GSU;
import org.sais.rasoid.dummy.CardViewer;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;

public class Tools {

	private static HashMap<Integer, Card> cdata = new HashMap<Integer, Card>();
	private static ArrayList<Integer> mIndices = new ArrayList<Integer>();
	public static HashMap<Integer, String> pron = new HashMap<Integer, String>();
	public static String[] loc = new String[1000];
	public static boolean customizedIcon = false;
	public static CardViewer cardviewer;
	public static CardViewer deckeditorcardviewer;
	public static Object sLock = new Object();

	private static String TAG = "Rasoid - Tools";

	static public Card getCard(int cardnum) {
		if (cardnum >= 0) {
			return cdata.get(cardnum);
		} else {
			return null;
		}
	}

	static public Set<Integer> listCards() {
		return cdata.keySet();
	}

	static public String getLoc(int index) {
		return loc[index];
	}

	/**
	 * Get the character index. Reimu=0.
	 * 
	 * @param charCardNum
	 *            The card number of character card.
	 * @return The character index.
	 */
	static public int getCharIndex(int charCardNum) {
		int ret = charCardNum / 100;
		if (ret == 80) {
			return 16;
		}
		if (ret == 81) {
			return 18;
		}
		return ret - 1;
	}

	/**
	 * Get the name of a char, used in the level condition text.
	 * 
	 * @param charindex
	 *            Character's index. Reimu=1, Prismriver=17, etc.
	 * @return Character's name.
	 */
	static public String getCharConditionName(int charindex) {
		if (charindex >= 0 && charindex <= 26) {
			return getLoc(130 + charindex);
		} else if (charindex >= 27) {
			return getLoc(194 + charindex - 27);
		} else {
			return "";
		}
	}

	static public final byte[] itoba(int value) {
		return new byte[] { (byte) (value >>> 24), (byte) (value >> 16 & 0xff),
				(byte) (value >> 8 & 0xff), (byte) (value & 0xff) };
	}

	static public final int batoi(byte[] value) {
		return (value[0] & 0xff) * 0x1000000 + (value[1] & 0xff) * 0x10000
				+ (value[2] & 0xff) * 0x100 + (value[3] & 0xff);
	}

	public static void readCardBase(Context ctx) throws FileNotFoundException,
			IOException { // Read Card Data Base
		BufferedReader database = new BufferedReader(new InputStreamReader(ctx
				.getResources().openRawResource(R.raw.carddatabase), "UTF-8"),
				8192);
		execReadCardDataBase(database);
	}

	private static void execReadCardDataBase(BufferedReader database) {
		Card card = null;
		// AutoMech mech = null;
		// ChoiceEffect eff = null;
		cdata = new HashMap<Integer, Card>();
		mIndices = new ArrayList<Integer>();

		int step = 0;
		int lastcardnum = -1;
		String line = "";

		// final int STEPBASE_AUTOMECH = 100;
		// final int STEPBASE_CHOICE_EFFECT = 1000;

		try {
			for (line = database.readLine(); line != null; line = database
					.readLine()) {
				if (line.startsWith("#")) {
					step = 0;
					cdata.put(card.cardnum, card);
					continue;
				}
				// Disabling AutoMech and ChoiceEffect here
				if (line.startsWith("~") || line.startsWith("*")) {
					for (String skip = database.readLine(); !skip
							.startsWith("#"); skip = database.readLine());
					step = 0;
					cdata.put(card.cardnum, card);
					continue;
				}
				switch (step) {
				case 0:
					card = new Card();
					card.cardnum = Integer.parseInt(line);
					step++;
					break;
				case 1:
					card.cardname = line.toString();
					step++;
					break;
				case 2:
					card.cardtype = Integer.parseInt(line);
					step++;
					break;
				case 3:
					String[] atr;
					switch (card.cardtype) {
					case 0: // character
						card.cardtext = new CardText_Character_GSU();
						card.cardtext.ruleText = "";
						atr = line.split("-");
						card.cardtext.attribute = atr[0];
						card.cardtext.hp = Integer.parseInt(atr[1]);
						card.cardtext.dodge = Integer.parseInt(atr[2]);
						card.cardtext.kesshi = Integer.parseInt(atr[3]);
						break;
					case 1: // spell
						card.cardtext = new CardText_Spell_GSU();
						card.cardtext.ruleText = "";
						atr = line.split("-");
						card.cardtext.attack = Integer.parseInt(atr[0]);
						card.cardtext.intercept = Integer.parseInt(atr[1]);
						card.cardtext.hit = Integer.parseInt(atr[2]);
						card.cardtext.bullet = Integer.parseInt(atr[3]);
						break;
					case 2: // sulineort
						atr = line.split("-");
						card.cardtext = new CardText_Support_GSU();
						card.cardtext.ruleText = "";
						card.cardtext.attachment = Integer.parseInt(line);
						break;
					case 3: // event
						atr = line.split("-");
						card.cardtext = new CardText_Event_GSU();
						card.cardtext.ruleText = "";
						card.cardtext.timing = Integer.parseInt(line);
						break;
					}
					line = "";
					step++;
					break;
				case 4:
					if (card.cardtype != 0) {
						card.cardtext.condition = line;
					}
					line = "";
					step++;
					break;
				case 5:
					if (card.cardtype != 0) {
						card.cardtext.mp = Integer.parseInt(line);
					}
					line = "";
					step++;
					break;
				case 6:
					if (!line.equals(null)) {
						card.cardtext.ruleText += line + "\n";
					}
					line = "";
					lastcardnum = card.cardnum;
					break;
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "step=" + step + ", count=" + cdata.size()
					+ ", lastcarnum=" + lastcardnum, ex);
			Log.e(TAG, "line=" + line, ex);
			ex.printStackTrace();
		}
		
		for (Integer i : cdata.keySet()) {
			mIndices.add(i);
		}
		Collections.sort(mIndices);
	}
	
	public static ArrayList<Integer> getIndices() {
		return mIndices;
	}

	public static void readPronunciation(Context ctx) {
		try {
			pron.clear();
			BufferedReader in = new BufferedReader(new InputStreamReader(ctx
					.getResources().openRawResource(R.raw.pronunciation)), 8192);
			String line = in.readLine();
			while (line != null) {
				String[] s = line.split("<>");
				if (s.length < 2) {
					break;
				}
				pron.put(Integer.parseInt(s[0]), s[1]);
				line = in.readLine();
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static String getPronunciation(Integer cardnum) {
		return pron.get(cardnum) != null ? pron.get(cardnum) : "";
	}

	/*
	 * public static void showCard(int cardnum, JTextPane SCArea) { try {
	 * SCArea.setText(""); if (cardnum <= 0) {
	 * SCArea.setBackground(Color.white); return; } Card scard = cdata[cardnum];
	 * String dp = ""; SimpleAttributeSet set = new SimpleAttributeSet();
	 * SCArea.setCharacterAttributes(set, true); Document doc =
	 * SCArea.getStyledDocument();
	 * 
	 * String pronunciation = getPronunciation(scard.cardnum);
	 * doc.insertString(doc.getLength(), pronunciation + "\n", set);
	 * 
	 * StyleConstants.setFontSize(set, 16); StyleConstants.setBold(set, true);
	 * doc.insertString(doc.getLength(), scard.cardname + "\n", set);
	 * StyleConstants.setFontSize(set, 12); StyleConstants.setBold(set, false);
	 * if (scard.cardtype == 0) { doc.insertString(doc.getLength(), loc[91] +
	 * scard.cardtext.attribute + "\n", set); }
	 * 
	 * doc.insertString(doc.getLength(), scard.Dump(loc) + "\n\n", set); switch
	 * (scard.cardtype) { case 0: SCArea.setBackground(new Color(0xFFD0D0));
	 * break; case 1: SCArea.setBackground(new Color(0xD0D0FF)); break; case 2:
	 * SCArea.setBackground(new Color(0xEEEEA0)); break; case 3:
	 * SCArea.setBackground(new Color(0xD0FFD0)); break; default:
	 * SCArea.setBackground(Color.white); }
	 * 
	 * String source = scard.cardtext.ruleText; for (int idx = 0; idx <
	 * source.length(); ++idx) { switch (source.charAt(idx)) { case '$':
	 * doc.insertString(doc.getLength(), dp, set); dp = "";
	 * StyleConstants.setForeground(set, Color.red); break; case '%':
	 * doc.insertString(doc.getLength(), dp, set); dp = "";
	 * StyleConstants.setForeground(set, new Color(0x209020)); break; case '^':
	 * doc.insertString(doc.getLength(), dp, set); dp = "";
	 * StyleConstants.setForeground(set, Color.blue); break; case '@':
	 * doc.insertString(doc.getLength(), dp, set); dp = "";
	 * StyleConstants.setForeground(set, Color.magenta); break; case '*':
	 * doc.insertString(doc.getLength(), dp, set); dp = "";
	 * StyleConstants.setForeground(set, new Color(0x902000)); break; case '&':
	 * doc.insertString(doc.getLength(), dp, set); dp = "";
	 * StyleConstants.setForeground(set, Color.black); break; case '\\': idx++;
	 * dp = dp + source.charAt(idx); break; default: dp = dp +
	 * source.charAt(idx); } } doc.insertString(doc.getLength(), dp + "\n",
	 * set);
	 * 
	 * if (scard.cardnum == 8000) { doc.insertString(doc.getLength(),
	 * "#1700/1\n", set); } else if (scard.cardnum == 8001) {
	 * doc.insertString(doc.getLength(), "#1700/2\n", set); } else if
	 * (scard.cardnum == 8002) { doc.insertString(doc.getLength(), "#1700/3\n",
	 * set); } else if (scard.cardnum == 8100) {
	 * doc.insertString(doc.getLength(), "#1900/1\n", set); } else if
	 * (scard.cardnum == 8101) { doc.insertString(doc.getLength(), "#1900/2\n",
	 * set); } else { doc.insertString(doc.getLength(), "No." + scard.cardnum +
	 * "\n", set); }
	 * 
	 * SCArea.setCaretPosition(0); } catch (Exception ex) {
	 * ex.printStackTrace(); } }
	 */

	public static String getNowTimeString(String format) {
		SimpleDateFormat D = new SimpleDateFormat(format);
		Date Now = new Date();
		long Time = Now.getTime();
		Time = Time + 100000L;
		return D.format(new Date(Time));
	}

	public static boolean XOR(boolean a, boolean b) {
		if ((a && b) || (!a && !b)) {
			return false;
		} else {
			return true;
		}
	}

	static long[] timer = new long[2];
	static int nowtimer = 0;

	static {
		timer[0] = 0;
		timer[1] = 0;
	}

	static void startTimer() {
		timer[nowtimer] = new Date().getTime();
	}

	static void switchTimer() {
		long interval = new Date().getTime() - timer[nowtimer];
		System.out.println("Interval is " + interval + " miliseconds.");
		nowtimer = nowtimer == 0 ? 1 : 0;
		timer[nowtimer] = new Date().getTime();
	}

	static long intervaltimer;

	public static long interval() {
		long now = new Date().getTime();
		long ret = now - intervaltimer;
		intervaltimer = now;
		return ret;
	}

	public static String parseLocale(int index, String[] arg) {
		String ret = loc[index];
		int i = 1;
		for (String s : arg) {
			ret = ret.replace("%" + i, s);
			++i;
		}
		return ret;
	}

	public static boolean isURLAvailable(String url) {
		boolean result = false;
		int count = 0;
		int state = -1;

		try {

			if (url == null || url.length() < 0) {
				result = false;
			}
			while (count < 5) {
				URL urlStr = new URL(url);
				HttpURLConnection connection = (HttpURLConnection) urlStr
						.openConnection();
				state = connection.getResponseCode();
				if (state == 200) {
					result = true;
					break;
				} else {
					count++;
				}
			}

		} catch (MalformedURLException ex) {
			Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Tools.class.getName()).log(Level.SEVERE, null, ex);
		}

		return result;
	}

	static HashMap<Long, Long> sIntervals = new HashMap<Long, Long>();

	public static void logInterval(String tag, String message, Long threadId) {
		if (sIntervals.containsKey(threadId)) {
			long delta = System.currentTimeMillis() - sIntervals.get(threadId);
			Log.v(tag, message + " - " + delta + " ms.");
			sIntervals.put(threadId, System.currentTimeMillis());
		} else {
			Log.v(tag, message + " - Interval start.");
			sIntervals.put(threadId, System.currentTimeMillis());
		}
	}

	public static SpannableStringBuilder getVisualString(Context ctx,
			int cardnum) {
		Card c = Tools.getCard(cardnum);
		SpannableStringBuilder b = new SpannableStringBuilder();
		b.append(Tools.getPronunciation(cardnum) + "\n");
		TextAppearanceSpan pronSpan = new TextAppearanceSpan(ctx, android.R.style.TextAppearance_Small);
		b.setSpan(pronSpan, 0, b.length() - 1, 0);
		int nameStart = b.length();
		b.append(c.cardname + "\n");
		TextAppearanceSpan nameSpan = new TextAppearanceSpan(ctx, android.R.style.TextAppearance_Large);
		b.setSpan(nameSpan, nameStart, b.length() - 1, 0);
		b.append(c.dump(ctx) + "\n");
		String ruleText = c.cardtext.ruleText;
		
		int markStart = 0;
		boolean closed = true;
    	ForegroundColorSpan span = null;
        for (int idx = 0; idx < ruleText.length(); ++idx) {
        	char ch = ruleText.charAt(idx);
            switch (ch) {
                case '$':
                	if (closed) {
                		markStart = b.length();
                    	span = new ForegroundColorSpan(Color.rgb(0xFF, 0x50, 0x50));
                		closed = false;
                	}
                    break;
                case '%':
                	if (closed) {
                		markStart = b.length();
                    	span = new ForegroundColorSpan(Color.GREEN);
                    	closed = false;
                	}
                    break;
                case '^':
                	if (closed) {
                		markStart = b.length();
                    	span = new ForegroundColorSpan(Color.CYAN);
                    	closed = false;
                	}
                    break;
                case '@':
                	if (closed) {
                		markStart = b.length();
                    	span = new ForegroundColorSpan(Color.MAGENTA);
                    	closed = false;
                	}
                    break;
                case '&':
                	b.setSpan(span, markStart, b.length(), 0);
                	closed = true;
                    break;
                default:
                    b.append(ch);
            }
        }

		b.append("\nNo." + cardnum);

		return b;
	}

}

interface CardGameDisplayInterface {

	abstract void ShowTextinJTextPane();
}