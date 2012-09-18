/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import javax.swing.Timer;

/**
 *
 * @author Romulus
 */
public class SwingUpdater extends Timer {

    private ActionListener parent;
    /*private */
    ArrayList<UpdateTask> cQueue = new ArrayList<UpdateTask>();

    SwingUpdater(ActionListener parent) {
        super(20, parent);
        this.parent = parent;
        setInitialDelay(0);
        setCoalesce(true);
        start();
    }

    void add(UpdateTask task) {
        Collections.synchronizedList(cQueue).add(task);
    }

    public UpdateTask pop() {
        return cQueue.size() > 0 ? Collections.synchronizedList(cQueue).remove(0) : null;
    }

    public static class UpdateTask {

        TaskList task;
        ArrayList<Object> parameters = new ArrayList<Object>();

        UpdateTask(TaskList task) {
            this.task = task;
        }

        public void addParameter(Object para) {
            parameters.add(para);
        }

        public enum TaskList {
            showLibrary,
            showRegion,
        }
    }
}
