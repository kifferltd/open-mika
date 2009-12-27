/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

/*
** [CG 20081124] Augmented using constants from Apache Harmony, see
** copyright notice below.
*/

package java.util.zip;

interface ZipConstants {

/*
** Some internal constants of ours, gathered here for convenience.
*/

	static final byte [] locFileHeaderS = { 80 , 75, 3, 4 };
	static final byte [] dataDescS = { 80, 75, 7, 8 };
	static final byte [] cenFileHeaderS = { 80 , 75, 1, 2 };
	static final byte [] endCenDirS = { 80 , 75, 5, 6 };
	static final int maxHeaderSize = 30;
	
	static final byte [] fileHeader = { 	80, 75, 1, 2,	// header signature
						20, 0, 20, 0,   // version mady by and version needed
						0, 0, 0, 0,     // general purpose + compr method *
						0, 0, 0, 0,	// last mod time and date *
						0, 0, 0, 0,	// crc-32 *
						0, 0, 0, 0,	// compressed size *
						0, 0, 0, 0,	// uncompressed size *
						0, 0, 		// filename length *
						0, 0,		// extra field
						0, 0, 		// file comment length *
						0, 0,		// disk start number *
						0, 0, 		// internal file attributes *
						0, 0, 0, 0,	// external file attributes *
						0, 0, 0, 0 };	// relative offset of local header *
                                             // all bytes marked with * should be set to have correct trailer;
                                             // if filename, file comment or extra field is non null,
                                             // then those fields must be written after thte header ...

      static final byte [] EndOfCDRecord = {	80 , 75, 5, 6,	// end of central dir signature
      						0, 0,		// number of this disk *	
                                                0, 0,		// number of disk with start of central Dir *
      						0, 0,		// nr of entries on this disk *
      						0, 0, 		// nr of entries in the central dir *
      						0, 0, 0, 0, 	// size of central dir offset of ... *
      						0, 0, 0, 0,	// starting disk number *
      						0, 0 	     };	// zipfile comment length *
                                             // all bytes marked with * should be set to have correct trailer;
												
/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

    public static final long LOCSIG = 0x4034b50, EXTSIG = 0x8074b50,
            CENSIG = 0x2014b50, ENDSIG = 0x6054b50;

    public static final int LOCHDR = 30, EXTHDR = 16, CENHDR = 46, ENDHDR = 22,
            LOCVER = 4, LOCFLG = 6, LOCHOW = 8, LOCTIM = 10, LOCCRC = 14,
            LOCSIZ = 18, LOCLEN = 22, LOCNAM = 26, LOCEXT = 28, EXTCRC = 4,
            EXTSIZ = 8, EXTLEN = 12, CENVEM = 4, CENVER = 6, CENFLG = 8,
            CENHOW = 10, CENTIM = 12, CENCRC = 16, CENSIZ = 20, CENLEN = 24,
            CENNAM = 28, CENEXT = 30, CENCOM = 32, CENDSK = 34, CENATT = 36,
            CENATX = 38, CENOFF = 42, ENDSUB = 8, ENDTOT = 10, ENDSIZ = 12,
            ENDOFF = 16, ENDCOM = 20;
}

