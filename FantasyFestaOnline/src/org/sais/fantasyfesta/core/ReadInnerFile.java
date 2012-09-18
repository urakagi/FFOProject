/*
 * ReadInnerFile.java
 *
 * Created on 2007/2/14 8:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.sais.fantasyfesta.core;

import java.io.*;
import java.net.URL;

/**
 *
 * @author Romulus
 */
public class ReadInnerFile {
   public InputStreamReader stream;
   public URL u;
   
   public ReadInnerFile(String filename) {
      try {
        u = this.getClass().getClassLoader().getResource(filename);
        stream = u == null ? null : new InputStreamReader(u.openStream(), "Unicode");
      } catch (Exception ex) {
         ex.printStackTrace();
      }
   }
   
   public static URL getURL(String filename) {
      return new ReadInnerFile(filename).u;
   }
   
   public static InputStreamReader getStream(String filename) {
      return new ReadInnerFile(filename).stream;
   }
}
