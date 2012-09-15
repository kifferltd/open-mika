/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.luni.internal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * A class used to initialize the time zone database.  This implementation uses the
 * 'zoneinfo' database as the source of time zone information.  However, to conserve
 * disk space the data for all time zones are concatenated into a single file, and a
 * second file is used to indicate the starting position of each time zone record.  A
 * third file indicates the version of the zoneinfo databse used to generate the data.
 *
 * {@hide}
 */
public final class ZoneInfoDB {
    private static final int TZNAME_LENGTH = 40;
    private static final int TZINT_LENGTH = 4;

    /**
     * The directory contining the time zone database files.
     */
    private static final String ZONE_DIRECTORY_NAME =
        System.getenv("ANDROID_ROOT") + "/usr/share/zoneinfo/";

    /**
     * The name of the file containing the concatenated time zone records.
     */
    private static final String ZONE_FILE_NAME =
        ZONE_DIRECTORY_NAME + "zoneinfo.dat";

    /**
     * The name of the file containing the index to each time zone record within
     * the zoneinfo.dat file.
     */
    private static final String INDEX_FILE_NAME =
        ZONE_DIRECTORY_NAME + "zoneinfo.idx";

    /**
     *  Zoneinfo version used prior to creation of the zoneinfo.version file,
     *  equal to "2007h".
     */
    private static final String DEFAULT_VERSION = "2007h";

    /**
     * The name of the file indicating the database version in use.  If the file is not
     * present or is unreadable, we assume a version of DEFAULT_VERSION.
     */
    private static final String VERSION_FILE_NAME =
        ZONE_DIRECTORY_NAME + "zoneinfo.version";

    private static Object lock = new Object();
    private static TimeZone defaultZone = null;

    private static String version;
    private static String[] names;
    private static int[] starts;
    private static int[] lengths;
    private static int[] offsets;

    /**
     * This class is uninstantiable.
     */
    private ZoneInfoDB() {
        // This space intentionally left blank.
    }

    private static void readVersion() throws IOException {
        RandomAccessFile versionFile = new RandomAccessFile(VERSION_FILE_NAME, "r");
        int len = (int) versionFile.length();
        byte[] vbuf = new byte[len];
        versionFile.readFully(vbuf);
        version = new String(vbuf, 0, len, "ISO-8859-1").trim();
        versionFile.close();
    }

    private static void readDatabase() throws IOException {
        if (starts != null) {
            return;
        }

        RandomAccessFile indexFile = new RandomAccessFile(INDEX_FILE_NAME, "r");
        byte[] nbuf = new byte[TZNAME_LENGTH];

        int numEntries =
            (int) (indexFile.length() / (TZNAME_LENGTH + 3*TZINT_LENGTH));

        char[] namebuf = new char[numEntries * TZNAME_LENGTH];
        int[] nameend = new int[numEntries];
        int nameoff = 0;

        starts = new int[numEntries];
        lengths = new int[numEntries];
        offsets = new int[numEntries];

        for (int i = 0; i < numEntries; i++) {
            indexFile.readFully(nbuf);
            starts[i] = indexFile.readInt();
            lengths[i] = indexFile.readInt();
            offsets[i] = indexFile.readInt();

            // Don't include null chars in the String
            int len = nbuf.length;
            for (int j = 0; j < len; j++) {
                if (nbuf[j] == 0) {
                    break;
                }
                namebuf[nameoff++] = (char) (nbuf[j] & 0xFF);
            }

            nameend[i] = nameoff;
        }

        String name = new String(namebuf, 0, nameoff);

        // Assumes the namebuf is all ASCII (so byte offsets == char offsets).
        names = new String[numEntries];
        for (int i = 0; i < numEntries; i++) {
            names[i] = name.substring(i == 0 ? 0 : nameend[i - 1],
                                      nameend[i]);
        }

        indexFile.close();
    }

    static {
        // Don't attempt to log here because the logger requires this class to be initialized
        try {
            readVersion();
        } catch (IOException e) {
            // The version can't be read, we can continue without it
            version = DEFAULT_VERSION;
        }

        try {
            readDatabase();
        } catch (IOException e) {
            // The database can't be read, try to continue without it
            names = new String[0];
            starts = new int[0];
            lengths = new int[0];
            offsets = new int[0];
        }
    }

    public static String getVersion() {
        return version;
    }

    public static String[] getAvailableIDs() {
        return _getAvailableIDs(0, false);
    }

    public static String[] getAvailableIDs(int rawOffset) {
        return _getAvailableIDs(rawOffset, true);
    }

    private static String[] _getAvailableIDs(int rawOffset,
            boolean checkOffset) {
        List<String> matches = new ArrayList<String>();

        int[] _offsets = ZoneInfoDB.offsets;
        String[] _names = ZoneInfoDB.names;
        int len = _offsets.length;
        for (int i = 0; i < len; i++) {
            if (!checkOffset || _offsets[i] == rawOffset) {
                matches.add(_names[i]);
            }
        }

        return matches.toArray(new String[matches.size()]);
    }

    /*package*/ static TimeZone _getTimeZone(String name)
            throws IOException {
        FileInputStream fis = null;
        int length = 0;

        File f = new File(ZONE_DIRECTORY_NAME + name);
        if (!f.exists()) {
            fis = new FileInputStream(ZONE_FILE_NAME);
            int i = Arrays.binarySearch(ZoneInfoDB.names, name);

            if (i < 0) {
                return null;
            }

            int start = ZoneInfoDB.starts[i];
            length = ZoneInfoDB.lengths[i];

            fis.skip(start);
        }

        if (fis == null) {
            fis = new FileInputStream(f);
            length = (int)f.length(); // data won't exceed 2G!
        }

        byte[] data = new byte[length];
        int nread = 0;
        while (nread < length) {
            int size = fis.read(data, nread, length - nread);
            if (size > 0) {
                nread += size;
            }
        }

        try {
            fis.close();
        } catch (IOException e3) {
            // probably better to continue than to fail here
            java.util.logging.Logger.global.warning("IOException " + e3 +
                " retrieving time zone data");
            e3.printStackTrace();
        }

        if (data.length < 36) {
            return null;
        }
        if (data[0] != 'T' || data[1] != 'Z' ||
            data[2] != 'i' || data[3] != 'f') {
            return null;
        }

        int ntransition = read4(data, 32);
        int ngmtoff = read4(data, 36);
        int base = 44;

        int[] transitions = new int[ntransition];
        for (int i = 0; i < ntransition; i++)
            transitions[i] = read4(data, base + 4 * i);
        base += 4 * ntransition;

        byte[] type = new byte[ntransition];
        for (int i = 0; i < ntransition; i++)
            type[i] = data[base + i];
        base += ntransition;

        int[] gmtoff = new int[ngmtoff];
        byte[] isdst = new byte[ngmtoff];
        byte[] abbrev = new byte[ngmtoff];
        for (int i = 0; i < ngmtoff; i++) {
            gmtoff[i] = read4(data, base + 6 * i);
            isdst[i] = data[base + 6 * i + 4];
            abbrev[i] = data[base + 6 * i + 5];
        }

        base += 6 * ngmtoff;

        return new ZoneInfo(name, transitions, type, gmtoff, isdst, abbrev, data, base);
    }

    private static int read4(byte[] data, int off) {
        return ((data[off    ] & 0xFF) << 24) |
               ((data[off + 1] & 0xFF) << 16) |
               ((data[off + 2] & 0xFF) <<  8) |
               ((data[off + 3] & 0xFF) <<  0);
    }

    public static TimeZone getTimeZone(String id) {
        if (id != null) {
            if (id.equals("GMT") || id.equals("UTC")) {
                TimeZone tz = new MinimalTimeZone(0);
                tz.setID(id);
                return tz;
            }

            if (id.startsWith("GMT")) {
                return new MinimalTimeZone(parseNumericZone(id) * 1000);
            }
        }

        TimeZone tz = ZoneInfo.getTimeZone(id);

        if (tz != null)
            return tz;

        /*
         * It isn't GMT+anything, and it also isn't something we have
         * in the database.  Give up and return GMT.
         */
        tz = new MinimalTimeZone(0);
        tz.setID("GMT");
        return tz;
    }

    public static TimeZone getDefault() {
        TimeZone zone;

        synchronized (lock) {
            if (defaultZone != null) {
                return defaultZone;
            }

            String zoneName = null;
            TimezoneGetter tzGetter = TimezoneGetter.getInstance();
            if (tzGetter != null) {
                zoneName = tzGetter.getId();
            }
            if (zoneName != null && zoneName.length() > 0) {
                zone = TimeZone.getTimeZone(zoneName.trim());
            } else {
                // use localtime here so that the simulator works
                zone = TimeZone.getTimeZone("localtime");
            }

            defaultZone = zone;
        }
        return zone;
    }

    // TODO - why does this ignore the 'zone' parameter?
    public static void setDefault(@SuppressWarnings("unused") TimeZone zone) {
        /*
         * if (zone == null), the next call to getDefault will set it to the
         * the system's default time zone.
         */
        synchronized (lock) {
            defaultZone = null;
        }
    }

    private static int parseNumericZone(String name) {
        if (name == null)
            return 0;

        if (!name.startsWith("GMT"))
            return 0;

        if (name.length() == 3)
            return 0;

        int sign;
        if (name.charAt(3) == '+')
            sign = 1;
        else if (name.charAt(3) == '-')
            sign = -1;
        else
            return 0;

        int where;
        int hour = 0;
        boolean colon = false;
        for (where = 4; where < name.length(); where++) {
            char c = name.charAt(where);

            if (c == ':') {
                where++;
                colon = true;
                break;
            }

            if (c >= '0' && c <= '9')
                hour = hour * 10 + c - '0';
            else
                return 0;
        }

        int min = 0;
        for (; where < name.length(); where++) {
            char c = name.charAt(where);

            if (c >= '0' && c <= '9')
                min = min * 10 + c - '0';
            else
                return 0;
        }

        if (colon)
            return sign * (hour * 60 + min) * 60;
        else if (hour >= 100)
            return sign * ((hour / 100) * 60 + (hour % 100)) * 60;
        else
            return sign * (hour * 60) * 60;
    }

    /*package*/ static class MinimalTimeZone extends TimeZone {
        private int rawOffset;

        public MinimalTimeZone(int offset) {
            rawOffset = offset;
            setID(getDisplayName());
        }

        @SuppressWarnings("unused")
        @Override
        public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
            return getRawOffset();
        }

        @Override
        public int getRawOffset() {
            return rawOffset;
        }

        @Override
        public void setRawOffset(int off) {
            rawOffset = off;
        }

        @SuppressWarnings("unused")
        @Override
        public boolean inDaylightTime(Date when) {
            return false;
        }

        @Override
        public boolean useDaylightTime() {
            return false;
        }
    }
}
