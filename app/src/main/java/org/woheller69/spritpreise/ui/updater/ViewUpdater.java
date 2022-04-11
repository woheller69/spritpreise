package org.woheller69.spritpreise.ui.updater;

import org.woheller69.spritpreise.database.Station;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 24.01.2017.
 */

public class ViewUpdater {
    private static List<IUpdateableCityUI> subscribers = new ArrayList<>();

    public static void addSubscriber(IUpdateableCityUI sub) {
        if (!subscribers.contains(sub)) {
            subscribers.add(sub);
        }
    }

    public static void removeSubscriber(IUpdateableCityUI sub) {
        subscribers.remove(sub);
    }

    public static void updateStations(List<Station> stations, int cityID) {
        ArrayList<IUpdateableCityUI> subcopy = new ArrayList<>(subscribers);
        for (IUpdateableCityUI sub : subcopy) {
            sub.processUpdateStations(stations,cityID);
        }
    }
}
