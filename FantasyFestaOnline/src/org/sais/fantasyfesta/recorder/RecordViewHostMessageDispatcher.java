/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.recorder;

import java.io.BufferedReader;
import java.io.IOException;
import org.sais.fantasyfesta.core.MessageDispatcher;

/**
 *
 * @author Romulus
 */
public class RecordViewHostMessageDispatcher extends MessageDispatcher {

    RecordViewHostFSock mParent;

    public RecordViewHostMessageDispatcher(RecordViewHostFSock parentSock) {
        super(parentSock);
        mParent = parentSock;
    }

    @Override
    public void dispatch(String ins) {
        dispatchViewhost(ins);
    }

    @SuppressWarnings("empty-statement")
    private boolean dispatchViewhost(String ins) {
        BufferedReader reader = mParent.getReader();
        String line, cmd;
        cmd = "$GIVEMECURRENT:";
        if (ins.startsWith(cmd)) {
            try {
                for (line = reader.readLine(); !line.startsWith("$CURRENT:"); line = reader.readLine()) {
                    if (line == null) {
                        sendDefault("$RECORDFINISH:");
                        return true;
                    }
                }
                sendDefault(line);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        cmd = "$NEXTACTION:";
        if (ins.startsWith(cmd)) {
            try {
                boolean stop = false;
                do {
                    line = reader.readLine();
                    if (line == null) {
                        sendDefault("$RECORDFINISH:");
                        return true;
                    }
                    if (line.contains(":$NOLOGMSG:") || line.contains(":$PASS:") || line.contains(":$CHAT:") || line.contains(":$MSG:")) {
                        stop = true;
                    }
                    sendDefault(line);
                } while (!stop);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        cmd = "$SKIPTURN:";
        if (ins.startsWith(cmd)) {
            try {
                do {
                    line = reader.readLine();
                    if (line == null) {
                        sendDefault("$RECORDFINISH:");
                        return true;
                    }
                    sendDefault(line);
                } while (!line.contains(":$NEXT:"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    private void sendDefault(String message) {
        send(mParent.getSocket(), message);
    }
}
