/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfestaproxy;

/**
 *
 * @author Romulus
 */
public class Watcher {

    private int id;
    private String name;
    private ProxyServer.ToWatcherTSock mSock;

    public Watcher(String name, int id, ProxyServer.ToWatcherTSock sock) {
        this.id = id;
        this.name = name;
        this.mSock = sock;
    }

    @Override
    public String toString() {
        return id + " " + name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProxyServer.ToWatcherTSock getSock() {
        return mSock;
    }

    void closeSocket() {
        mSock.closeSocket();
    }
}
