MATRIX CODE SIMULATOR APPLET VERSION 0.5 - 6/9/2003
WRITTEN BY SCOTT P. SMITH - http://www.scott-smith.com


LICENSE

Permission to use, copy, modify, distribute, and sell this software and its
documentation for any purpose is hereby granted without fee, provided that
the all original copyright notices appear in all distributions in user readable
form and that this permission notice appear in supporting documentation.
No representations are made about the suitability of this
software for any purpose.  It is provided "as is" without express or
implied warranty.

I would appretiate (but don't require) a notice like the following on any
web site that uses this applet:

"Matrix code panelInterface applet written by Scott P. Smith - http://www.scott-smith.com"


BUILDING THE JAR FILE

Install Ant (http://ant.apache.org/) then enter the command 'ant' from the top
level directory (the directory where build.xml is located).


REVISION HISTORY:

0.5     Remove packages so that all classes are in the default package.
        This was done in hopes of making the applet work with some
        browser environments that don't seem to support packages.
        Replaced use of Math.random() with java.util.Random.

0.1     First release 1/30/2003


TODO:

* The ability to choose the font size and specify other configuration information
via an applet parameter would be good. Right now, everything is hard-coded.

* It would be nice to allow text messages to float in front of or behind the code.

* Adding the ability to display animated gifs with transparent background over/under
the code would be cool too.

None of these mods are very hard, I just don't want to do them right now.


CONTRIBUTIONS:

If you improve this code, let me know and I will incorporate your changes.
You can get my email address from my web site: http://www.scott-smith.com.


CREDITS:

Copyright (c) 2003 Scott P. Smith (http://www.scott-smith.com)


I started with the DukeAnim.java example at
http://java.sun.com/products/java-media/2D/samples/suite/index.html, then
used techniques described by Matthew Reeder
(http://students.cs.byu.edu/~kawigi/animation/) to achive low CPU
overhead animation.

I used IrfanView (http://www.irfanview.com) to resize the font bitmap and
convert it to a png file.

I used NetBeans (http://www.netbeans.org) to edit all files.

I downloaded the kmatrix screensaver source code (and font bitmap)
from http://www.geocities.com/dmalykhanov/kmatrix which is maintained
by Dmitry DELTA Malykhanov.

The only thing I took from the kmatrix screensaver was the font.  If someone
would like to built me a replacement font, I'll use it so I can remove the
following copyrights that I put here for completeness, even though I only
used the fonts:


KMATRIX SCREENSAVER CREDITS:

lines   -  Copyright (c) 1997 Dirk Staneker
  blob    -  Copyright (c) 1997 Tiaan Wessels <tiaan@netsys.co.za>
  science -  Copyright (c) 1998 Rene Beutler

Ported from xlockmore:     (but blame me if they don't work)
  rock    -  Copyright (c) 1992 by Jamie Zawinski
  flame   -  Copyright (c) 1991 by Patrick J. Naughton.
  pyro    -  Copyright (c) 1991 by Patrick J. Naughton.
  laser   -  Copyright (c) 1995 Pascal Pensa <pensa@aurora.unice.fr>

Ported from xlockmore by Emanuel Pirker <epirker@edu.uni-klu.ac.at>:
  bat     -  Copyright (c) 1988 by Sun Microsystems
  forest  -  Copyright (c) 1995 Pascal Pensa <pensa@aurora.unice.fr>
  hop     -  Copyright (c) 1991 by Patrick J. Naughton.
  lissie  -  Copyright (c) Alexander Jolk <ub9x@rz.uni-karlsruhe.de>
  slip    -  Copyright (c) 1992 by Scott Draves (spot@cs.cmu.edu)
  swarm   -  Copyright (c) 1991 by Patrick J. Naughton
  morph3d -  Copyright (c) 1997 by Marcelo F. Vianna

Ported from xscreensaver by Tom Vijlbrief <tom.vijlbrief@knoware.nl>
  attraction - Copyright (c) 1997 by Jamie Zawinski <jwz@jwz.org>
  slidescreen - Copyright (c) 1997 by Jamie Zawinski <jwz@jwz.org>

  The files xs_* are also from xscreensaver.


xlock Copyright Notice:

 Permission to use, copy, modify, distribute, and sell this software and its
 documentation for any purpose is hereby granted without fee, provided that
 the above copyright notice appear in all copies and that both that
 copyright notice and this permission notice appear in supporting
 documentation.  No representations are made about the suitability of this
 software for any purpose.  It is provided "as is" without express or
 implied warranty.

xscreensaver Copyright Notice:

 Permission to use, copy, modify, distribute, and sell this software and its
 documentation for any purpose is hereby granted without fee, provided that
 the above copyright notice appear in all copies and that both that
 copyright notice and this permission notice appear in supporting
 documentation.  No representations are made about the suitability of this
 software for any purpose.  It is provided "as is" without express or
 implied warranty








