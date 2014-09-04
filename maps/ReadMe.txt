To create a new map for use within the RegionalMap tabs in Starbase,
follow one of these procedures:

----------------------------------------------------------------------------------------------------
IF you have an existing image to use for the RegionalMaps:

Place the file in dist/maps in the Starbase distribution area.

Use a **plain** text editor (e.g. Wordpad, but not a word processor) to edit the file:
dist/imports/frameworks.xml to record the filename and location represented by the new map.

Edit this XML entry to suit the name of your new map image:

        <MapFilename>europe.png</MapFilename>

Edit these XML entries to suit the new map,
remembering that Starbase uses the astronomical sign convention.
You must use the exact format shown, with the correct number of digits.

        <!-- BEWARE! Longitude is POSITIVE to the WEST -->
        <!-- BEWARE! Latitude is POSITIVE to the NORTH -->

        <MapTopLeftLongitude>+010:37:29</MapTopLeftLongitude>
        <MapTopLeftLatitude>+65:00:00</MapTopLeftLatitude>

        <MapBottomRightLongitude>-025:00:00</MapBottomRightLongitude>
        <MapBottomRightLatitude>+34:30:00</MapBottomRightLatitude>

----------------------------------------------------------------------------------------------------

ELSE

Generate a map image with a **linear** mapping of latitude and logitude to position.
This is necessary in order to simplify the reading of the cursor position as the mouse moves round the map.

For example, a suitable Coastline Extractor is at: http://rimmer.ngdc.noaa.gov

Either use the Java applet to select the desired map area, or enter the coordinates manually.
Remember that WEST is NEGATIVE for the Coastline Extractor.

Generate a map dataset, using the following settings:

 * NO COMPRESSION
 * MAPGEN FORMAT
 * GMT PLOT
 * WORLD DATA BANK II

Examine the preview image, and make sure it is the correct area.

Download the map data file, it will be called something like 1234.dat

Rename the 1234.data file to coastline.dat,
and place it in dist/maps in the Starbase distribution area.

Run Starbase Observatory, and start any instrument which uses a RegionalMap, e.g. the VLF Receiver.

Display the RegionalMap tab.

Click the globe icon to "make a new map from maps/coastline.data"

You will find a new file called coastline.tsv in dist/maps,
this is the downloaded data, checked and reformatted.
Make a note of the map latitude and longitude coordinates in the file header.

The exisiting map image will have been backed up in a timestamped file,
and a new image created in its place.
You may now edit this image to suit your taste,
e.g. fill in the land areas, or change the colour of the sea!
Note that not all editors will handle PNG files correctly.

Use a **plain** text editor (e.g. Wordpad, but not a word processor) to edit the file:
dist/imports/frameworks.xml to record the filename and location represented by the new map.

Edit this XML entry to suit the name of your new map image:

        <MapFilename>europe.png</MapFilename>

Edit these XML entries to suit the values read from coastline.tsv,
remembering that Starbase uses the astronomical sign convention,
which is different from the Coastline Extractor.
You must use the exact format shown, with the correct number of digits.

        <!-- BEWARE! Longitude is POSITIVE to the WEST -->
        <!-- BEWARE! Latitude is POSITIVE to the NORTH -->

        <MapTopLeftLongitude>+010:37:29</MapTopLeftLongitude>
        <MapTopLeftLatitude>+65:00:00</MapTopLeftLatitude>

        <MapBottomRightLongitude>-025:00:00</MapBottomRightLongitude>
        <MapBottomRightLatitude>+34:30:00</MapBottomRightLatitude>

----------------------------------------------------------------------------------------------------

Restart Starbase or log out and back in again,
and your map should now be available on the RegionalMap tabs!

REMEMBER to save your new image outside Starbase as well,
in case a new distribution overwrites it.

You may delete the downloaded coastline.dat and the generated coastline.tsv files.

Any problems, please contact starbase@ukraa.com

Laurence Newell
2009-10-10

