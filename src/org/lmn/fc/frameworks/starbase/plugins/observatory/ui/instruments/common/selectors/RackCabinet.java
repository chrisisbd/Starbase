// Copyright 2000, 2001, 2002, 2003, 04, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2013
// Laurence Newell
// starbase@ukraa.com
// radio.telescope@btinternet.com
//
// This file is part of Starbase.
//
// Starbase is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Starbase is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Starbase.  If not, see http://www.gnu.org/licenses.

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.selectors;

import org.lmn.fc.common.exceptions.FrameworkException;
import org.lmn.fc.common.xml.XmlBeansUtilities;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.*;
import org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common.ObservatoryUIHelper;
import org.lmn.fc.model.plugins.AtomPlugin;
import org.lmn.fc.model.xmlbeans.groups.Definition;
import org.lmn.fc.model.xmlbeans.instruments.Instrument;
import org.lmn.fc.ui.components.BlankUIComponent;
import org.lmn.fc.ui.components.UIComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***************************************************************************************************
 * The RackCabinet implementation of the InstrumentSelector UIComponent.
 */

public final class RackCabinet extends UIComponent
                               implements InstrumentSelector
    {
    private static final long serialVersionUID = 4525792376674624671L;

    // Injections
    private final ObservatoryUIInterface hostUI;
    private final Definition definitionGroup;
    private final java.util.List<Instrument> listInstrumentsInGroup;
    private final Hashtable<String, ObservatoryInstrumentInterface> hashtableInstantiatedInstruments;
    private final AtomPlugin hostAtom;
    private final String strResourceKey;

    // Instrument Management
    private final Vector<ObservatoryInstrumentInterface> vecInstrumentsOnSelector;
    private ObservatoryInstrumentInterface selectedInstrument;

    // UI
    private Container container;


    /***********************************************************************************************
     * Construct a retro-styled RackCabinet InstrumentSelector for the specified Group Definition.
     *
     * @param hostui
     * @param group
     * @param instrumentsingroup
     * @param instantiatedinstrumentstable
     * @param hostatom
     * @param resourcekey
     */

    public RackCabinet(final ObservatoryUIInterface hostui,
                       final Definition group,
                       final List<Instrument> instrumentsingroup,
                       final Hashtable<String, ObservatoryInstrumentInterface> instantiatedinstrumentstable,
                       final AtomPlugin hostatom,
                       final String resourcekey)
        {
        super();

        if ((hostui == null)
            || (group == null)
            || (!XmlBeansUtilities.isValidXml(group))
            || (instrumentsingroup == null)
            || (instantiatedinstrumentstable == null)
            || (hostatom == null)
            || (!hostatom.validatePlugin())
            || (resourcekey == null)
            || (EMPTY_STRING.equals(resourcekey)))
            {
            throw new FrameworkException(EXCEPTION_PARAMETER_INVALID);
            }

        // Injections
        hostUI = hostui;
        definitionGroup = group;
        listInstrumentsInGroup = instrumentsingroup;
        hashtableInstantiatedInstruments = instantiatedinstrumentstable;
        hostAtom = hostatom;
        strResourceKey = resourcekey;

        vecInstrumentsOnSelector = new Vector<ObservatoryInstrumentInterface>(10);
        selectedInstrument = null;

        container = Box.createVerticalBox();
        }


    /***********************************************************************************************
     * Initialise this RackCabinet to show a rack of initialised Instruments.
     */

    public final void initialiseUI()
        {
        super.initialiseUI();

        // Prepare the UI
        removeAll();
        container = Box.createVerticalBox();
        getSelectorPanelContainer().add(Box.createVerticalStrut(MARGIN_TOP_HEIGHT));

        add(getSelectorPanelContainer(), BorderLayout.NORTH);
        setBackground(DEFAULT_COLOUR_CANVAS.getColor());

        //LOGGER.debugTimedEvent("RackCabinet.initialiseUI() NO INSTRUMENT SELECTED");
        setSelectedInstrument(null);
        getInstrumentsOnSelector().clear();

        // Add the ObservatoryInstruments to the RackCabinet
        if ((getHostUI() != null)
            && (getGroupDefinition() != null)
            && (getInstrumentsInGroup() != null))
            {
            // Add instantiated Instruments to the Rack
            addInstrumentsToSelector(getInstrumentsInGroup());

            if ((getInstrumentsOnSelector() != null)
                && (!getInstrumentsOnSelector().isEmpty()))
                {
                final Iterator<ObservatoryInstrumentInterface> iterInstruments;

                iterInstruments = getInstrumentsOnSelector().iterator();

                while (iterInstruments.hasNext())
                    {
                    final ObservatoryInstrumentInterface obsInstrument;

                    obsInstrument = iterInstruments.next();

                    // Only initialise once!
                    if (!InstrumentState.INITIALISED.equals(obsInstrument.getInstrumentState()))
                        {
                        //LOGGER.debugProtocolEvent("Initialising " + obsInstrument.getInstrument().getIdentifier());
                        obsInstrument.initialise();
                        }
                    }

                // Set an initial selection if possible
                if ((getHostUI() != null)
                    && (getInstrumentsOnSelector() != null)
                    && (getInstrumentsOnSelector().size() > 0)
                    && (getInstrumentsOnSelector().get(INDEX_INITIAL_INSTRUMENT_SELECTION) != null))
                    {
                    // All instruments have already been initialised
                    // Set the initial selection to the first instrument in the first Group of the rack
                    if (getSelectedInstrument() == null)
                        {
//                        LOGGER.debugProtocolEvent("RackCabinet.initialiseUI() SELECT INSTRUMENT INITIAL_SELECTION "
//                                                + getInstrumentsOnSelector().get(INDEX_INITIAL_INSTRUMENT_SELECTION).getInstrument().getName());
                        setSelectedInstrument(getInstrumentsOnSelector().get(INDEX_INITIAL_INSTRUMENT_SELECTION));
                        }
                    else
                        {
                        // Set the UIOccupant to the instrument's InstrumentPanel
//                        LOGGER.debugProtocolEvent("RackCabinet.initialiseUI() SELECT INSTRUMENT "
//                                                + getSelectedInstrument().getInstrument().getName());
                        }
                    }
                else
                    {
                    // There are no instruments at all...
                    //LOGGER.debugProtocolEvent("RackCabinet.initialiseUI() NO INSTRUMENT SELECTED");
                    setSelectedInstrument(null);
                    }

                revalidate();
                }
            }
        }


    /***********************************************************************************************
     * Run this UIComponent.
     * Add the ControlPanel of each ObservatoryInstrument in this Group to the RackCabinet.
     */

    public final void runUI()
        {
        // runUI() renders the InstrumentPanel for the selected Instrument
        // and shows the correct context menus and toolbar

        if (getInstrumentsOnSelector() != null)
            {
            if (!getInstrumentsOnSelector().isEmpty())
                {
                final Iterator<ObservatoryInstrumentInterface> iterInstruments;

                getSelectorPanelContainer().removeAll();
                //getSelectorPanelContainer().add(Box.createVerticalStrut(MARGIN_TOP_HEIGHT));

                iterInstruments = getInstrumentsOnSelector().iterator();

                while (iterInstruments.hasNext())
                    {
                    final ObservatoryInstrumentInterface obsInstrument;

                    obsInstrument = iterInstruments.next();

                    if (obsInstrument != null)
                        {
                        // Add the ControlPanel of each ObservatoryInstrument to the RackCabinet
                        getSelectorPanelContainer().add(obsInstrument.getSelectorPanel());
                        }
                    }

                // Set an initial selection if possible
                // All instruments have already been initialised
                // Set the initial selection to the first instrument in the first Group of the rack
                if ((getSelectedInstrument() == null)
                    && (getInstrumentsOnSelector().get(INDEX_INITIAL_INSTRUMENT_SELECTION) != null))
                    {
    //                LOGGER.debugProtocolEvent("RackCabinet.runUI() SELECT INSTRUMENT INITIAL_SELECTION "
    //                                        + getInstrumentsOnSelector().get(INDEX_INITIAL_INSTRUMENT_SELECTION).getInstrument().getName());
                    setSelectedInstrument(getInstrumentsOnSelector().get(INDEX_INITIAL_INSTRUMENT_SELECTION));
                    }
                else
                    {
                    // Set the UIOccupant to the instrument's InstrumentPanel
    //                LOGGER.debugProtocolEvent("RackCabinet.runUI() SELECTED INSTRUMENT "
    //                                        + getSelectedInstrument().getInstrument().getName());
                    }

                if (getHostUI() != null)
                    {
                    // Perform any extra actions to make it visible with runUI()
                    getHostUI().setUIOccupant(getSelectedInstrument().getInstrumentPanel());
                    }
                else
                    {
                    LOGGER.error("RackCabinet.runUI() Cannot make selection");
                    setSelectedInstrument(null);
                    }
                }
            else
                {
                //System.out.println("RackCabinet.runUI() No Instruments to render");

                setSelectedInstrument(null);
                getSelectorPanelContainer().removeAll();

                if (getHostUI() != null)
                    {
                    getHostUI().setUIOccupant(new BlankUIComponent(getGroupDefinition().getName()));
                    }
                else
                    {
                    LOGGER.error("RackCabinet.runUI() Cannot draw empty Selector");
                    }
                }
            }
        else
            {
            // NULL
            LOGGER.error("RackCabinet NULL INSTRUMENTS ON SELECTOR");
            }

        super.runUI();
        revalidate();
        }


    /***********************************************************************************************
     * Stop this UIComponent.
     */

    public final void stopUI()
        {
        if ((getInstrumentsOnSelector() != null)
            && (getSelectorPanelContainer() != null))
            {
            final Iterator<ObservatoryInstrumentInterface> iterInstruments;

            iterInstruments = getInstrumentsOnSelector().iterator();

            while (iterInstruments.hasNext())
                {
                final ObservatoryInstrumentInterface instrument;

                instrument = iterInstruments.next();
                // Leave the Instrument running while we go away...
                getSelectorPanelContainer().remove(instrument.getSelectorPanel());
                }

            // Remove any leftovers...
            getSelectorPanelContainer().removeAll();
            }

        super.stopUI();
        }


    /***********************************************************************************************
     * Dispose of all components of this UIComponent.
     */

    public void disposeUI()
        {
        if (getInstrumentsOnSelector() != null)
            {
            final Iterator<ObservatoryInstrumentInterface> iterInstruments;

            iterInstruments = getInstrumentsOnSelector().iterator();

            while (iterInstruments.hasNext())
                {
                final ObservatoryInstrumentInterface instrument;

                instrument = iterInstruments.next();
                instrument.stop();
                getSelectorPanelContainer().remove(instrument.getSelectorPanel());
                }

            // Remove any leftovers from the RackCabinet...
            removeAll();
            getSelectorPanelContainer().removeAll();

            // There are now no instruments at all...
            setSelectedInstrument(null);
            }

        super.disposeUI();
        }


    /***********************************************************************************************
     * Add the Instruments to the Selector.
     *
     * @param instruments
     */

    private void addInstrumentsToSelector(final java.util.List<Instrument> instruments)
        {
        final Iterator<Instrument> iterInstruments;

        iterInstruments = instruments.iterator();

        while (iterInstruments.hasNext())
            {
            final Instrument instrument;

            instrument = iterInstruments.next();

            //LOGGER.logTimedEvent("Please wait while Observatory Instruments are identified...");

            // The Instrument may be NULL if no SortIndex was used for that entry
            if (instrument != null)
                {
                addInstrumentToSelector(instrument);
                }
            }
        }


    /***********************************************************************************************
     * Add the specified Instrument to the Selector.
     *
     * @param instrumentxml
     */

    private void addInstrumentToSelector(final Instrument instrumentxml)
        {
        final ObservatoryInstrumentInterface instrument;

        if (getInstantiatedInstrumentsTable().containsKey(instrumentxml.getIdentifier()))
            {
            // We already have an instance, keyed by Identifier
            instrument = getInstantiatedInstrumentsTable().get(instrumentxml.getIdentifier());
            addInstrumentToSelector(instrument);
            }
        else
            {
            // Try to instantiate an ObservatoryInstrument from the classname in the Xml
            instrument = ObservatoryInstrumentHelper.instantiateInstrument(instrumentxml,
                                                                           getHostAtom(),
                                                                           getHostUI(),
                                                                           getResourceKey());
            // If successful, add it to the RackCabinet and the List of instantiated Instruments
            if ((instrument != null)
                && (!getInstantiatedInstrumentsTable().containsKey(instrumentxml.getIdentifier())))
                {
                getInstantiatedInstrumentsTable().put(instrumentxml.getIdentifier(),
                                                      instrument);
                addInstrumentToSelector(instrument);
                }
            else
                {
                LOGGER.error("It was not possible to instantiate an Instrument - check the file instrument-XXX.xml");
                }
            }

        ObservatoryUIHelper.identifyClockAndLog(getHostUI(), instrument);
        }


    /***********************************************************************************************
     * Get the Container holding the SelectorPanels.
     *
     * @return Container
     */

    public Container getSelectorPanelContainer()
        {
        return (this.container);
        }


    /**********************************************************************************************/
    /* ObservatoryInstruments                                                                     */
    /***********************************************************************************************
     * Add an ObservatoryInstrument to the List in the InstrumentSelector.
     * This does not render the Instrument, or affect its UI state.
     *
     * @param instrument
     */

    public final void addInstrumentToSelector(final ObservatoryInstrumentInterface instrument)
        {
        if ((getInstrumentsOnSelector() != null)
            && (instrument != null)
            && (!getInstrumentsOnSelector().contains(instrument)))
            {
            getInstrumentsOnSelector().add(instrument);
            }
        }


    /***********************************************************************************************
     * Remove an ObservatoryInstrument from the List in the InstrumentSelector.
     * This does not render the Instrument, or affect its UI state.
     *
     * @param instrument
     */

    public final void removeInstrumentFromSelector(final ObservatoryInstrumentInterface instrument)
        {
        if ((getInstrumentsOnSelector() != null)
            && (instrument != null)
            && (getInstrumentsOnSelector().contains(instrument)))
            {
            getInstrumentsOnSelector().remove(instrument);
            }
        }


    /***********************************************************************************************
     * Get the List of ObservatoryInstruments currently on this InstrumentSelector.
     *
     * @return Vector<ObservatoryInstrumentInterface>
     */

    public Vector<ObservatoryInstrumentInterface> getInstrumentsOnSelector()
        {
        return (this.vecInstrumentsOnSelector);
        }


    /***********************************************************************************************
     * Get the currently selected ObservatoryInstrument.
     *
     * @return ObservatoryInstrumentInterface
     */

    public ObservatoryInstrumentInterface getSelectedInstrument()
        {
        return (this.selectedInstrument);
        }


    /***********************************************************************************************
     * Set the selected ObservatoryInstrument.
     *
     * @param instrument
     */

    public void setSelectedInstrument(final ObservatoryInstrumentInterface instrument)
        {
        this.selectedInstrument = instrument;
        }


    /**********************************************************************************************/
    /* Injections                                                                                 */
    /***********************************************************************************************
     * Get the host UI.
     *
     * @return ObservatoryUIInterface
     */

    private ObservatoryUIInterface getHostUI()
        {
        return (this.hostUI);
        }


    /***********************************************************************************************
     * Get the Group Definition for this InstrumentSelector.
     *
     * @return Definition
     */

    private Definition getGroupDefinition()
        {
        return (this.definitionGroup);
        }


    /***********************************************************************************************
     * Get the List of Instruments for this InstrumentSelector.
     *
     * @return java.util.List<Instrument>
     */

    private java.util.List<Instrument> getInstrumentsInGroup()
        {
        return (this.listInstrumentsInGroup);
        }


    /***********************************************************************************************
     * Get the table of ObservatoryInstruments currently instantiated.
     *
     * @return Hashtable<String, ObservatoryInstrumentInterface>
     */

    private Hashtable<String, ObservatoryInstrumentInterface> getInstantiatedInstrumentsTable()
        {
        return (this.hashtableInstantiatedInstruments);
        }


    /***********************************************************************************************
     * Get the host AtomPlugin.
     *
     * @return AtomPlugin
     */

    private AtomPlugin getHostAtom()
        {
        return (this.hostAtom);
        }


    /***********************************************************************************************
     * Get the ResourceKey.
     *
     * @return String
     */

    private String getResourceKey()
        {
        return (this.strResourceKey);
        }
    }
