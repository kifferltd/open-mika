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

package org.apache.harmony.luni.tests.java.lang;

//import dalvik.annotation.AndroidOnly;
//import dalvik.annotation.BrokenTest;
//import dalvik.annotation.KnownFailure;
//import dalvik.annotation.TestTargets;
//import dalvik.annotation.TestLevel;
//import dalvik.annotation.TestTargetNew;
//import dalvik.annotation.TestTargetClass;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.NoSuchElementException;

//@TestTargetClass(ClassLoader.class) 
public class ClassLoaderTest extends TestCase {
    
    private static final String SYSTEM_RESOURCE_PATH = "META-INF/MANIFEST.MF";
    public static volatile int flag;

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassLoader",
        args = {}
    )
    */
    public void test_ClassLoader() {
        PublicClassLoader pcl = new PublicClassLoader();
        SecurityManager sm = new SecurityManager() {
            RuntimePermission rp = new RuntimePermission("getProtectionDomain");

            public void checkCreateClassLoader() {
                throw new SecurityException();
            }
            
            public void checkPermission(Permission perm) {
                if (perm.equals(rp)) {
                    throw new SecurityException();
                }
            }
        };

        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            new PublicClassLoader();
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
            // expected
        } finally {
            System.setSecurityManager(oldSm);
        }
    } 
 
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ClassLoader",
        args = {java.lang.ClassLoader.class}
    )
    @BrokenTest("Infinite loop in classloader. Actually a known failure.")
    */
    public void test_ClassLoaderLClassLoader() {
      PublicClassLoader pcl = new PublicClassLoader(
                                            ClassLoader.getSystemClassLoader());
      assertEquals(ClassLoader.getSystemClassLoader(), pcl.getParent());
      
      SecurityManager sm = new SecurityManager() {
          RuntimePermission rp = new RuntimePermission("getProtectionDomain");

          public void checkCreateClassLoader() {
              throw new SecurityException();
          }
                  
          public void checkPermission(Permission perm) {
              if (perm.equals(rp)) {
                  throw new SecurityException();
              }
          }
      };

      SecurityManager oldSm = System.getSecurityManager();
      System.setSecurityManager(sm);
      try {
          new PublicClassLoader(ClassLoader.getSystemClassLoader());
          fail("SecurityException should be thrown.");
      } catch (SecurityException e) {
          // expected
      } finally {
          System.setSecurityManager(oldSm);
      }
    }     

    /*
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        notes = "",
        method = "clearAssertionStatus",
        args = {}
    )
    @KnownFailure("Android doesn't support assertions to be activated " +
            "through the api")
    */
    public void test_clearAssertionStatus() {
        String className = getClass().getPackage().getName() + ".TestAssertions";
        String className1 = getClass().getPackage().getName() + ".TestAssertions1";
        ClassLoader cl = getClass().getClassLoader();
        cl.setClassAssertionStatus("TestAssertions", true);
        cl.setDefaultAssertionStatus(true);
        try {
          
            Class clazz = cl.loadClass(className);
            
              TestAssertions ta = (TestAssertions) clazz.newInstance();
              try {
                  ta.test();
                  fail("AssertionError should be thrown.");
              } catch(AssertionError ae) {
                  //expected
              }
              cl.clearAssertionStatus();
              clazz = cl.loadClass(className1);
              
              TestAssertions1 ta1 = (TestAssertions1) clazz.newInstance();
              try {
                  ta1.test();
              } catch(AssertionError ae) {
                  fail("AssertionError should not be thrown.");
              }
              
        } catch(Exception cnfe) {
            fail("Unexpected exception: " + cnfe.toString());
        }
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This method is not supported. " +
                "UnsupportedOperationException should be thrown.",
        method = "defineClass",
        args = {byte[].class, int.class, int.class}
    )
    @AndroidOnly("define methods re not supported on Android. " +
            "UnsupportedOperationException should be thrown.")
    */
    public void test_defineClassLbyteArrayLintLint() throws Exception {

        try {
            Class<?> a = new Ldr().define(Ldr.TEST_CASE_DEFINE_1);
            //assertEquals("org.apache.harmony.luni.tests.java.lang.A", a.getName());
            fail("UnsupportedOperationException was not thrown.");
        } catch(UnsupportedOperationException uoe) {
            //expected
        }
        
        try {
            new Ldr().define(1000, Ldr.TEST_CASE_DEFINE_1);
            fail("IndexOutOfBoundsException is not thrown.");
        } catch(IndexOutOfBoundsException  ioobe) {
            fail("UnsupportedOperationException should be thrown.");
        } catch(UnsupportedOperationException uoe) {
            //expected  
        }
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This method is not supported. UnsupportedOperationException should be thrown.",
        method = "defineClass",
        args = {java.lang.String.class, byte[].class, int.class, int.class, java.security.ProtectionDomain.class}
    )
    @AndroidOnly("define methods re not supported on Android. " +
            "UnsupportedOperationException should be thrown.")    
    */
    public void test_defineClassLjava_lang_StringLbyteArrayLintLintLProtectionDomain()
                    throws Exception {
        
        try {
            Class<?> a = new Ldr().define(Ldr.TEST_CASE_DEFINE_2);
            //assertEquals("org.apache.harmony.luni.tests.java.lang.A", a.getName());
            //assertEquals(getClass().getProtectionDomain(), a.getProtectionDomain());
            fail("UnsupportedOperationException was not thrown.");
        } catch(UnsupportedOperationException uoe) {
            //expected
        }        
        
        try {
            new Ldr().define(1000, Ldr.TEST_CASE_DEFINE_2);
            fail("IndexOutOfBoundsException is not thrown.");
        } catch(IndexOutOfBoundsException  ioobe) {
            fail("UnsupportedOperationException should be thrown.");
        } catch(UnsupportedOperationException uoe) {
            //expected  
        }
        
      /*try {
            ErrorLdr loader = new ErrorLdr();
            
            try {
                loader.define("WrongFormatClass", Ldr.TEST_CASE_DEFINE_2);
                fail("ClassFormatError should be thrown.");
            } catch (ClassFormatError le) {
                //expected
            } catch(UnsupportedOperationException uoe) {
                //expected  
            }
            
            try {
                loader.defineWrongName("TestClass", 0);
                fail("NoClassDefFoundError should be thrown.");
            } catch (NoClassDefFoundError ncdfe) {
                //expected
            } catch(UnsupportedOperationException uoe) {
                //expected  
            }            
            
            try {
                Class<?> clazz = loader.defineNullDomain("TestClass", 0);
                assertEquals(getClass().getProtectionDomain(), 
                        clazz.getProtectionDomain());
            } catch(UnsupportedOperationException uoe) {
                //expected  
            }
                      
        } catch(Exception e) {
            fail("Unexpected exception was thrown: " + e.toString());
        }
        */
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This method is not supported. UnsupportedOperationException should be thrown.",
        method = "defineClass",
        args = {java.lang.String.class, java.nio.ByteBuffer.class, java.security.ProtectionDomain.class}
    )
    @AndroidOnly("define methods re not supported on Android. " +
            "UnsupportedOperationException should be thrown.")    
    */
    public void test_defineClassLjava_lang_StringLByteBufferLProtectionDomain() {
         
        try {
            try {
                Class<?> a = new Ldr().define(Ldr.TEST_CASE_DEFINE_3);
                //assertEquals("org.apache.harmony.luni.tests.java.lang.A", a.getName());
                //assertEquals(getClass().getProtectionDomain(), a.getProtectionDomain());
                fail("UnsupportedOperationException was not thrown.");
            } catch(UnsupportedOperationException uoe) {
                //expected
            }                  
        
            try {
                new Ldr().define(1000, Ldr.TEST_CASE_DEFINE_3);
                fail("IndexOutOfBoundsException is not thrown.");
            } catch(IndexOutOfBoundsException  ioobe) {
                fail("UnsupportedOperationException should be thrown.");
            } catch(UnsupportedOperationException uoe) {
                //expected  
            }
    
        } catch(Exception e) {
            fail("Unexpected exception was thrown: " + e.toString());
        }        
    }
    
    /**
     * Tests that Classloader.defineClass() assigns appropriate 
     * default domains to the defined classes.
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "This method is not supported. " +
                "UnsupportedOperationException should be thrown.",
        method = "defineClass",
        args = {java.lang.String.class, byte[].class, int.class, int.class}
    )
    @AndroidOnly("define methods re not supported on Android. " +
            "UnsupportedOperationException should be thrown.")  
     */
    public void test_defineClass_defaultDomain() throws Exception {
        try {
            Class<?> a = new Ldr().define(Ldr.TEST_CASE_DEFINE_0);
            fail("UnsupportedOperationException was not thrown.");
        } catch(UnsupportedOperationException uoe) {
            //expected
        }  
        
        try {
            new Ldr().define(1000, Ldr.TEST_CASE_DEFINE_0);
            fail("IndexOutOfBoundsException is not thrown.");
        } catch(IndexOutOfBoundsException  ioobe) {
            fail("UnsupportedOperationException should be thrown.");
        } catch(UnsupportedOperationException uoe) {
            //expected  
        }
    }
    
    static class SyncTestClassLoader extends ClassLoader {
        Object lock;
        volatile int numFindClassCalled;
        
        SyncTestClassLoader(Object o) {
            this.lock = o;
            numFindClassCalled = 0;
        }

        /*
         * Byte array of bytecode equivalent to the following source code:
         * public class TestClass {
         * }
         */
        private byte[] classData = new byte[] {
            -54, -2, -70, -66, 0, 0, 0, 49, 0, 13,
            10, 0, 3, 0, 10, 7, 0, 11, 7, 0,
            12, 1, 0, 6, 60, 105, 110, 105, 116, 62,
            1, 0, 3, 40, 41, 86, 1, 0, 4, 67,
            111, 100, 101, 1, 0, 15, 76, 105, 110, 101,
            78, 117, 109, 98, 101, 114, 84, 97, 98, 108,
            101, 1, 0, 10, 83, 111, 117, 114, 99, 101,
            70, 105, 108, 101, 1, 0, 14, 84, 101, 115,
            116, 67, 108, 97, 115, 115, 46, 106, 97, 118,
            97, 12, 0, 4, 0, 5, 1, 0, 9, 84,
            101, 115, 116, 67, 108, 97, 115, 115, 1, 0,
            16, 106, 97, 118, 97, 47, 108, 97, 110, 103,
            47, 79, 98, 106, 101, 99, 116, 0, 33, 0,
            2, 0, 3, 0, 0, 0, 0, 0, 1, 0,
            1, 0, 4, 0, 5, 0, 1, 0, 6, 0,
            0, 0, 29, 0, 1, 0, 1, 0, 0, 0,
            5, 42, -73, 0, 1, -79, 0, 0, 0, 1,
            0, 7, 0, 0, 0, 6, 0, 1, 0, 0,
            0, 1, 0, 1, 0, 8, 0, 0, 0, 2,
            0, 9 };

        protected Class findClass(String name) throws ClassNotFoundException {
            try {
                while (flag != 2) {
                    synchronized (lock) {
                        lock.wait();
                    }
                }
            } catch (InterruptedException ie) {}

            if (name.equals("TestClass")) {
                numFindClassCalled++;
                return defineClass(null, classData, 0, classData.length);
            } else {
                throw new ClassNotFoundException("Class " + name + " not found.");
            }
        }
    }
    
    static class SyncLoadTestThread extends Thread {
        volatile boolean started;
        ClassLoader cl;
        Class cls;
        
        SyncLoadTestThread(ClassLoader cl) {
            this.cl = cl;
        }
        
        public void run() {
            try {
                started = true;
                cls = Class.forName("TestClass", false, cl);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Regression test for HARMONY-1939:
     * 2 threads simultaneously run Class.forName() method for the same classname 
     * and the same classloader. It is expected that both threads succeed but
     * class must be defined just once.  
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Regression test.",
        method = "loadClass",
        args = {java.lang.String.class}
    )
    @BrokenTest("Defining classes not supported, unfortunately the test appears"
            + " to succeed, which is not true, so marking it broken.")
     */
    public void test_loadClass_concurrentLoad() throws Exception 
    {    
        Object lock = new Object();
        SyncTestClassLoader cl = new SyncTestClassLoader(lock);
        SyncLoadTestThread tt1 = new SyncLoadTestThread(cl);
        SyncLoadTestThread tt2 = new SyncLoadTestThread(cl);
        flag = 1;
        tt1.start();
        tt2.start();

        while (!tt1.started && !tt2.started) {
            Thread.sleep(100);
        }

        flag = 2;
        synchronized (lock) {
            lock.notifyAll();
        }
        tt1.join();
        tt2.join();
        
        assertSame("Bad or redefined class", tt1.cls, tt2.cls);
        assertEquals("Both threads tried to define class", 1, cl.numFindClassCalled);
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "loadClass",
        args = {java.lang.String.class}
    )    
    */
    public void test_loadClassLjava_lang_String() {
        
        String [] classNames = {"org.apache.harmony.luni.tests.java.lang.TestClass1",
                "org.apache.harmony.luni.tests.java.lang.TestClass3",
                "org.apache.harmony.luni.tests.java.lang.A"};
        
        ClassLoader cl = getClass().getClassLoader();
        for(String str:classNames) {
            try {
                Class<?> clazz = cl.loadClass(str);
                assertNotNull(clazz);
                assertEquals(str, clazz.getName());
                if(str.endsWith("A"))
                    clazz.newInstance();
                if(str.endsWith("TestClass1")) {
                    try {
                        clazz.newInstance();
                        fail("ExceptionInInitializerError was not thrown.");
                    } catch(ExceptionInInitializerError eiine) {
                        //expected
                    }
                }
                if(str.endsWith("TestClass3")) {
                    try {
                        clazz.newInstance();
                        fail("IllegalAccessException was not thrown.");
                    } catch(IllegalAccessException ie) {
                        //expected
                    }
                }
            } catch (ClassNotFoundException e) {
                fail("ClassNotFoundException was thrown." + e.getMessage());
            } catch (InstantiationException e) {
                fail("InstantiationException was thrown.");                
            } catch (IllegalAccessException e) {
                fail("IllegalAccessException was thrown.");                 
            }            
        }
        
        try {
            Class<?> clazz = cl.loadClass("org.apache.harmony.luni.tests.java.lang.TestClass4");
            fail("ClassNotFoundException was not thrown.");
        } catch (ClassNotFoundException e) {
            //expected
        }   
        
        try {
            Class<?> clazz = cl.loadClass("org.apache.harmony.luni.tests.java.lang.TestClass5");
            fail("ClassNotFoundException was not thrown.");
        } catch (ClassNotFoundException e) {
            //expected
        }                
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "loadClass",
        args = {java.lang.String.class, boolean.class}
    )
    */
    public void test_loadClassLjava_lang_StringLZ() throws
            IllegalAccessException, InstantiationException,
            ClassNotFoundException {
        PackageClassLoader pcl = new PackageClassLoader(
                getClass().getClassLoader());
        String className = getClass().getPackage().getName() + ".A";

        Class<?> clazz = pcl.loadClass(className, false);
        assertEquals(className, clazz.getName());
        assertNotNull(clazz.newInstance());
        
        clazz = pcl.loadClass(className, true);
        assertEquals(className, clazz.getName());
        assertNotNull(clazz.newInstance());
            
        try {
            clazz = pcl.loadClass("UnknownClass", false);
            assertEquals("TestClass", clazz.getName());
            fail("ClassNotFoundException was not thrown.");
        } catch (ClassNotFoundException e) {
            //expected
        }
    }
    
    /**
     * @tests java.lang.ClassLoader#getResource(java.lang.String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getResource",
        args = {java.lang.String.class}
    )
     */
    public void test_getResourceLjava_lang_String() {
        // Test for method java.net.URL
        // java.lang.ClassLoader.getResource(java.lang.String)
        java.net.URL u = getClass().getClassLoader().getResource("hyts_Foo.c");
        assertNotNull("Unable to find resource", u);
        java.io.InputStream is = null;
        try {
            is = u.openStream();
            assertNotNull("Resource returned is invalid", is);
            is.close();
        } catch (java.io.IOException e) {
            fail("IOException getting stream for resource : " + e.getMessage());
        }
        
        
        
        assertNull(getClass().getClassLoader()
                .getResource("not.found.resource"));
    }

    /**
     * @tests java.lang.ClassLoader#getResourceAsStream(java.lang.String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getResourceAsStream",
        args = {java.lang.String.class}
    )
     */
    public void test_getResourceAsStreamLjava_lang_String() {
        // Test for method java.io.InputStream
        // java.lang.ClassLoader.getResourceAsStream(java.lang.String)
        // Need better test...

        java.io.InputStream is = null;
        assertNotNull("Failed to find resource: HelloWorld.txt",
                (is = getClass().getClassLoader()
                        .getResourceAsStream("HelloWorld.txt")));

        byte [] array = new byte[13];
        try {
            is.read(array);
        } catch(IOException ioe) {
            fail("IOException was not thrown.");
        } finally {
            try {
                is.close();
            } catch(IOException ioe) {}
        }       
        
        assertEquals("Hello, World.", new String(array));
                
        
        try {
            is.close();
        } catch (java.io.IOException e) {
            fail("Exception during getResourceAsStream: " + e.toString());
        }
        
        assertNull(getClass().getClassLoader().
                getResourceAsStream("unknownResource.txt"));
    }

    /**
     * @tests java.lang.ClassLoader#getSystemClassLoader()
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "",
        method = "getSystemClassLoader",
        args = {}
    )
    */
    public void test_getSystemClassLoader() {
        // Test for method java.lang.ClassLoader
        // java.lang.ClassLoader.getSystemClassLoader()
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        java.io.InputStream is = cl.getResourceAsStream(SYSTEM_RESOURCE_PATH);
        assertNotNull("Failed to find resource from system classpath", is);
        try {
            is.close();
        } catch (java.io.IOException e) {
        }

        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
                if(perm.getName().equals("getClassLoader")) {
                   throw new SecurityException(); 
                }
            }
        };    
        
        SecurityManager oldManager = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            ClassLoader.getSystemClassLoader();
        } catch(SecurityException se) {
            //expected
        } finally {
            System.setSecurityManager(oldManager);
        }
/* 
 *       // java.lang.Error is not thrown on RI, but it's specified.  
 *       
 *       String keyProp = "java.system.class.loader";
 *       String oldProp = System.getProperty(keyProp);
 *       System.setProperty(keyProp, "java.test.UnknownClassLoader");
 *       boolean isFailed = false;
 *       try {
 *           ClassLoader.getSystemClassLoader();
 *           isFailed = true;
 *       } catch(java.lang.Error e) {
 *           //expected
 *       } finally {
 *           if(oldProp == null) {
 *               System.clearProperty(keyProp);
 *           }  else {
 *               System.setProperty(keyProp, oldProp);
 *           }
 *       }
 *       assertFalse("java.lang.Error was not thrown.", isFailed);
 */       
    }

    /**
     * @tests java.lang.ClassLoader#getSystemResource(java.lang.String)
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSystemResource",
        args = {java.lang.String.class}
    )
    @AndroidOnly("The RI doesn't have a classes.dex as resource in the "
            + "core-tests.jar. Also on Android this file is the only one "
            + "that is sure to exist.")
     */
    public void test_getSystemResourceLjava_lang_String() throws IOException {
        // java.lang.ClassLoader.getSystemResource(java.lang.String)
        // Need better test...

        
        //String classResource = getClass().getPackage().getName().replace(".", "/") + "/" +
        //                        getClass().getSimpleName()  + ".class";
        //assertNotNull("Failed to find resource: " + classResource, 
        //        ClassLoader.getSystemResource(classResource));   
        
        URL url = getClass().getClassLoader().getSystemResource(SYSTEM_RESOURCE_PATH);
        assertNotNull(String.format("Failed to find resource: %s", SYSTEM_RESOURCE_PATH), url);
        java.io.InputStream is = url.openStream();

        assertTrue("System resource not found", is.available() > 0);
        assertNull("Doesn't return null for unknown resource.", 
                getClass().getClassLoader().getSystemResource("NotFound"));   
    }
        
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSystemResourceAsStream",
        args = {java.lang.String.class}
    )
    @AndroidOnly("The RI doesn't have a classes.dex as resource in the "
            + "core-tests.jar. Also on Android this file is the only one "
            + "that is sure to exist.")
    */
    public void test_getSystemResourceAsStreamLjava_lang_String()
            throws IOException {

        //String classResource = getClass().getPackage().getName().replace(".", "/") + "/" +
        //                    getClass().getSimpleName()  + ".class";
        //assertNotNull("Failed to find resource: " + classResource, 
        //            ClassLoader.getSystemResourceAsStream(classResource));   

        java.io.InputStream is = getClass().getClassLoader()
                .getSystemResourceAsStream(SYSTEM_RESOURCE_PATH);
        assertNotNull(String.format("Failed to find resource: %s", SYSTEM_RESOURCE_PATH), is);
        
        assertTrue("System resource not found", is.available() > 0);
        
        assertNull(ClassLoader.getSystemResourceAsStream("NotFoundResource"));
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSystemResources",
        args = {java.lang.String.class}
    )
    @AndroidOnly("The RI doesn't have a classes.dex as resource in the "
            + "core-tests.jar. Also on Android this file is the only one "
            + "that is sure to exist.")
    */
    public void test_getSystemResources() {
        
        String textResource = SYSTEM_RESOURCE_PATH;
        
        try {
            Enumeration<URL> urls = ClassLoader.getSystemResources(textResource);
            assertNotNull(urls);
            assertTrue(urls.nextElement().getPath().endsWith(textResource));
            while (urls.hasMoreElements()) {
                assertNotNull(urls.nextElement());
            }
            try {
                urls.nextElement();
                fail("NoSuchElementException was not thrown.");
            } catch(NoSuchElementException nse) {
                //expected
            }
        } catch(IOException ioe) {
            fail("IOException was thrown.");
        }
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPackage",
        args = {java.lang.String.class}
    )
    @KnownFailure("PackageClassLoader.getPackage returns null.")
    */
    public void test_getPackageLjava_lang_String() {
        PackageClassLoader pcl = new PackageClassLoader(
                getClass().getClassLoader());
        
        String [] packageProperties = { "test.package", "title", "1.0", 
                "Vendor", "Title", "1.1", "implementation vendor"};
        
        URL url = null;
        try {
            url = new URL("file:");
        } catch (MalformedURLException e) {
            fail("MalformedURLException was thrown.");
        }
        pcl.definePackage(packageProperties[0], 
                          packageProperties[1], 
                          packageProperties[2], 
                          packageProperties[3], 
                          packageProperties[4],
                          packageProperties[5],
                          packageProperties[6],
                          url);
        
       assertNotNull(pcl.getPackage(packageProperties[0])); 
       
       assertEquals("should define current package", getClass().getPackage(), 
               pcl.getPackage(getClass().getPackage().getName()));
       
       assertNull(pcl.getPackage("not.found.package"));
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPackages",
        args = {}
    )
    @KnownFailure("The package canot be found. Seems like the cache is not " +
            "shared between the class loaders. But this test seems to " +
            "expect exactly that. this tests works on the RI.")
    */
    public void test_getPackages() {
        
        PackageClassLoader pcl = new PackageClassLoader(
                getClass().getClassLoader());
        
        String [] packageProperties = { "test.package", "title", "1.0", 
                "Vendor", "Title", "1.1", "implementation vendor"};
        
        URL url = null;
        try {
            url = new URL("file:");
        } catch (MalformedURLException e) {
            fail("MalformedURLException was thrown.");
        }
        pcl.definePackage(packageProperties[0], 
                          packageProperties[1], 
                          packageProperties[2], 
                          packageProperties[3], 
                          packageProperties[4],
                          packageProperties[5],
                          packageProperties[6],
                          url);
        
        Package [] packages = pcl.getPackages();
        assertTrue(packages.length != 0);
        
        pcl = new PackageClassLoader(getClass().getClassLoader());
        packages = pcl.getPackages();
        assertNotNull(packages);
        
        boolean isThisFound = false;
        for(Package p:packages) {
            if(p.equals(getClass().getPackage())) {
                isThisFound = true;
            }
        }
        assertTrue(isThisFound);
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getParent",
        args = {}
    )
    */
    public void test_getParent() {
        PublicClassLoader pcl = new PublicClassLoader();
        assertNotNull(pcl.getParent());
        ClassLoader cl = getClass().getClassLoader().getParent();
        assertNotNull(cl); 
        
        SecurityManager sm = new SecurityManager() {
            
            final String perName = "getClassLoader";

            public void checkPermission(Permission perm) {
                if (perm.getName().equals(perName)) {
                    throw new SecurityException();
                }
            }
        };
        
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            getClass().getClassLoader().getParent();
            fail("Should throw SecurityException");
        } catch (SecurityException e) {
            // expected
        } finally {
            System.setSecurityManager(oldSm);
        }
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getResources",
        args = {java.lang.String.class}
    )
    */
    public void test_getResourcesLjava_lang_String() {
        Enumeration<java.net.URL> urls = null;
        FileInputStream fis = null;
        try {
            urls = getClass().getClassLoader().getResources("HelloWorld.txt");
            URL url = urls.nextElement();
            fis = new FileInputStream(url.getFile());
            byte [] array = new byte[13];
            fis.read(array);
            assertEquals("Hello, World.", new String(array));
        } catch (IOException e) {

        } finally {
            try {
                fis.close();
            } catch(Exception e) {}
        }
        
        assertNull(getClass().getClassLoader()
                .getResource("not.found.resource")); 

    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "definePackage",
        args = {java.lang.String.class, java.lang.String.class, 
                java.lang.String.class, java.lang.String.class, 
                java.lang.String.class, java.lang.String.class, 
                java.lang.String.class, java.net.URL.class }
    )
    */
    public void test_definePackage() {
        
        PackageClassLoader pcl = new PackageClassLoader(
                getClass().getClassLoader());
        
        String [] packageProperties = { "test.package", "title", "1.0", 
                "Vendor", "Title", "1.1", "implementation vendor"};
        
        URL url = null;
        try {
            url = new URL("file:");
        } catch (MalformedURLException e) {
            fail("MalformedURLException was thrown.");
        }
        pcl.definePackage(packageProperties[0], 
                          packageProperties[1], 
                          packageProperties[2], 
                          packageProperties[3], 
                          packageProperties[4],
                          packageProperties[5],
                          packageProperties[6],
                          url);
        
       Package pack = pcl.getPackage(packageProperties[0]);
       assertEquals(packageProperties[1], pack.getSpecificationTitle()); 
       assertEquals(packageProperties[2], pack.getSpecificationVersion()); 
       assertEquals(packageProperties[3], pack.getSpecificationVendor()); 
       assertEquals(packageProperties[4], pack.getImplementationTitle());
       assertEquals(packageProperties[5], pack.getImplementationVersion());  
       assertEquals(packageProperties[6], pack.getImplementationVendor()); 
       assertTrue(pack.isSealed(url));
       assertTrue(pack.isSealed());
        
       try {
           pcl.definePackage(packageProperties[0], 
                   packageProperties[1], 
                   packageProperties[2], 
                   packageProperties[3], 
                   packageProperties[4],
                   packageProperties[5],
                   packageProperties[6],
                   null);           
           fail("IllegalArgumentException was not thrown.");
       } catch(IllegalArgumentException  iae) {
           //expected
       }
       
       pcl.definePackage("test.package.test", null, null, null, null, 
               null, null, null);
       pack = pcl.getPackage("test.package.test");
       assertNull(pack.getSpecificationTitle()); 
       assertNull(pack.getSpecificationVersion()); 
       assertNull(pack.getSpecificationVendor()); 
       assertNull(pack.getImplementationTitle());
       assertNull(pack.getImplementationVersion());  
       assertNull(pack.getImplementationVendor());  
       assertFalse(pack.isSealed());
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "findClass",
        args = {java.lang.String.class}
    )
    @AndroidOnly("findClass method throws ClassNotFoundException exception.")
    */
    public void test_findClass(){
        
        try {
            PackageClassLoader pcl = new PackageClassLoader(
                    getClass().getClassLoader());
            pcl.findClass(getClass().getPackage().getName() + ".A");
            fail("ClassNotFoundException was not thrown.");
        } catch(ClassNotFoundException cnfe) {
            //expected
        } 
        
       try {
           PackageClassLoader pcl = new PackageClassLoader(
                   getClass().getClassLoader());
           pcl.findClass("TestClass");
           fail("ClassNotFoundException was not thrown.");
       } catch(ClassNotFoundException cnfe) {
           //expected
       }
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "findLibrary",
        args = {java.lang.String.class}
    )
    @AndroidOnly("findLibrary method is not supported, it returns null.")
    */
    public void test_findLibrary() {
        PackageClassLoader pcl = new PackageClassLoader(
                getClass().getClassLoader());
        assertNull(pcl.findLibrary("libjvm.so"));
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "findResource",
        args = {java.lang.String.class}
    )
    @AndroidOnly("findResource method is not supported, it returns null.")    
    */
    public void test_findResourceLjava_lang_String() {
        assertNull(new PackageClassLoader(
                getClass().getClassLoader()).findResource("hyts_Foo.c"));
    }

    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "findResources",
        args = {java.lang.String.class}
    )
    @AndroidOnly("findResources method is not supported, it returns " +
            "empty Enumeration.")      
    */
    public void test_findResourcesLjava_lang_String() throws IOException {
        assertFalse(new PackageClassLoader(
                getClass().getClassLoader()).findResources("hyts_Foo.c").
                hasMoreElements());
    }
    
    /*
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "findSystemClass",
        args = {java.lang.String.class}
    )    
    */
    public void test_findSystemClass() {
        PackageClassLoader pcl = new PackageClassLoader(
                getClass().getClassLoader());
        
        Class [] classes = { String.class, Integer.class, Object.class,
                Object[].class };
        
        for(Class clazz:classes) {
            try {
                String className = clazz.getName();
                assertEquals(clazz, pcl.findSystemClazz(className));
            } catch(ClassNotFoundException cnfe) {
                fail("ClassNotFoundException was thrown: " + cnfe.getMessage());
            }
        }
        try {
            pcl.findSystemClazz("unknownClass");
            fail("ClassNotFoundException was not thrown.");
        } catch(ClassNotFoundException cnfe) {
            //expected
        }
    }
  
    /*
   @TestTargetNew(
       level = TestLevel.COMPLETE,
       notes = "",
       method = "findLoadedClass",
       args = {java.lang.String.class }
    )
    */
    public void test_findLoadedClass() {
       PackageClassLoader pcl = new PackageClassLoader(
               getClass().getClassLoader());
       
       Class [] classes = { A.class, PublicTestClass.class,
               TestAnnotation.class, TestClass1.class };
       
       for(Class clazz:classes) {
           String className = clazz.getName();
           assertNull(pcl.findLoadedClazz(className));
       }

       assertNull(pcl.findLoadedClazz("unknownClass"));
    }
    
    /*
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "setClassAssertionStatus is not supported.",
            method = "setClassAssertionStatus",
            args = {java.lang.String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "setDefaultAssertionStatus is not supported.",
            method = "setDefaultAssertionStatus",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "setPackageAssertionStatus is not supported.",
            method = "setPackageAssertionStatus",
            args = {java.lang.String.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "resolveClass is not supported.",
            method = "resolveClass",
            args = {java.lang.Class.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "setSigners is not supported.",
            method = "setSigners",
            args = {java.lang.Class.class, java.lang.Object[].class}
        )
    })
   */
    public void test_notSupported() {
        getClass().getClassLoader().setClassAssertionStatus(getName(), true);
        getClass().getClassLoader().setDefaultAssertionStatus(true);
        getClass().getClassLoader().setPackageAssertionStatus(
                getClass().getPackage().getName(), true);
    }
}

class DynamicPolicy extends Policy {

    public PermissionCollection pc;

    @Override
    public PermissionCollection getPermissions(ProtectionDomain pd) {
        return pc;
    }

    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        return pc;
    }

    @Override
    public void refresh() {
    }
}

class A {
}

class Ldr extends ClassLoader {

    /*
     * These bytes are the content of the file
     * /org/apache/harmony/luni/tests/java/lang/A.class
     */
    byte[] classBytes = new byte[] { -54, -2, -70, -66, 0, 0, 0, 49, 0, 16, 7,
            0, 2, 1, 0, 41, 111, 114, 103, 47, 97, 112, 97, 99, 104, 101, 47,
            104, 97, 114, 109, 111, 110, 121, 47, 108, 117, 110, 105, 47, 116,
            101, 115, 116, 115, 47, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47,
            65, 7, 0, 4, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47,
            79, 98, 106, 101, 99, 116, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1,
            0, 3, 40, 41, 86, 1, 0, 4, 67, 111, 100, 101, 10, 0, 3, 0, 9, 12, 0,
            5, 0, 6, 1, 0, 15, 76, 105, 110, 101, 78, 117, 109, 98, 101, 114,
            84, 97, 98, 108, 101, 1, 0, 18, 76, 111, 99, 97, 108, 86, 97, 114,
            105, 97, 98, 108, 101, 84, 97, 98, 108, 101, 1, 0, 4, 116, 104, 105,
            115, 1, 0, 43, 76, 111, 114, 103, 47, 97, 112, 97, 99, 104, 101, 47,
            104, 97, 114, 109, 111, 110, 121, 47, 108, 117, 110, 105, 47, 116,
            101, 115, 116, 115, 47, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47,
            65, 59, 1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1,
            0, 20, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 84, 101,
            115, 116, 46, 106, 97, 118, 97, 0, 32, 0, 1, 0, 3, 0, 0, 0, 0, 0, 1,
            0, 0, 0, 5, 0, 6, 0, 1, 0, 7, 0, 0, 0, 47, 0, 1, 0, 1, 0, 0, 0, 5,
            42, -73, 0, 8, -79, 0, 0, 0, 2, 0, 10, 0, 0, 0, 6, 0, 1, 0, 0, 4,
            -128, 0, 11, 0, 0, 0, 12, 0, 1, 0, 0, 0, 5, 0, 12, 0, 13, 0, 0, 0,
            1, 0, 14, 0, 0, 0, 2, 0, 15 };
    
    public static final int TEST_CASE_DEFINE_0 = 0;
    public static final int TEST_CASE_DEFINE_1 = 1;
    public static final int TEST_CASE_DEFINE_2 = 2;
    public static final int TEST_CASE_DEFINE_3 = 3;
    
    @SuppressWarnings("deprecation")
    public Class<?> define(int len, int testCase) throws Exception {
        
        if(len < 0) len = classBytes.length;
        Class<?> clazz = null;
        String className = "org.apache.harmony.luni.tests.java.lang.A";
        switch(testCase) {
            case TEST_CASE_DEFINE_0:
                clazz = defineClass(className, classBytes, 0, len);
                break;
            case TEST_CASE_DEFINE_1:
                clazz = defineClass(classBytes, 0, len);                
                break;
            case TEST_CASE_DEFINE_2:
                clazz = defineClass(className, classBytes, 0, len, 
                        getClass().getProtectionDomain());
                break;
            case TEST_CASE_DEFINE_3:
                ByteBuffer bb = ByteBuffer.wrap(classBytes);
                clazz = defineClass(className, 
                        bb, getClass().getProtectionDomain());                
                break;
        }
        return clazz;
    }
   
    public Class<?> define(int testCase) throws Exception {
        return  define(-1, testCase);
    }   

}

class PackageClassLoader extends ClassLoader { 
    public PackageClassLoader() {
        super();
    }

    public PackageClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Package definePackage(String name,
            String specTitle,
            String specVersion,
            String specVendor,
            String implTitle,
            String implVersion,
            String implVendor,
            URL sealBase)
     throws IllegalArgumentException {
        return super.definePackage(name, specTitle, specVersion, 
                specVendor, implTitle, implVersion, implVendor, sealBase);
    }
    
    public Package getPackage(String name) {
        return super.getPackage(name);
    }
    
    public Package[] getPackages() {
        return super.getPackages();
    }
    
    public Class<?> findClass(String name)
        throws ClassNotFoundException {
       return super.findClass(name); 
    }
    
    public String findLibrary(String libname) {
        return super.findLibrary(libname);
    }
    
    public Class<?> loadClass(String name, boolean resolve) 
                                            throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }
    
    public URL findResource(String name) {
        return super.findResource(name);
    }
    
    public Enumeration<URL> findResources(String resName) 
                                            throws IOException {
        return super.findResources(resName);
    }
    
    public Class<?> findSystemClazz(String name) throws ClassNotFoundException {
        return super.findSystemClass(name);
    }

    public Class<?> findLoadedClazz(String name) {
        return super.findLoadedClass(name);
    }
} 
