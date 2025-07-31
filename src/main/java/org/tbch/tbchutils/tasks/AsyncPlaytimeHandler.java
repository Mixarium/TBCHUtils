package org.tbch.tbchutils.tasks;

import org.tbch.tbchutils.memory.PlaytimeMemory;

import java.util.ArrayList;
import java.util.List;

public class AsyncPlaytimeHandler implements Runnable{
    private final List<String> usernames;

    public AsyncPlaytimeHandler(List<String> usernames) {
        //this.usernames = usernames;
        // line below is meant as a workaround for ConcurrentModificationException
        this.usernames = new ArrayList<>(usernames);
    }

    @Override
    public void run() {
        for (String username : usernames) {
            PlaytimeMemory.setLastTimestampSaved(username, true);
        }
    }
}
