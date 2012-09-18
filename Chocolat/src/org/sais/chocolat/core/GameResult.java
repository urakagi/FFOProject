/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sais.chocolat.core;

/**
 *
 * @author Romulus
 */
public class GameResult {
  public Participant opp;
  public int result;

  public GameResult() {}

  public GameResult(Participant opp_, int result_) {
    opp = opp_;
    result = result_;
  }

  public static final int RESULT_WON = 3;
  public static final int RESULT_LOSS = 0;
  public static final int RESULT_DRAW = 1;
}
