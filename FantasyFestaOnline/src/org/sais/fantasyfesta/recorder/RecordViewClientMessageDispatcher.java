/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.recorder;

import javax.swing.JOptionPane;
import org.sais.fantasyfesta.core.ClientMessageDispatcher;
import org.sais.fantasyfesta.core.Startup;

/**
 *
 * @author Romulus
 */
public class RecordViewClientMessageDispatcher extends ClientMessageDispatcher {

    public RecordViewClientMessageDispatcher(Startup startup, RecordViewClientSocketThread parentSock) {
        super(startup, parentSock);
    }

    @Override
    public void dispatch(String ins) {
        if (dispatchRecordView(ins)) {
            return;
        }
        if (dispatchClient(ins)) {
            return;
        }
        if (dispatchWatcher(ins)) {
            return;
        }
        dispathCommon(ins, true);
    }

    private boolean dispatchRecordView(String ins) {
        String cmd;
        cmd = "$RECORDSTART:";
        if (ins.startsWith(cmd)) {
            ((RecordViewClientSocketThread) mParentSocketThread).createCore();
            // mParentSocketThread.start();
            return true;
        }
        cmd = "$RECORDFINISH:";
        if (ins.startsWith(cmd)) {
            JOptionPane.showMessageDialog(null, "Record finished.");
            return true;
        }
        return false;
    }
}
