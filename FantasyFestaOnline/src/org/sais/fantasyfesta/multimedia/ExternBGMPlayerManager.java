/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.sais.fantasyfesta.multimedia;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sais.fantasyfesta.tool.FTool;

/**
 *
 * @author user
 */
public class ExternBGMPlayerManager implements BGMPlayable {

    String externBGMPlayer = "";
    Process process = null;
	Runtime runtime=Runtime.getRuntime();

    public ExternBGMPlayerManager(File externBGMPlayer)
    {
        this.externBGMPlayer = externBGMPlayer.getAbsolutePath();
    }
     public ExternBGMPlayerManager(String externBGMPlayer)
    {
        this.externBGMPlayer = externBGMPlayer;
    }


    @Override
    public void play(String fname) {

        	 // 		externBGMPlayer = "E:\\Program Files\\foobar2000\\foobar2000.exe";
			System.out.println("player: "+externBGMPlayer);

			String playerPath = "\""+ externBGMPlayer+"\"";
			String musicPath ="\""+ fname+"\"";
			String[] commandArgs={playerPath,musicPath};
		    try {

		    	if(process!=null)
		    	{
		    		process.destroy();

		    		//System.out.println(process.waitFor());
		    		process = null;
		    		Thread.sleep(50);
		    	}
//				process = runtime.exec("cmd /c start/min "+ playerPath+" "+playerPath+" "+musicPath);
	    	process = runtime.exec(commandArgs);
		    } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }

    @Override
    public void stopplay() {

      	play(" ");
    }

    @Override
    public void close() {
        		  	if(process!=null)
		    	{
            try {
                process.destroy();
                System.out.println(process.waitFor());
                process = null;

            } catch (InterruptedException ex) {
                Logger.getLogger(ExternBGMPlayerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
		    	}
    }

    	  public static void main(String args[])
	  {
		  ExternBGMPlayerManager bp = new ExternBGMPlayerManager(FTool.readConfig("externbgmplayer"));
		  bp.play("F:\\game\\海猫\\Umineko2\\BGM\\Answer.ogg");
		  System.out.println("Playing...");
		  try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			bp.play(" ");


		  try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bp.play("F:\\game\\海猫\\Umineko2\\BGM\\worldenddominator.ogg");

		  try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

					bp.stopplay();
                    bp.close();

	  }

}
