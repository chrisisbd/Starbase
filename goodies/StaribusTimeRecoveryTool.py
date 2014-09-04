__author__ = 'mark'
import argparse
import re
import datetime
import os
import sys

parser = argparse.ArgumentParser()
parser.add_argument("logfile", help="enter name of starbase log file to perform recovery on",
                    type=str)
parser.add_argument("instrument", help="enter one of the following instrument codes - vlf, 4ch, 8ch",
                    type=str)
parser.add_argument("outfile", help="enter name of output csv file",
                    type=str)
args = parser.parse_args()

lastmeta = '0000-00-00 00:00:00 +000 0000'


def recover(logfile, instrument, outfile):

    global lastmeta

    try:
        open(outfile, 'r')
        print "\nError an outfile with the name", outfile, "already exists\n"
        sys.exit()
    except IOError:
        pass

    print "\nStarting recovery on logfile - ", logfile, "\n"

    try:
        os.remove('tf.tmp')
    except OSError:
        pass

    with open(logfile) as f:
        for line in f:
            if "StaribusRxStream.read()" in line:  # find line with StaribusRxStream.read()
                x = line[67:]  # remove first 67 characters
                if len(x) > 23:  # check x is still greater than 23 characters
                    if "ResponseValue=" in x:  # next find line with ResponseValue=
                        a = x[14:]  # strip ResponseValue=
                        b = a[:512]  # take the next 512 characters as being the data
                        c = b + '\n'  # add back the newline as it's easier to parse
                        data = c[29:]
                        current = c[:29].split(' ')  # iterate the date, time, temperature, and rate

                        drop = False

                        if re.match('^\d{4}-\d{2}-\d{2}$', current[0]):  # match if date format is correct
                            pass
                        else:
                            if lastmeta[0] == '0':  # check to see if lastmeta command is empty
                                drop = True
                            else:
                                n = lastmeta[0].split('-')
                                b = lastmeta[1].split(':')
                                a = datetime.datetime(int(n[0]),int(n[1]),int(n[2]),int(b[0]),int(b[1]),int(b[2]))
                                c = a + datetime.timedelta(seconds=int(lastmeta[3].lstrip('0')))
                                current[0] = c.date()

                        if re.match('^\d{4}$', current[3]):  # match if rate is correct current[3]
                            pass
                        else:
                            if lastmeta[3] == '0':  # check to see if lastmeta rate is empty
                                drop = True
                            else:
                                current[3] = lastmeta[3]

                        if re.match('^\d{2}:\d{2}:\d{2}$', current[1]):  # match if time format is correct
                            pass
                        else:
                            if lastmeta[1] == '0':  # check to see if lastmeta command is empty
                                drop = True
                            else:
                                n = lastmeta[1].split(':')
                                a = datetime.datetime(101,1,1,int(n[0]),int(n[1]),int(n[2]))
                                b = a + datetime.timedelta(seconds=int(lastmeta[3].lstrip('0')))
                                current[1] = b.time()

                        if re.match('^[+/-]\d{3}$', current[2]):  # match if temp format is correct
                            pass
                        else:
                            if lastmeta[2] == '0':  # check to see if lastmeta command is empty
                                current[2] = '+000'
                            else:
                                current[2] = lastmeta[2]

                        if drop is False:
                            try:
                                value = ' '.join(current) + str(data)
                            except StandardError:
                                pass
                            else:
                                tf = open('tf.tmp', 'ab')
                                tf.write(value)
                                tf.close()
                                lastmeta = current
                    else:
                        pass
            else:
                pass

    try:
        open('tf.tmp', 'r')
    except IOError:
        pass
    else:
        start = open('tf.tmp', 'r').readline().strip('\r\n').split(' ')
        if instrument == 'vlf':
            metadata = "Observation.Axis.Label.X,Time (UT),String,Dimensionless,\r\nObservation.Axis.Label.Y.0,Multichannel Outputs,String,Dimensionless,\r\n" + \
                "Observation.Channel.Colour.0,r=255 g=102 b=000,ColourData,Dimensionless,The Colour of the Channel 0 graph\r\n" + \
                "Observation.Channel.Colour.Temperature,r=255 g=000 b=000,ColourData,Dimensionless,The Colour of the Temperature channel graph\r\n" + \
                "Observation.Channel.Count,2,DecimalInteger,Dimensionless,The number of channels of data produced by this Instrument\r\n" + \
                "Observation.Channel.DataType.0,DecimalInteger,DataType,Dimensionless,The DataType of channel 0\r\n" + \
                "Observation.Channel.DataType.Temperature,DecimalFloat,DataType,Dimensionless,The DataType of the Temperature channel\r\n" + \
                "Observation.Channel.Description.0,The output from Channel 0,String,Dimensionless,The Description of channel 0\r\n" + \
                "Observation.Channel.Description.Temperature,The temperature of the Controller module,String,Dimensionless,The Description of the Temperature channel\r\n" + \
                "Observation.Channel.Name.0,Channel 0,String,Dimensionless,The name of channel 0\r\n" + \
                "Observation.Channel.Name.Temperature,Temperature,String,Dimensionless,The name of the Temperature channel\r\n" + \
                "Observation.Channel.Units.0,mV,Units,Dimensionless,The Units of channel 0\r\n" + \
                "Observation.Channel.Units.Temperature,Celsius,Units,Dimensionless,The Units of the Temperature channel\r\n" + "Observation.Finish.Date," + \
                str(lastmeta[0]) + ",Date,YearMonthDay,The Date of the End of the Observation\r\nObservation.Finish.Time," + \
                str(lastmeta[1]) + ",Time,HourMinSec,The Time of the End of the Observation\r\nObservation.Start.Date," + \
                str(start[0]) + ",Date,YearMonthDay,The Date of the Start of the Observation\r\nObservation.Start.Time," + \
                str(start[1]) + ",Time,HourMinSec,The Time of the Start of the Observation\r\nObservation.Title,Staribus VLF Receiver,String,Dimensionless\r\n"
            open(outfile, 'a').write(metadata)
        elif instrument == '4ch':
            metadata = "Observation.Axis.Label.X,Time (UT),String,Dimensionless,\r\n" + \
                "Observation.Axis.Label.Y.0,Multichannel Outputs,String,Dimensionless,\r\n" + \
                "Observation.Channel.Colour.0,r=255 g=102 b=000,ColourData,Dimensionless,The Colour of the Channel 0 graph\r\n" + \
                "Observation.Channel.Colour.1,r=255 g=153 b=000,ColourData,Dimensionless,The Colour of the Channel 1 graph\r\n" + \
                "Observation.Channel.Colour.2,r=255 g=204 b=000,ColourData,Dimensionless,The Colour of the Channel 2 graph\r\n" + \
                "Observation.Channel.Colour.3,r=255 g=255 b=000,ColourData,Dimensionless,The Colour of the Channel 3 graph\r\n" + \
                "Observation.Channel.Colour.Temperature,r=255 g=000 b=000,ColourData,Dimensionless,The Colour of the Temperature channel graph\r\n" + \
                "Observation.Channel.Count,5,DecimalInteger,Dimensionless,The number of channels of data produced by this Instrument\r\n" + \
                "Observation.Channel.DataType.0,DecimalInteger,DataType,Dimensionless,The DataType of channel 0\r\n" + \
                "Observation.Channel.DataType.1,DecimalInteger,DataType,Dimensionless,The DataType of channel 1\r\n" + \
                "Observation.Channel.DataType.2,DecimalInteger,DataType,Dimensionless,The DataType of channel 2\r\n" + \
                "Observation.Channel.DataType.3,DecimalInteger,DataType,Dimensionless,The DataType of channel 3\r\n" + \
                "Observation.Channel.DataType.Temperature,DecimalFloat,DataType,Dimensionless,The DataType of the Temperature channel\r\n" + \
                "Observation.Channel.Description.0,The output from Channel 0,String,Dimensionless,The Description of channel 0\r\n" + \
                "Observation.Channel.Description.1,The output from Channel 1,String,Dimensionless,The Description of channel 1\r\n" + \
                "Observation.Channel.Description.2,The output from Channel 2,String,Dimensionless,The Description of channel 2\r\n" + \
                "Observation.Channel.Description.3,The output from Channel 3,String,Dimensionless,The Description of channel 3\r\n" + \
                "Observation.Channel.Description.Temperature,The temperature of the Controller module,String,Dimensionless,The Description of the Temperature channel\r\n" + \
                "Observation.Channel.Name.0,Channel 0,String,Dimensionless,The name of channel 0\r\n" + \
                "Observation.Channel.Name.1,Channel 1,String,Dimensionless,The name of channel 1\r\n" + \
                "Observation.Channel.Name.2,Channel 2,String,Dimensionless,The name of channel 2\r\n" + \
                "Observation.Channel.Name.3,Channel 3,String,Dimensionless,The name of channel 3\r\n" + \
                "Observation.Channel.Name.Temperature,Temperature,String,Dimensionless,The name of the Temperature channel\r\n" + \
                "Observation.Channel.Units.0,mV,Units,Dimensionless,The Units of channel 0\r\n" + \
                "Observation.Channel.Units.1,mV,Units,Dimensionless,The Units of channel 1\r\n" + \
                "Observation.Channel.Units.2,mV,Units,Dimensionless,The Units of channel 2\r\n" + \
                "Observation.Channel.Units.3,mV,Units,Dimensionless,The Units of channel 3\r\n" + \
                "Observation.Channel.Units.Temperature,Celsius,Units,Dimensionless,The Units of the Temperature channel\r\n" + \
                "Observation.Finish.Date," + str(lastmeta[0]) + ",Date,YearMonthDay,The Date of the End of the Observation\r\n" + \
                "Observation.Finish.Time," + str(lastmeta[1]) + ",Time,HourMinSec,The Time of the End of the Observation\r\n" + \
                "Observation.Start.Date," + str(start[0]) + ",Date,YearMonthDay,The Date of the Start of the Observation\r\n" + \
                "Observation.Start.Time," + str(start[1]) + ",Time,HourMinSec,The Time of the Start of the Observation\r\n" + \
                "Observation.Title,Staribus Multichannel Data Logger,String,Dimensionless,\r\n"
            open(outfile, 'a').write(metadata)
        elif instrument == '8ch':
            metadata = "Observation.Axis.Label.X,Time (UT),String,Dimensionless,\r\n" + \
                "Observation.Axis.Label.Y.0,Multichannel Outputs,String,Dimensionless,\r\n" + \
                "Observation.Channel.Colour.0, r=255 g=000 b=000,ColourData,Dimensionless,The Colour of the Channel 0 graph\r\n" + \
                "Observation.Channel.Colour.1, r=255 g=102 b=000,ColourData,Dimensionless,The Colour of the Channel 1 graph\r\n" + \
                "Observation.Channel.Colour.2, r=255 g=153 b=000,ColourData,Dimensionless,The Colour of the Channel 2 graph\r\n" + \
                "Observation.Channel.Colour.3, r=255 g=204 b=000,ColourData,Dimensionless,The Colour of the Channel 3 graph\r\n" + \
                "Observation.Channel.Colour.4, r=170 g=230 b=000,ColourData,Dimensionless,The Colour of the Channel 4 graph\r\n" + \
                "Observation.Channel.Colour.5, r=000 g=204 b=000,ColourData,Dimensionless,The Colour of the Channel 5 graph\r\n" + \
                "Observation.Channel.Colour.6, r=255 g=153 b=204,ColourData,Dimensionless,The Colour of the Channel 6 graph\r\n" + \
                "Observation.Channel.Colour.7, r=000 g=085 b=204,ColourData,Dimensionless,The Colour of the Channel 7 graph\r\n" + \
                "Observation.Channel.Colour.Temperature,r=255 g=000 b=000,ColourData,Dimensionless,The Colour of the Temperature channel graph\r\n" + \
                "Observation.Channel.Count,9,DecimalInteger,Dimensionless,The number of channels of data produced by this Instrument\r\n" + \
                "Observation.Channel.DataType.0,DecimalInteger,DataType,Dimensionless,The DataType of channel 0\r\n" + \
                "Observation.Channel.DataType.1,DecimalInteger,DataType,Dimensionless,The DataType of channel 1\r\n" + \
                "Observation.Channel.DataType.2,DecimalInteger,DataType,Dimensionless,The DataType of channel 2\r\n" + \
                "Observation.Channel.DataType.3,DecimalInteger,DataType,Dimensionless,The DataType of channel 3\r\n" + \
                "Observation.Channel.DataType.4,DecimalInteger,DataType,Dimensionless,The DataType of channel 4\r\n" + \
                "Observation.Channel.DataType.5,DecimalInteger,DataType,Dimensionless,The DataType of channel 5\r\n" + \
                "Observation.Channel.DataType.6,DecimalInteger,DataType,Dimensionless,The DataType of channel 6\r\n" + \
                "Observation.Channel.DataType.7,DecimalInteger,DataType,Dimensionless,The DataType of channel 7\r\n" + \
                "Observation.Channel.DataType.Temperature,DecimalFloat,DataType,Dimensionless,The DataType of the Temperature channel\r\n" + \
                "Observation.Channel.Description.0,The output from Channel 0,String,Dimensionless,The Description of channel 0\r\n" + \
                "Observation.Channel.Description.1,The output from Channel 1,String,Dimensionless,The Description of channel 1\r\n" + \
                "Observation.Channel.Description.2,The output from Channel 2,String,Dimensionless,The Description of channel 2\r\n" + \
                "Observation.Channel.Description.3,The output from Channel 3,String,Dimensionless,The Description of channel 3\r\n" + \
                "Observation.Channel.Description.4,The output from Channel 4,String,Dimensionless,The Description of channel 4\r\n" + \
                "Observation.Channel.Description.5,The output from Channel 5,String,Dimensionless,The Description of channel 5\r\n" + \
                "Observation.Channel.Description.6,The output from Channel 6,String,Dimensionless,The Description of channel 6\r\n" + \
                "Observation.Channel.Description.7,The output from Channel 7,String,Dimensionless,The Description of channel 7\r\n" + \
                "Observation.Channel.Description.Temperature,The temperature of the Controller module,String,Dimensionless,The Description of the Temperature channel\r\n" + \
                "Observation.Channel.Name.0,Channel 0,String,Dimensionless,The name of channel 0\r\n" + \
                "Observation.Channel.Name.1,Channel 1,String,Dimensionless,The name of channel 1\r\n" + \
                "Observation.Channel.Name.2,Channel 2,String,Dimensionless,The name of channel 2\r\n" + \
                "Observation.Channel.Name.3,Channel 3,String,Dimensionless,The name of channel 3\r\n" + \
                "Observation.Channel.Name.4,Channel 4,String,Dimensionless,The name of channel 4\r\n" + \
                "Observation.Channel.Name.5,Channel 5,String,Dimensionless,The name of channel 5\r\n" + \
                "Observation.Channel.Name.6,Channel 6,String,Dimensionless,The name of channel 6\r\n" + \
                "Observation.Channel.Name.7,Channel 7,String,Dimensionless,The name of channel 7\r\n" + \
                "Observation.Channel.Name.Temperature,Temperature,String,Dimensionless,The name of the Temperature channel\r\n" + \
                "Observation.Channel.Units.0,mV,Units,Dimensionless,The Units of channel 0\r\n" + \
                "Observation.Channel.Units.1,mV,Units,Dimensionless,The Units of channel 1\r\n" + \
                "Observation.Channel.Units.2,mV,Units,Dimensionless,The Units of channel 2\r\n" + \
                "Observation.Channel.Units.3,mV,Units,Dimensionless,The Units of channel 3\r\n" + \
                "Observation.Channel.Units.4,mV,Units,Dimensionless,The Units of channel 4\r\n" + \
                "Observation.Channel.Units.5,mV,Units,Dimensionless,The Units of channel 5\r\n" + \
                "Observation.Channel.Units.6,mV,Units,Dimensionless,The Units of channel 6\r\n" + \
                "Observation.Channel.Units.7,mV,Units,Dimensionless,The Units of channel 7\r\n" + \
                "Observation.Channel.Units.Temperature,Celsius,Units,Dimensionless,The Units of the Temperature channel\r\n" + \
                "Observation.Finish.Date," + str(lastmeta[0]) + ",Date,YearMonthDay,The Date of the End of the Observation\r\n" + \
                "Observation.Finish.Time," + str(lastmeta[1]) + ",Time,HourMinSec,The Time of the End of the Observation\r\n" + \
                "Observation.Start.Date," + str(start[0]) + ",Date,YearMonthDay,The Date of the Start of the Observation\r\n" + \
                "Observation.Start.Time," + str(start[1]) + ",Time,HourMinSec,The Time of the Start of the Observation\r\n" + \
                "Observation.Title,Staribus Multichannel Data Logger,String,Dimensionless,\r\n"
            open(outfile, 'a').write(metadata)
        else:
            print "\nFatal error instrument unknown"
            sys.exit()

    with open('tf.tmp', 'r') as fp:
        f = open(outfile, 'a')
        for line in fp:

            print '.',
            block = line.split(' ')
            tp = block[2]

            if instrument == 'vlf':
                dt = str(block[0]) + ',' + str(block[1])
                for datum in re.findall('\d{4}', block[6]):
                    f.write(str(dt) + ',' + str(tp) + ',' + str(int(datum)) + '\r\n')
                    v = dt.split(',')
                    n = v[0].split('-')
                    b = v[1].split(':')
                    a = datetime.datetime(int(n[0]),int(n[1]),int(n[2]),int(b[0]),int(b[1]),int(b[2]))
                    c = a + datetime.timedelta(seconds=int(lastmeta[3].lstrip('0')))
                    dt = str(c.date()) + ',' + str(c.time())
            elif instrument == '4ch':
                dt = str(block[0]) + ',' + str(block[1])
                for datum in re.findall('\d{16}', block[6]):
                    dat = re.findall('....', str(datum))
                    f.write(str(dt) + ',' + str(tp) + ',' + str(int(dat[0])) + ',' + str(int(dat[1])) + ',' + str(int(dat[2])) \
                        + ',' + str(int(dat[3])) + '\r\n')
                    v = dt.split(',')
                    n = v[0].split('-')
                    b = v[1].split(':')
                    a = datetime.datetime(int(n[0]),int(n[1]),int(n[2]),int(b[0]),int(b[1]),int(b[2]))
                    c = a + datetime.timedelta(seconds=int(lastmeta[3].lstrip('0')))
                    dt = str(c.date()) + ',' + str(c.time())
            elif instrument == '8ch':
                dt = str(block[0]) + ',' + str(block[1])
                for datum in re.findall('\d{32}', block[6]):
                    dat = re.findall('....', str(datum))
                    f.write(str(dt) + ',' + str(tp) + ',' + str(int(dat[0])) + ',' + str(int(dat[1])) + ',' + str(int(dat[2])) + ',' + str(int(dat[3])) \
                        + ',' + str(int(dat[4])) + ',' + str(int(dat[5])) + ',' + str(int(dat[6])) + ',' + str(int(dat[7])) + '\r\n')
                    v = dt.split(',')
                    n = v[0].split('-')
                    b = v[1].split(':')
                    a = datetime.datetime(int(n[0]),int(n[1]),int(n[2]),int(b[0]),int(b[1]),int(b[2]))
                    c = a + datetime.timedelta(seconds=int(lastmeta[3].lstrip('0')))
                    dt = str(c.date()) + ',' + str(c.time())
            else:
                print "\nFatal error instrument unknown"
                sys.exit()


    count = len(open('tf.tmp', 'r').readline())

    try:
        os.remove('tf.tmp')
    except OSError:
        pass

    print "\n\nRecovered", str(count), "stardata blocks"
    print "Recovery Completed -", outfile, "\n"


if __name__ == "__main__":
    recover(args.logfile, args.instrument, args.outfile)
