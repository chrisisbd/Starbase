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

package org.lmn.fc.frameworks.starbase.plugins.observatory.ui.instruments.common;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.lmn.fc.common.constants.FrameworkConstants;
import org.lmn.fc.common.constants.FrameworkMetadata;
import org.lmn.fc.common.constants.FrameworkRegex;
import org.lmn.fc.common.constants.FrameworkStrings;
import org.lmn.fc.common.utilities.time.TimeSystem;
import org.lmn.fc.frameworks.starbase.plugins.observatory.MetadataDictionary;
import org.lmn.fc.model.datatypes.DataTypeDictionary;
import org.lmn.fc.model.xmlbeans.metadata.Metadata;
import org.lmn.fc.model.xmlbeans.metadata.SchemaUnits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/***********************************************************************************************
 * A test Hydrogen spectrum from John McKay.
 */

public final class HydrogenSpectrumHelper implements FrameworkConstants,
                                                     FrameworkStrings,
                                                     FrameworkMetadata,
                                                     FrameworkRegex
    {
    private static final int FREQUENCY_OFFSET_MIN = -1000;
    private static final int FREQUENCY_OFFSET_MAX = 1000;
    private static final int FREQUENCY_OFFSET_STEP = 5;

    private static final int CHANNEL_MIN = 0;
    private static final int CHANNEL_MAX = 1;

    private static final int INDEX_FREQUENCY_OFFSET = 0;
    private static final int INDEX_FREQUENCY = 1;
    private static final int INDEX_CHANNEL = 2;


    /***********************************************************************************************
     * The Hydrogen spectrum.
     * Offset in KHz from Hygrogen rest frequency -/+ 1000kHz
     * The Rx frequency 1419.504751 to 1421.405751 MHz
     * Rx output in Volts (two channels).
     */

    private static final double[][] dblSpectrum =
        {
        //   Offset Frequency     Channel 0  Channel 1
            {FREQUENCY_OFFSET_MIN, 1419.405751,  3.061516,  3.041984},
            {-995,  1419.410751,  3.059074,  3.061516},
            {-990,  1419.415751,  3.547354,  3.142082},
            {-985,  1419.420751,  8.146952,  3.430167},
            {-980,  1419.425751,  9.040504,  3.45214},
            {-975,  1419.430751,  5.3369,    3.186027},
            {-970,  1419.435751,  3.840322,  3.098137},
            {-965,  1419.440751,  3.361808,  3.098137},
            {-960,  1419.445751,  3.186027,  3.054191},
            {-955,  1419.450751,  3.132316,  3.049309},
            {-950,  1419.455751,  3.112785,  3.081047},
            {-945,  1419.460751,  3.110343,  3.071281},
            {-940,  1419.465751,  3.129875,  3.061516},
            {-935,  1419.470751,  3.12255,   3.066398},
            {-930,  1419.475751,  3.120109,  3.06884},
            {-925,  1419.480751,  3.066398,  3.090812},
            {-920,  1419.485751,  3.081047,  3.083488},
            {-915,  1419.490751,  3.117668,  3.093254},
            {-910,  1419.495751,  3.115226,  3.090812},
            {-905,  1419.500751,  3.107902,  3.112785},
            {-900,  1419.505751,  3.105461,  3.100578},
            {-895,  1419.510751,  3.098137,  3.08593},
            {-890,  1419.515751,  3.107902,  3.098137},
            {-885,  1419.520751,  3.127433,  3.06884},
            {-880,  1419.525751,  3.115226,  3.076164},
            {-875,  1419.530751,  3.088371,  3.071281},
            {-870,  1419.535751,  3.124992,  3.093254},
            {-865,  1419.540751,  3.142082,  3.107902},
            {-860,  1419.545751,  3.142082,  3.100578},
            {-855,  1419.550751,  3.137199,  3.073723},
            {-850,  1419.555751,  3.095695,  3.071281},
            {-845,  1419.560751,  3.134758,  3.078605},
            {-840,  1419.565751,  3.127433,  3.071281},
            {-835,  1419.570751,  3.120109,  3.083488},
            {-830,  1419.575751,  3.098137,  3.06884},
            {-825,  1419.580751,  3.107902,  3.100578},
            {-820,  1419.585751,  3.110343,  3.149406},
            {-815,  1419.590751,  3.132316,  3.120109},
            {-810,  1419.595751,  3.107902,  3.13964},
            {-805,  1419.600751,  3.112785,  3.129875},
            {-800,  1419.605751,  3.112785,  3.098137},
            {-795,  1419.610751,  3.129875,  3.083488},
            {-790,  1419.615751,  3.129875,  3.105461},
            {-785,  1419.620751,  3.129875,  3.063957},
            {-780,  1419.625751,  3.112785,  3.098137},
            {-775,  1419.630751,  3.115226,  3.112785},
            {-770,  1419.635751,  3.144523,  3.120109},
            {-765,  1419.640751,  3.110343,  3.093254},
            {-760,  1419.645751,  3.129875,  3.073723},
            {-755,  1419.650751,  3.120109,  3.103019},
            {-750,  1419.655751,  3.115226,  3.117668},
            {-745,  1419.660751,  3.120109,  3.098137},
            {-740,  1419.665751,  3.146965,  3.098137},
            {-735,  1419.670751,  3.15673,  3.120109},
            {-730,  1419.675751,  3.134758,  3.115226},
            {-725,  1419.680751,  3.129875,  3.110343},
            {-720,  1419.685751,  3.107902,  3.124992},
            {-715,  1419.690751,  3.142082,  3.146965},
            {-710,  1419.695751,  3.178703,  3.144523},
            {-705,  1419.700751,  3.166496,  3.146965},
            {-700,  1419.705751,  3.137199,  3.149406},
            {-695,  1419.710751,  3.112785,  3.110343},
            {-690,  1419.715751,  3.154289,  3.129875},
            {-685,  1419.720751,  3.129875,  3.154289},
            {-680,  1419.725751,  3.151847,  3.127433},
            {-675,  1419.730751,  3.137199,  3.117668},
            {-670,  1419.735751,  3.134758,  3.115226},
            {-665,  1419.740751,  3.168937,  3.132316},
            {-660,  1419.745751,  3.134758,  3.12255},
            {-655,  1419.750751,  3.144523,  3.13964},
            {-650,  1419.755751,  3.115226,  3.107902},
            {-645,  1419.760751,  3.129875,  3.146965},
            {-640,  1419.765751,  3.159172,  3.144523},
            {-635,  1419.770751,  3.149406,  3.103019},
            {-630,  1419.775751,  3.151847,  3.117668},
            {-625,  1419.780751,  3.186027,  3.13964},
            {-620,  1419.785751,  3.159172,  3.137199},
            {-615,  1419.790751,  3.188468,  3.132316},
            {-610,  1419.795751,  3.159172,  3.137199},
            {-605,  1419.800751,  3.144523,  3.146965},
            {-600,  1419.805751,  3.142082,  3.132316},
            {-595,  1419.810751,  3.195792,  3.107902},
            {-590,  1419.815751,  3.178703,  3.12255},
            {-585,  1419.820751,  3.13964,   3.110343},
            {-580,  1419.825751,  3.12255,   3.137199},
            {-575,  1419.830751,  3.124992,  3.161613},
            {-570,  1419.835751,  3.161613,  3.168937},
            {-565,  1419.840751,  3.154289,  3.166496},
            {-560,  1419.845751,  3.17382,   3.146965},
            {-555,  1419.850751,  3.188468,  3.13964},
            {-550,  1419.855751,  3.161613,  3.146965},
            {-545,  1419.860751,  3.142082,  3.13964},
            {-540,  1419.865751,  3.178703,  3.166496},
            {-535,  1419.870751,  3.164054,  3.146965},
            {-530,  1419.875751,  3.188468,  3.13964},
            {-525,  1419.880751,  3.178703,  3.159172},
            {-520,  1419.885751,  3.207999,  3.13964},
            {-515,  1419.890751,  3.183586,  3.154289},
            {-510,  1419.895751,  3.178703,  3.159172},
            {-505,  1419.900751,  3.142082,  3.178703},
            {-500,  1419.905751,  3.132316,  3.168937},
            {-495,  1419.910751,  3.166496,  3.159172},
            {-490,  1419.915751,  3.178703,  3.168937},
            {-485,  1419.920751,  3.161613,  3.19091},
            {-480,  1419.925751,  3.188468,  3.13964},
            {-475,  1419.930751,  3.183586,  3.161613},
            {-470,  1419.935751,  3.13964,   3.17382},
            {-465,  1419.940751,  3.161613,  3.168937},
            {-460,  1419.945751,  3.178703,  3.178703},
            {-455,  1419.950751,  3.166496,  3.166496},
            {-450,  1419.955751,  3.176261,  3.151847},
            {-445,  1419.960751,  3.166496,  3.178703},
            {-440,  1419.965751,  3.161613,  3.176261},
            {-435,  1419.970751,  3.181144,  3.171379},
            {-430,  1419.975751,  3.168937,  3.166496},
            {-425,  1419.980751,  3.178703,  3.15673},
            {-420,  1419.985751,  3.168937,  3.161613},
            {-415,  1419.990751,  3.181144,  3.164054},
            {-410,  1419.995751,  3.188468,  3.186027},
            {-405,  1420.000751,  3.149406,  3.15673},
            {-400,  1420.005751,  3.176261,  3.161613},
            {-395,  1420.010751,  3.15673,   3.151847},
            {-390,  1420.015751,  3.176261,  3.166496},
            {-385,  1420.020751,  3.200675,  3.178703},
            {-380,  1420.025751,  3.181144,  3.188468},
            {-375,  1420.030751,  3.178703,  3.159172},
            {-370,  1420.035751,  3.207999,  3.181144},
            {-365,  1420.040751,  3.19091,   3.186027},
            {-360,  1420.045751,  3.178703,  3.168937},
            {-355,  1420.050751,  3.168937,  3.166496},
            {-350,  1420.055751,  3.186027,  3.186027},
            {-345,  1420.060751,  3.198234,  3.195792},
            {-340,  1420.065751,  3.193351,  3.195792},
            {-335,  1420.070751,  3.176261,  3.181144},
            {-330,  1420.075751,  3.171379,  3.154289},
            {-325,  1420.080751,  3.171379,  3.176261},
            {-320,  1420.085751,  3.183586,  3.203117},
            {-315,  1420.090751,  3.183586,  3.205558},
            {-310,  1420.095751,  3.166496,  3.168937},
            {-305,  1420.100751,  3.176261,  3.186027},
            {-300,  1420.105751,  3.188468,  3.188468},
            {-295,  1420.110751,  3.225089,  3.205558},
            {-290,  1420.115751,  3.205558,  3.207999},
            {-285,  1420.120751,  3.207999,  3.176261},
            {-280,  1420.125751,  3.193351,  3.195792},
            {-275,  1420.130751,  3.198234,  3.181144},
            {-270,  1420.135751,  3.193351,  3.159172},
            {-265,  1420.140751,  3.19091,   3.166496},
            {-260,  1420.145751,  3.15673,   3.149406},
            {-255,  1420.150751,  3.176261,  3.188468},
            {-250,  1420.155751,  3.205558,  3.176261},
            {-245,  1420.160751,  3.188468,  3.186027},
            {-240,  1420.165751,  3.171379,  3.193351},
            {-235,  1420.170751,  3.171379,  3.227531},
            {-230,  1420.175751,  3.188468,  3.198234},
            {-225,  1420.180751,  3.186027,  3.178703},
            {-220,  1420.185751,  3.195792,  3.176261},
            {-215,  1420.190751,  3.215324,  3.217765},
            {-210,  1420.195751,  3.186027,  3.200675},
            {-205,  1420.200751,  3.207999,  3.195792},
            {-200,  1420.205751,  3.195792,  3.195792},
            {-195,  1420.210751,  3.227531,  3.168937},
            {-190,  1420.215751,  3.171379,  3.178703},
            {-185,  1420.220751,  3.222648,  3.207999},
            {-180,  1420.225751,  3.212882,  3.217765},
            {-175,  1420.230751,  3.154289,  3.178703},
            {-170,  1420.235751,  3.188468,  3.200675},
            {-165,  1420.240751,  3.198234,  3.159172},
            {-160,  1420.245751,  3.178703,  3.176261},
            {-155,  1420.250751,  3.200675,  3.166496},
            {-150,  1420.255751,  3.186027,  3.178703},
            {-145,  1420.260751,  3.168937,  3.181144},
            {-140,  1420.265751,  3.198234,  3.151847},
            {-135,  1420.270751,  3.188468,  3.178703},
            {-130,  1420.275751,  3.17382,   3.222648},
            {-125,  1420.280751,  3.171379,  3.207999},
            {-120,  1420.285751,  3.188468,  3.215324},
            {-115,  1420.290751,  3.207999,  3.234855},
            {-110,  1420.295751,  3.168937,  3.217765},
            {-105,  1420.300751,  3.217765,  3.217765},
            {-100,  1420.305751,  3.207999,  3.210441},
            {-95,   1420.310751,  3.232414,  3.183586},
            {-90,   1420.315751,  3.198234,  3.195792},
            {-85,   1420.320751,  3.207999,  3.215324},
            {-80,   1420.325751,  3.217765,  3.232414},
            {-75,   1420.330751,  3.210441,  3.215324},
            {-70,   1420.335751,  3.203117,  3.176261},
            {-65,   1420.340751,  3.227531,  3.186027},
            {-60,   1420.345751,  3.193351,  3.207999},
            {-55,   1420.350751,  3.212882,  3.207999},
            {-50,   1420.355751,  3.256828,  3.227531},
            {-45,   1420.360751,  3.229972,  3.217765},
            {-40,   1420.365751,  3.251945,  3.198234},
            {-35,   1420.370751,  3.256828,  3.215324},
            {-30,   1420.375751,  3.247062,  3.220206},
            {-25,   1420.380751,  3.256828,  3.188468},
            {-20,   1420.385751,  3.259269,  3.239738},
            {-15,   1420.390751,  3.293448,  3.227531},
            {-10,   1420.395751,  3.310538,  3.237296},
            {-5,    1420.400751,  3.361808,  3.232414},
            {0,     1420.405751,  3.413077,  3.227531},
            {5,     1420.410751,  3.459464,  3.251945},
            {10,    1420.415751,  3.498526,  3.283683},
            {15,    1420.420751,  3.52294,   3.29589},
            {20,    1420.425751,  3.598624,  3.244621},
            {25,    1420.430751,  3.715811,  3.271476},
            {30,    1420.435751,  3.891592,  3.293448},
            {35,    1420.440751,  4.089345,  3.334952},
            {40,    1420.445751,  4.201649,  3.334952},
            {45,    1420.450751,  4.379871,  3.408194},
            {50,    1420.455751,  4.633777,  3.45214},
            {55,    1420.460751,  4.782702,  3.500968},
            {60,    1420.465751,  4.9536,    3.605948},
            {65,    1420.470751,  5.124498,  3.701162},
            {70,    1420.475751,  5.185534,  3.874502},
            {75,    1420.480751,  5.224596,  4.047841},
            {80,    1420.485751,  5.290514,  4.179677},
            {85,    1420.490751,  5.280748,  4.353016},
            {90,    1420.495751,  5.288072,  4.436024},
            {95,    1420.500751,  5.2661,    4.592273},
            {100,  1420.505751,  5.207506,  4.658191},
            {105,  1420.510751,  5.222155,  4.63866},
            {110,  1420.515751,  5.258776,  4.628894},
            {115,  1420.520751,  5.288072,  4.660633},
            {120,  1420.525751,  5.253893,  4.58739},
            {125,  1420.530751,  5.263659,  4.548328},
            {130,  1420.535751,  5.251451,  4.467762},
            {135,  1420.540751,  5.219713,  4.448231},
            {140,  1420.545751,  5.234362,  4.399403},
            {145,  1420.550751,  5.209948,  4.357899},
            {150,  1420.555751,  5.163561,  4.321278},
            {155,  1420.560751,  5.031725,  4.32372},
            {160,  1420.565751,  4.960925,  4.252919},
            {165,  1420.570751,  4.799792,  4.216298},
            {170,  1420.575751,  4.660633,  4.213856},
            {175,  1420.580751,  4.553211,  4.22118},
            {180,  1420.585751,  4.362782,  4.174794},
            {185,  1420.590751,  4.23827,  4.160146},
            {190,  1420.595751,  4.118642,  4.16747},
            {195,  1420.600751,  4.013661,  4.165029},
            {200,  1420.605751,  3.986806,  4.152822},
            {205,  1420.610751,  3.947744,  4.169911},
            {210,  1420.615751,  3.913564,  4.184559},
            {215,  1420.620751,  3.867177,  4.145497},
            {220,  1420.625751,  3.842763,  4.145497},
            {225,  1420.630751,  3.815908,  4.103993},
            {230,  1420.635751,  3.825674,  4.118642},
            {235,  1420.640751,  3.781729,  4.121083},
            {240,  1420.645751,  3.789053,  4.050282},
            {245,  1420.650751,  3.811025,  3.986806},
            {250,  1420.655751,  3.835439,  3.977041},
            {255,  1420.660751,  3.796377,  3.908681},
            {260,  1420.665751,  3.886709,  3.881826},
            {265,  1420.670751,  3.894033,  3.867177},
            {270,  1420.675751,  3.913564,  3.823232},
            {275,  1420.680751,  3.935537,  3.859853},
            {280,  1420.685751,  3.950185,  3.825674},
            {285,  1420.690751,  3.969716,  3.847646},
            {290,  1420.695751,  3.952626,  3.925771},
            {295,  1420.700751,  3.90624,   3.950185},
            {300,  1420.705751,  3.911123,  3.969716},
            {305,  1420.710751,  3.911123,  3.989248},
            {310,  1420.715751,  3.945302,  4.077138},
            {315,  1420.720751,  3.940419,  4.096669},
            {320,  1420.725751,  3.955068,  4.1162},
            {325,  1420.730751,  3.950185,  4.155263},
            {330,  1420.735751,  3.940419,  4.169911},
            {335,  1420.740751,  3.913564,  4.199208},
            {340,  1420.745751,  3.842763,  4.145497},
            {345,  1420.750751,  3.811025,  4.194325},
            {350,  1420.755751,  3.78417,   4.152822},
            {355,  1420.760751,  3.754873,  4.106435},
            {360,  1420.765751,  3.725576,  4.106435},
            {365,  1420.770751,  3.669424,  4.099111},
            {370,  1420.775751,  3.647452,  4.042958},
            {375,  1420.780751,  3.630362,  4.050282},
            {380,  1420.785751,  3.620596,  4.020986},
            {385,  1420.790751,  3.598624,  3.981923},
            {390,  1420.795751,  3.618155,  3.969716},
            {395,  1420.800751,  3.64501,   3.930654},
            {400,  1420.805751,  3.69628,   3.911123},
            {405,  1420.810751,  3.710928,  3.903799},
            {410,  1420.815751,  3.769521,  3.918447},
            {415,  1420.820751,  3.815908,  3.852529},
            {420,  1420.825751,  3.859853,  3.850088},
            {425,  1420.830751,  3.950185,  3.811025},
            {430,  1420.835751,  3.979482,  3.867177},
            {435,  1420.840751,  4.018544,  3.881826},
            {440,  1420.845751,  4.016103,  3.886709},
            {445,  1420.850751,  4.02831,   3.920888},
            {450,  1420.855751,  4.038075,  3.959951},
            {455,  1420.860751,  4.006337,  4.016103},
            {460,  1420.865751,  4.018544,  4.018544},
            {465,  1420.870751,  4.01122,   4.072255},
            {470,  1420.875751,  3.908681,  4.072255},
            {475,  1420.880751,  3.90624,   4.082021},
            {480,  1420.885751,  3.920888,  4.099111},
            {485,  1420.890751,  3.823232,  4.074697},
            {490,  1420.895751,  3.825674,  4.040517},
            {495,  1420.900751,  3.771963,  4.006337},
            {500,  1420.905751,  3.786611,  3.969716},
            {505,  1420.910751,  3.779287,  3.908681},
            {510,  1420.915751,  3.752432,  3.913564},
            {515,  1420.920751,  3.725576,  3.85497},
            {520,  1420.925751,  3.676748,  3.850088},
            {525,  1420.930751,  3.608389,  3.81835},
            {530,  1420.935751,  3.579092,  3.730459},
            {535,  1420.940751,  3.532706,  3.67919},
            {540,  1420.945751,  3.483878,  3.598624},
            {545,  1420.950751,  3.464347,  3.601065},
            {550,  1420.955751,  3.461905,  3.62792},
            {555,  1420.960751,  3.425284,  3.62792},
            {560,  1420.965751,  3.381339,  3.552237},
            {565,  1420.970751,  3.369132,  3.566885},
            {570,  1420.975751,  3.361808,  3.559561},
            {575,  1420.980751,  3.305655,  3.530264},
            {580,  1420.985751,  3.303214,  3.518057},
            {585,  1420.990751,  3.276359,  3.50585},
            {590,  1420.995751,  3.237296,  3.420401},
            {595,  1421.000751,  3.212882,  3.420401},
            {600,  1421.005751,  3.188468,  3.420401},
            {605,  1421.010751,  3.186027,  3.352042},
            {610,  1421.015751,  3.200675,  3.315421},
            {615,  1421.020751,  3.198234,  3.344718},
            {620,  1421.025751,  3.207999,  3.315421},
            {625,  1421.030751,  3.183586,  3.315421},
            {630,  1421.035751,  3.188468,  3.29589},
            {635,  1421.040751,  3.19091,   3.256828},
            {640,  1421.045751,  3.146965,  3.254386},
            {645,  1421.050751,  3.137199,  3.256828},
            {650,  1421.055751,  3.117668,  3.225089},
            {655,  1421.060751,  3.103019,  3.207999},
            {660,  1421.065751,  3.13964,   3.217765},
            {665,  1421.070751,  3.12255,   3.198234},
            {670,  1421.075751,  3.164054,  3.19091},
            {675,  1421.080751,  3.127433,  3.159172},
            {680,  1421.085751,  3.13964,   3.149406},
            {685,  1421.090751,  3.117668,  3.137199},
            {690,  1421.095751,  3.12255,   3.107902},
            {695,  1421.100751,  3.098137,  3.110343},
            {700,  1421.105751,  3.081047,  3.127433},
            {705,  1421.110751,  3.090812,  3.124992},
            {710,  1421.115751,  3.090812,  3.12255},
            {715,  1421.120751,  3.100578,  3.115226},
            {720,  1421.125751,  3.103019,  3.12255},
            {725,  1421.130751,  3.095695,  3.088371},
            {730,  1421.135751,  3.105461,  3.098137},
            {735,  1421.140751,  3.103019,  3.093254},
            {740,  1421.145751,  3.078605,  3.090812},
            {745,  1421.150751,  3.103019,  3.071281},
            {750,  1421.155751,  3.088371,  3.100578},
            {755,  1421.160751,  3.090812,  3.088371},
            {760,  1421.165751,  3.06884,   3.06884},
            {765,  1421.170751,  3.095695,  3.039543},
            {770,  1421.175751,  3.06884,   3.061516},
            {775,  1421.180751,  3.041984,  3.03466},
            {780,  1421.185751,  3.066398,  3.071281},
            {785,  1421.190751,  3.039543,  3.078605},
            {790,  1421.195751,  3.015129,  3.090812},
            {795,  1421.200751,  3.063957,  3.061516},
            {800,  1421.205751,  3.05175,   3.041984},
            {805,  1421.210751,  3.063957,  3.05175},
            {810,  1421.215751,  3.054191,  3.01757},
            {815,  1421.220751,  3.076164,  3.046867},
            {820,  1421.225751,  3.054191,  3.022453},
            {825,  1421.230751,  3.049309,  3.03466},
            {830,  1421.235751,  3.044426,  3.05175},
            {835,  1421.240751,  3.066398,  3.059074},
            {840,  1421.245751,  3.049309,  3.024894},
            {845,  1421.250751,  2.995598,  3.046867},
            {850,  1421.255751,  3.022453,  3.029777},
            {855,  1421.260751,  3.029777,  3.01757},
            {860,  1421.265751,  3.041984,  3.015129},
            {865,  1421.270751,  3.029777,  3.039543},
            {870,  1421.275751,  2.993156,  3.041984},
            {875,  1421.280751,  3.039543,  3.022453},
            {880,  1421.285751,  3.020012,  3.03466},
            {885,  1421.290751,  3.005363,  3.022453},
            {890,  1421.295751,  3.029777,  3.022453},
            {895,  1421.300751,  3.012687,  3.010246},
            {900,  1421.305751,  3.002922,  3.06884},
            {905,  1421.310751,  3.010246,  3.041984},
            {910,  1421.315751,  3.027336,  3.012687},
            {915,  1421.320751,  3.041984,  3.002922},
            {920,  1421.325751,  3.007805,  2.995598},
            {925,  1421.330751,  3.010246,  3.032219},
            {930,  1421.335751,  2.990715,  3.789053},
            {935,  1421.340751,  3.022453,  6.85301},
            {940,  1421.345751,  3.027336,  6.85301},
            {945,  1421.350751,  3.007805,  4.409168},
            {950,  1421.355751,  3.005363,  3.45214},
            {955,  1421.360751,  3.012687,  3.15673},
            {960,  1421.365751,  2.983391,  3.046867},
            {965,  1421.370751,  3.002922,  3.032219},
            {970,  1421.375751,  2.983391,  3.007805},
            {975,  1421.380751,  2.983391,  3.012687},
            {980,  1421.385751,  3.020012,  3.012687},
            {985,  1421.390751,  2.980949,  2.983391},
            {990,  1421.395751,  2.956535,  2.983391},
            {995,  1421.400751,  2.973625,  2.993156},
            {FREQUENCY_OFFSET_MAX, 1421.405751,  2.973625,  2.993156}
        };


    /***********************************************************************************************
     * Get the full Hydrogen spectrum data as an array of doubles.
     *
     * @return double[][]
     */

    public static double[][] HYDROGEN_SPECTRUM()
        {
        return (dblSpectrum);
        }


    /***********************************************************************************************
     * Get the whole spectrum suitable for a Report, for the specified channel.
     * The spectrum is signal vs. frequency offset.
     *
     * @param channel
     *
     * @return List<List>
     */

    public static List<List> getSpectrumAsList(final int channel)
        {
        List<List> listSpectrum;

        listSpectrum = null;

        if ((channel >= CHANNEL_MIN)
            && (channel <= CHANNEL_MAX))
            {
            listSpectrum = new ArrayList<List>(dblSpectrum.length);

            for (int i = 0;
                 i < dblSpectrum.length;
                 i++)
                {
                final List listRow;
                final double[] data;

                listRow = new ArrayList(2);
                data = dblSpectrum[i];

                listRow.add(data[INDEX_FREQUENCY_OFFSET]);
                listRow.add(data[INDEX_CHANNEL + channel]);

                listSpectrum.add(listRow);
                }
            }

        return (listSpectrum);
        }


    /***********************************************************************************************
     * Get the whole spectrum suitable for a Report, for the specified channel.
     * The spectrum is signal vs. frequency offset.
     *
     * @param channel
     *
     * @return Vector<Object>
     */

    public static Vector<Object> getSpectrumAsVector(final int channel)
        {
        Vector<Object> vecSpectrum;

        vecSpectrum = null;

        if ((channel >= CHANNEL_MIN)
            && (channel <= CHANNEL_MAX))
            {
            vecSpectrum = new Vector<Object>(dblSpectrum.length);

            for (int i = 0;
                 i < dblSpectrum.length;
                 i++)
                {
                final Vector<Object> vecRow;
                final double[] data;

                vecRow = new Vector<Object>(2);
                data = dblSpectrum[i];

                vecRow.add(data[INDEX_FREQUENCY_OFFSET]);
                vecRow.add(data[INDEX_CHANNEL + channel]);

                vecSpectrum.add(vecRow);
                }

            }

        return (vecSpectrum);
        }


    /***********************************************************************************************
     * Get the specified part of the spectrum suitable for a Report, for the specified channel.
     * The spectrum is signal vs. frequency offset.
     *
     * @param start
     * @param end
     * @param channel
     *
     * @return Vector<Object>
     */

    public static Vector<Object> getSpectrumByOffsetAsVector(final int start,
                                                             final int end,
                                                             final int channel)
        {
        Vector<Object> vecSpectrum;

        vecSpectrum = null;

        // Ensure that all parameters are sensible...
        if ((start >= FREQUENCY_OFFSET_MIN)
            && (start <= FREQUENCY_OFFSET_MAX)
            && (Math.IEEEremainder(start, FREQUENCY_OFFSET_STEP) == 0)
            && (end >= FREQUENCY_OFFSET_MIN)
            && (end <= FREQUENCY_OFFSET_MAX)
            && (Math.IEEEremainder(end, FREQUENCY_OFFSET_STEP) == 0)
            && (end > start)
            && (channel >= CHANNEL_MIN)
            && (channel <= CHANNEL_MAX))
            {
            vecSpectrum = new Vector<Object>((end - start) / FREQUENCY_OFFSET_STEP);

            // Values retrieved are inclusive
            for (int i = ((start - FREQUENCY_OFFSET_MIN) / FREQUENCY_OFFSET_STEP);
                 i < (((end - FREQUENCY_OFFSET_MIN) / FREQUENCY_OFFSET_STEP) + 1);
                 i++)
                {
                final Vector<Object> vecRow;
                final double[] data;

                vecRow = new Vector<Object>(2);
                data = dblSpectrum[i];

                vecRow.add(data[INDEX_FREQUENCY_OFFSET]);
                vecRow.add(data[INDEX_CHANNEL + channel]);

                vecSpectrum.add(vecRow);
                }
            }

        return (vecSpectrum);
        }


    /***********************************************************************************************
     * Get one Hydrogen spectrum 'bin' by specifying the frequency offset (-1000...+1000)
     * and channel (0...1). The offset must be in units of 5kHz.
     *
     * @param offset
     * @param channel
     *
     * @return double
     */

    public static double getSpectrumBin(final int offset,
                                        final int channel)
        {
        double dblBin;

        dblBin = 0.0;

        if ((offset >= FREQUENCY_OFFSET_MIN)
            && (offset <= FREQUENCY_OFFSET_MAX)
            && (Math.IEEEremainder(offset, FREQUENCY_OFFSET_STEP) == 0)
            && (channel >= CHANNEL_MIN)
            && (channel <= CHANNEL_MAX))
            {
            final int intIndex;

            // Find out which row to retrieve
            intIndex = (offset - FREQUENCY_OFFSET_MIN) / FREQUENCY_OFFSET_STEP;

            dblBin = HYDROGEN_SPECTRUM()[intIndex][channel+ INDEX_CHANNEL];
            }

        return (dblBin);
        }


    /***********************************************************************************************
     * Get some Metadata for testing.
     *
     * @return List<Metadata>
     */

    public static List<Metadata> createHydrogenSpectrumMetadata()
        {
        final List<Metadata> listMetaData;

        listMetaData = new ArrayList<Metadata>(20);

        //------------------------------------------------------------------------------------------
        // Observatory

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATORY_NAME.getKey(),
                                      "John McKay Observatory",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "John McKay Observatory");

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATORY_LONGITUDE.getKey(),
                                      "+001:20:30",
                                      REGEX_LONGITUDE_DMS_SIGNED,
                                      DataTypeDictionary.SIGNED_LONGITUDE,
                                      SchemaUnits.DEG_MIN_SEC,
                                      "The Observatory Longitude");

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATORY_LATITUDE.getKey(),
                                      "+52:01:02",
                                      REGEX_LATITUDE_DMS_SIGNED,
                                      DataTypeDictionary.LATITUDE,
                                      SchemaUnits.DEG_MIN_SEC,
                                      "The Observatory Latitude");

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATORY_LOCATION.getKey(),
                                      "In the North",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Observatory Location");

        //------------------------------------------------------------------------------------------
        // Observer

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVER_NAME.getKey(),
                                      "John McKay",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Observer's Name");

        //------------------------------------------------------------------------------------------
        // Observation

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATION_TITLE.getKey(),
                                      "SpectraCyber Hydrogen Spectrum",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Observation Title");

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATION_TIMEZONE.getKey(),
                                      "GMT+00:00",
                                      REGEX_TIMEZONE,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Observation TimeZone");

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATION_TIMESYSTEM.getKey(),
                                      TimeSystem.UT.getMnemonic(),
                                      REGEX_TIMESYSTEM,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Observation Time System");

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATION_NOTES.getKey(),
                                      "This observation kindly provided by John McKay for Starbase testing",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Observation Notes");

        //--------------------------------------------------------------------------------------
        // Channels

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_COUNT.getKey(),
                                      Integer.toString(dblSpectrum[0].length - INDEX_CHANNEL),
                                      REGEX_CHANNEL_COUNT,
                                      DataTypeDictionary.DECIMAL_INTEGER,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Channel Count");

        //--------------------------------------------------------------------------------------
        // Channel Labels
        // These end up on ControlPanel Tooltips

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ZERO,
                                      "Channel 0",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Channel Name");

        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATION_CHANNEL_NAME.getKey() + MetadataDictionary.SUFFIX_CHANNEL_ONE,
                                      "Channel 1",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Channel Name");

        //--------------------------------------------------------------------------------------
        // Axis Labels
        // There's not necessarily as many Axes as there are Channels...

        // Axis.X is Time
        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_X.getKey(),
                                      "Frequency Offset kHz",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The X axis Label");

        // These end up on ControlPanel Value Units
        MetadataHelper.addNewMetadata(listMetaData,
                                      MetadataDictionary.KEY_OBSERVATION_AXIS_LABEL_Y.getKey() + MetadataDictionary.SUFFIX_SERIES_ZERO,
                                      "Receiver Output Volts",
                                      REGEX_STRING,
                                      DataTypeDictionary.STRING,
                                      SchemaUnits.DIMENSIONLESS,
                                      "The Y axis Label");

        return (listMetaData);
        }


    /***********************************************************************************************
     * Get an XYDataset of the spectrum for the specified channel.
     * The incoming spectrum is (frequency_offset, signal).
     *
     * @param spectrum
     * @param key
     *
     * @return XYDataset
     */

    public static XYDataset getXYDataset(final Vector<Object> spectrum,
                                         final Comparable key)
        {
        final XYSeriesCollection xyData;

        // Prepare a XYSeriesCollection for the results
        xyData = new XYSeriesCollection();

        if ((spectrum != null)
            && (!spectrum.isEmpty()))
            {
            final Iterator iterSpectrum;
            final XYSeries xySeries;

            iterSpectrum = spectrum.iterator();
            xySeries = new XYSeries(key);

            while (iterSpectrum.hasNext())
                {
                final Vector data;

                data = (Vector) iterSpectrum.next();

                // The incoming spectrum is (frequency_offset, signal)
                xySeries.add((Double)data.get(0), (Double)data.get(1));
                }

            if ((xySeries != null)
                && (xySeries.getItemCount() > 0))
                {
                xyData.addSeries(xySeries);
                }
            }

        return (xyData);
        }


    /***********************************************************************************************
     * Some simple testing!
     *
     * @param args
     */

    public static void main(final String[] args)
        {
        System.out.println("size=" + HYDROGEN_SPECTRUM().length);

        System.out.println("Bin=" + getSpectrumBin(-1000, 0));
        System.out.println("Bin=" + getSpectrumBin(-1000, 1));
        System.out.println("Bin=" + getSpectrumBin(590, 0));
        System.out.println("Bin=" + getSpectrumBin(590, 1));
        System.out.println("Bin=" + getSpectrumBin(1000, 0));
        System.out.println("Bin=" + getSpectrumBin(1000, 1));

        // This should fail...
        //System.out.println("Bin=" + getSpectrumBin(1000, 2));

        final Vector<Object> vecTest;

//        vecTest = getSpectrumByOffsetAsList(0);
//
//        Iterator iterTest;
//
//        iterTest = vecTest.iterator();
//        while (iterTest.hasNext())
//            {
//            List data = (List) iterTest.next();
//
//            System.out.println("data=" + data.get(0) + " " + data.get(1));
//            }

        vecTest = getSpectrumByOffsetAsVector(-200, 455, 0);

        final Iterator iterTest2;

        iterTest2 = vecTest.iterator();
        while (iterTest2.hasNext())
            {
            final List data = (List) iterTest2.next();

            System.out.println("data=" + data.get(0) + " " + data.get(1));
            }
        }
    }
