/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * @author Alexey A. Petrenko
 * Dumbed-down for JavaME by CG 20121006
 */
package java.awt;


/**
 * BufferCapabilities
 *
 */
public class BufferCapabilities implements Cloneable {
    private final ImageCapabilities frontBufferCapabilities;
    private final ImageCapabilities backBufferCapabilities;
    private final FlipContents flipContents;

    public BufferCapabilities(ImageCapabilities frontBufferCapabilities, 
            ImageCapabilities backBufferCapabilities, FlipContents flipContents) {
        if (frontBufferCapabilities == null || backBufferCapabilities == null) {
            throw new IllegalArgumentException();
        }

        this.frontBufferCapabilities = frontBufferCapabilities;
        this.backBufferCapabilities = backBufferCapabilities;
        this.flipContents = flipContents;
    }

    public Object clone() {
        return new BufferCapabilities(frontBufferCapabilities, backBufferCapabilities, flipContents);
    }

    public ImageCapabilities getFrontBufferCapabilities() {
        return frontBufferCapabilities;
    }

    public ImageCapabilities getBackBufferCapabilities() {
        return backBufferCapabilities;
    }

    public FlipContents getFlipContents() {
        return flipContents;
    }

    public boolean isPageFlipping() {
        return flipContents != null;
    }

    public boolean isFullScreenRequired() {
        return false;
    }

    public boolean isMultiBufferAvailable() {
        return false;
    }

    public static final class FlipContents {
        public static final FlipContents BACKGROUND = new FlipContents();
        public static final FlipContents COPIED = new FlipContents();
        public static final FlipContents PRIOR = new FlipContents();
        public static final FlipContents UNDEFINED = new FlipContents();

        private FlipContents() {

        }

        public int hashCode() {
            return super.hashCode();
        }

        public String toString() {
            return super.toString();
        }
    }
}

