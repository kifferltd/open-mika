/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.luni.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Utility functions for IPV6 operations.
 */
public class Inet6Util {


    public static boolean isIP6AddressInFullForm(String ipAddress) {
        if (isValidIP6Address(ipAddress)) {
            int doubleColonIndex = ipAddress.indexOf("::");
            if (doubleColonIndex >= 0) {
                // Simplified form which contains ::
                return false;
            }
            return true;
        }
        return false;
    }

	public static boolean isValidIP6Address(String ipAddress) {
		int length = ipAddress.length();
		boolean doubleColon = false;
		int numberOfColons = 0;
		int numberOfPeriods = 0;
		int numberOfPercent = 0;
		String word = "";
		char c = 0;
		char prevChar = 0;
		int offset = 0; // offset for [] IP addresses

		if (length < 2) {
            return false;
        }

		for (int i = 0; i < length; i++) {
			prevChar = c;
			c = ipAddress.charAt(i);
			switch (c) {

			// case for an open bracket [x:x:x:...x]
			case '[':
				if (i != 0) {
                    return false; // must be first character
                }
				if (ipAddress.charAt(length - 1) != ']') {
                    return false; // must have a close ]
                }
				offset = 1;
				if (length < 4) {
                    return false;
                }
				break;

			// case for a closed bracket at end of IP [x:x:x:...x]
			case ']':
				if (i != length - 1) {
                    return false; // must be last character
                }
				if (ipAddress.charAt(0) != '[') {
                    return false; // must have a open [
                }
				break;

			// case for the last 32-bits represented as IPv4 x:x:x:x:x:x:d.d.d.d
			case '.':
				numberOfPeriods++;
				if (numberOfPeriods > 3) {
                    return false;
                }
				if (!isValidIP4Word(word)) {
                    return false;
                }
				if (numberOfColons != 6 && !doubleColon) {
                    return false;
                }
				// a special case ::1:2:3:4:5:d.d.d.d allows 7 colons with an
				// IPv4 ending, otherwise 7 :'s is bad
				if (numberOfColons == 7 && ipAddress.charAt(0 + offset) != ':'
						&& ipAddress.charAt(1 + offset) != ':') {
                    return false;
                }
				word = "";
				break;

			case ':':
				numberOfColons++;
				if (numberOfColons > 7) {
                    return false;
                }
				if (numberOfPeriods > 0) {
                    return false;
                }
				if (prevChar == ':') {
					if (doubleColon) {
                        return false;
                    }
					doubleColon = true;
				}
				word = "";
				break;
			case '%':
				if (numberOfColons == 0) {
                    return false;
                }
				numberOfPercent++;

				// validate that the stuff after the % is valid
				if ((i + 1) >= length) {
					// in this case the percent is there but no number is
					// available
					return false;
				}
				try {
					Integer.parseInt(ipAddress.substring(i + 1));
				} catch (NumberFormatException e) {
					// right now we just support an integer after the % so if
					// this is not
					// what is there then return
					return false;
				}
				break;

			default:
				if (numberOfPercent == 0) {
					if (word.length() > 3) {
                        return false;
                    }
					if (!isValidHexChar(c)) {
                        return false;
                    }
				}
				word += c;
			}
		}

		// Check if we have an IPv4 ending
		if (numberOfPeriods > 0) {
			if (numberOfPeriods != 3 || !isValidIP4Word(word)) {
                return false;
            }
		} else {
			// If we're at then end and we haven't had 7 colons then there is a
			// problem unless we encountered a doubleColon
			if (numberOfColons != 7 && !doubleColon) {
				return false;
			}

			// If we have an empty word at the end, it means we ended in either
			// a : or a .
			// If we did not end in :: then this is invalid
			if (numberOfPercent == 0) {
				if (word == "" && ipAddress.charAt(length - 1 - offset) == ':'
						&& ipAddress.charAt(length - 2 - offset) != ':') {
					return false;
				}
			}
		}

		return true;
	}

	public static boolean isValidIP4Word(String word) {
		char c;
		if (word.length() < 1 || word.length() > 3) {
            return false;
        }
		for (int i = 0; i < word.length(); i++) {
			c = word.charAt(i);
			if (!(c >= '0' && c <= '9')) {
                return false;
            }
		}
		if (Integer.parseInt(word) > 255) {
            return false;
        }
		return true;
	}

	static boolean isValidHexChar(char c) {

		return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')
				|| (c >= 'a' && c <= 'f');
	}

	/**
	 * Takes a string and parses it to see if it is a valid IPV4 address.
	 *
	 * @return true, if the string represents an IPV4 address in dotted
	 *         notation, false otherwise
	 */
	public static boolean isValidIPV4Address(String value) {
            // BEGIN android-changed
            // general test
            if (!value.matches("\\p{Digit}+(\\.\\p{Digit}+)*")) {
                return false;
            }

            String[] parts = value.split("\\.");
            int length = parts.length;
            if (length < 1 || length > 4) {
                return false;
            }

            if (length == 1) {
                // One part decimal numeric address
                long longValue = Long.parseLong(parts[0]);
                return longValue <= 0xFFFFFFFFL;
            } else {
                // Test each part for inclusion in the correct range
                for (int i = 0; i < length; i++) {
                    // For two part addresses, the second part expresses
                    // a 24-bit quantity; for three part addresses, the third
                    // part expresses a 16-bit quantity.
                    int max = 0xff;
                    if ((length == 2) && (i == 1)) {
                        max = 0xffffff;
                    } else if ((length == 3) && (i == 2)) {
                        max = 0xffff;
                    }
                    if (Integer.parseInt(parts[i]) > max) {
                        return false;
                    }
                }
                return true;
            }
            // END android-changed
	}

}
