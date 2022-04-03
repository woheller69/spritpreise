package org.woheller69.spritpreise.api;

import org.woheller69.spritpreise.database.Station;

/**
 * This interface defines the frame of the functionality to extract information which
 * is returned by some API.
 */
public interface IDataExtractor {


    boolean wasCityFound(String data);


    Station extractStation(String data);


}
