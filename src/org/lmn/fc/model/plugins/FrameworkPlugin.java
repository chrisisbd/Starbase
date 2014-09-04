package org.lmn.fc.model.plugins;

import org.lmn.fc.common.os.OperatingSystem;
import org.lmn.fc.common.support.jmx.HttpMBeanAdaptor;
import org.lmn.fc.frameworks.starbase.events.FrameworkChangedListener;
import org.lmn.fc.model.datatypes.DegMinSecInterface;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.poi.LineOfInterest;
import org.lmn.fc.model.xmlbeans.poi.PointOfInterest;

import javax.management.MBeanServer;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * The FrameworkPlugin Interface.
 */

public interface FrameworkPlugin extends AtomPlugin
    {
    // Some fairly arbitrary size limits
    int MAX_TASKS = 20;
    int MAX_APPLICATIONS = 10;

    //----------------------------------------------------------------------------------------------
    // JMX MBeans

    void initialiseMBeanServer();

    void registerAtom(AtomPlugin atom);

    void unregisterAtom(AtomPlugin atom);

    MBeanServer getMBeanServer();

    void setMBeanServer(MBeanServer server);

    HttpMBeanAdaptor getHttpAdaptor();

    void setHttpAdaptor(HttpMBeanAdaptor adaptor);

    //----------------------------------------------------------------------------------------------
    // Miscellaneous

    OperatingSystem getOperatingSystem();

    void setOperatingSystem(OperatingSystem os);

    //----------------------------------------------------------------------------------------------
    // XML Persistence

    List<String> getRecursionLevels();

    void setRecursionLevels(String[] levels);

    String getLanguageISOCode();

    void setLanguageISOCode(String code);

    String getCountryISOCode();

    void setCountryISOCode(String code);

    //----------------------------------------------------------------------------------------------
    // The below have been incorporated into Framework Metadata

    String getTimeZoneCode();

    void setTimeZoneCode(String code);

    DegMinSecInterface getLongitude();

    void setLongitude(DegMinSecInterface longitude);

    DegMinSecInterface getLatitude();

    void setLatitude(DegMinSecInterface latitude);

    double getHASL();

    void setHASL(double hasl);

    // The above have been incorporated into Framework Metadata
    //----------------------------------------------------------------------------------------------


    boolean isAutoUpdate();

    void setAutoUpdate(boolean auto);

    String getSplashScreenFilename();

    void setSplashScreenFilename(String filename);


    /**********************************************************************************************/
    /* Metadata                                                                                   */
    /***********************************************************************************************
     * Get the List of Metadata for the Framework.
     *
     * @return List<Metadata>
     */

    List<Metadata> getFrameworkMetadata();


    /**********************************************************************************************/
    /* PointOfInterest                                                                            */
    /***********************************************************************************************
     * Add a PointOfInterest to the Framework.
     *
     * @param poi
     */

    void addPointOfInterest(PointOfInterest poi);


    /***********************************************************************************************
     * Remove all PointsOfInterest from the Framework.
     */

    void clearPointsOfInterest();


    /***********************************************************************************************
     * Get the Points of Interest for the Framework.
     *
     * @return List<PointOfInterest>
     */

    List<PointOfInterest> getPointOfInterestList();


    /***********************************************************************************************
     * Set the Points of Interest for the Framework.
     *
     * @param pois
     */

    void setPointOfInterestList(List<PointOfInterest> pois);


    /**********************************************************************************************/
    /* LineOfInterest                                                                             */
    /***********************************************************************************************
     * Add a LineOfInterest to the Framework.
     *
     * @param loi
     */

    void addLineOfInterest(LineOfInterest loi);


    /***********************************************************************************************
     * Remove all LinesOfInterest from the Framework.
     */

    void clearLinesOfInterest();


    /***********************************************************************************************
     * Get the list of LinesOfInterest.
     *
     * @return List<LineOfInterest>
     */

    List<LineOfInterest> getLineOfInterestList();


    /***********************************************************************************************
     * Set the Lines of Interest for the Framework.
     *
     * @param lois
     */

    void setLineOfInterestList(List<LineOfInterest> lois);


    /**********************************************************************************************/
    /* Mapping                                                                                    */
    /***********************************************************************************************
     *
     * @return String
     */
    String getMapFilename();

    void setMapFilename(String filename);

    DegMinSecInterface getMapTopLeftLongitude();

    void setMapTopLeftLongitude(DegMinSecInterface longitude);

    DegMinSecInterface getMapTopLeftLatitude();

    void setMapTopLeftLatitude(DegMinSecInterface latitude);

    DegMinSecInterface getMapBottomRightLongitude();

    void setMapBottomRightLongitude(DegMinSecInterface longitude);

    DegMinSecInterface getMapBottomRightLatitude();

    void setMapBottomRightLatitude(DegMinSecInterface latitude);


    /***********************************************************************************************
     * Get the filename of the Framework Licence HTML file.
     *
     * @return String
     */

    String getLicenceFilename();


    /***********************************************************************************************
     * Set the filename of the Framework Licence HTML file.
     *
     * @param filename
     */

    void setLicenceFilename(String filename);


    /***********************************************************************************************
     * Notify all listeners of FrameworkChangedEvents.
     *
     * @param eventsource
     */

    void notifyFrameworkChangedEvent(Object eventsource);


    /***********************************************************************************************
     * Get the FrameworkChanged Listeners (mostly for testing).
     *
     * @return Vector<FrameworkChangedListener>
     */

    Vector<FrameworkChangedListener> getFrameworkChangedListeners();


    /***********************************************************************************************
     * Add a listener for this event.
     *
     * @param listener
     */

    void addFrameworkChangedListener(FrameworkChangedListener listener);


    /***********************************************************************************************
     * Remove a listener for this event.
     *
     * @param listener
     */

    void removeFrameworkChangedListener(FrameworkChangedListener listener);
    }
