/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.scr.impl.helper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.osgi.framework.Bundle;

import junit.framework.TestCase;

public class AnnotationTest extends TestCase
{
    
    public void testNameFixup() throws Exception
    {
        assertEquals("foo", Annotations.fixup("foo"));
        assertEquals("foo", Annotations.fixup("$foo"));
        assertEquals("foo", Annotations.fixup("foo$"));
        assertEquals("$foo", Annotations.fixup("$$foo"));
        assertEquals("foobar", Annotations.fixup("foo$bar"));
        assertEquals("foo$bar", Annotations.fixup("foo$$bar"));
        assertEquals("foo.", Annotations.fixup("foo_"));
        assertEquals("foo_", Annotations.fixup("foo__"));
        assertEquals(".foo", Annotations.fixup("_foo"));
        assertEquals("_foo", Annotations.fixup("__foo"));
        assertEquals("foo.bar", Annotations.fixup("foo_bar"));
        assertEquals("foo_bar", Annotations.fixup("foo__bar"));
        assertEquals("foo$", Annotations.fixup("foo$$$"));
        assertEquals("foo_.", Annotations.fixup("foo___"));
        assertEquals("foo..bar", Annotations.fixup("foo$_$_bar"));
    }

    public enum E1 {a, b, c}
    public @interface A1 {
        boolean bool();
        byte byt();
        Class<?> clas();
        E1 e1();
        double doubl();
        float floa();
        int integer();
        long lon();
        short shor();
        String string();
    }
    
    private Bundle mockBundle() throws ClassNotFoundException
    {
        Bundle b = EasyMock.createMock(Bundle.class);
        EasyMock.expect(b.loadClass(String.class.getName())).andReturn((Class) String.class).anyTimes();
        EasyMock.expect(b.loadClass(Integer.class.getName())).andReturn((Class) Integer.class).anyTimes();
        EasyMock.replay(b);
        return b;
    }
    
    public void testA1() throws Exception
    {
        Map<String, Object> values = allValues();
        
        Object o = Annotations.toObject( A1.class, values, mockBundle(), false);
        assertTrue("expected an A1", o instanceof A1);
        
        A1 a = (A1) o;
        checkA1(a);
    }

    private void checkA1(A1 a)
    {
        assertEquals(true, a.bool());
        assertEquals((byte)12, a.byt());
        assertEquals(String.class, a.clas());
        assertEquals(E1.a, a.e1());
        assertEquals(3.14d, a.doubl());
        assertEquals(500f, a.floa());
        assertEquals(3, a.integer());
        assertEquals(12345678l,  a.lon());
        assertEquals((short)3, a.shor());
        assertEquals("3", a.string());
    }

    public void testA1FromArray() throws Exception
    {
        Map<String, Object> values = arrayValues();
        
        Object o = Annotations.toObject( A1.class, values, mockBundle(), false);
        assertTrue("expected an A1", o instanceof A1);
        
        A1 a = (A1) o;
        assertEquals(true, a.bool());
        assertEquals((byte)12, a.byt());
        assertEquals(String.class, a.clas());
        assertEquals(E1.a, a.e1());
        assertEquals(3.14d, a.doubl());
        assertEquals(500f, a.floa());
        assertEquals(3, a.integer());
        assertEquals(12345678l,  a.lon());
        assertEquals((short)3, a.shor());
        assertEquals(null, a.string());
    }

    private Map<String, Object> allValues()
    {
        Map<String, Object> values = new HashMap();
        values.put("bool", "true");
        values.put("byt", 12l);
        values.put("clas", String.class.getName());
        values.put("e1", E1.a.toString());
        values.put("doubl", "3.14");
        values.put("floa", 500l);
        values.put("integer", 3.0d);
        values.put("lon", "12345678");
        values.put("shor", 3l);
        values.put("string", 3);
        return values;
    }

    public void testA1NoValues() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        
        Object o = Annotations.toObject( A1.class, values, mockBundle(), false);
        assertTrue("expected an A1", o instanceof A1);
        
        A1 a = (A1) o;
        assertEquals(false, a.bool());
        assertEquals((byte)0, a.byt());
        assertEquals(null, a.clas());
        assertEquals(null, a.e1());
        assertEquals(0d, a.doubl());
        assertEquals(0f, a.floa());
        assertEquals(0, a.integer());
        assertEquals(0l,  a.lon());
        assertEquals((short)0, a.shor());
        assertEquals(null, a.string());
    }

    public @interface A2 {
        boolean bool() default true;
        byte byt() default 5;
        Class<?> clas() default Integer.class;
        E1 e1() default E1.b;
        double doubl() default -2;
        float floa() default -4;
        int integer() default -5;
        long lon() default Long.MIN_VALUE;
        short shor() default -8;
        String string() default "default";
    }
    
    public void testA2AllValues() throws Exception
    {
        Map<String, Object> values = allValues();
        
        Object o = Annotations.toObject( A2.class, values, mockBundle(), false);
        assertTrue("expected an A2", o instanceof A2);
        
        A2 a = (A2) o;
        assertEquals(true, a.bool());
        assertEquals((byte)12, a.byt());
        assertEquals(String.class, a.clas());
        assertEquals(E1.a, a.e1());
        assertEquals(3.14d, a.doubl());
        assertEquals(500f, a.floa());
        assertEquals(3, a.integer());
        assertEquals(12345678l,  a.lon());
        assertEquals((short)3, a.shor());
        assertEquals("3", a.string());
    }
    
    public @interface A1Arrays {
        boolean[] bool();
        byte[] byt();
        Class<?>[] clas();
        E1[] e1();
        double[] doubl();
        float[] floa();
        int[] integer();
        long[] lon();
        short[] shor();
        String[] string();
    }
    
    public void testA1ArraysNoValues() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        
        Object o = Annotations.toObject( A1Arrays.class, values, mockBundle(), false);
        assertTrue("expected an A1Arrays", o instanceof A1Arrays);
        
        A1Arrays a = (A1Arrays) o;
        assertEquals(null, a.bool());
        assertEquals(null, a.byt());
        assertEquals(null, a.clas());
        assertEquals(null, a.e1());
        assertEquals(null, a.doubl());
        assertEquals(null, a.floa());
        assertEquals(null, a.integer());
        assertEquals(null,  a.lon());
        assertEquals(null, a.shor());
        assertEquals(null, a.string());
    }

    public void testA1Array() throws Exception
    {
        Map<String, Object> values = allValues();
        
        Object o = Annotations.toObject( A1Arrays.class, values, mockBundle(), false);
        assertTrue("expected an A1Arrays", o instanceof A1Arrays);
        
        A1Arrays a = (A1Arrays) o;
        assertArrayEquals(new boolean[] {true}, a.bool());
        assertArrayEquals(new byte[] {(byte)12}, a.byt());
        assertArrayEquals(new Class<?>[] {String.class}, a.clas());
        assertArrayEquals(new E1[] {E1.a}, a.e1());
        assertArrayEquals(new double[] {3.14d}, a.doubl());
        assertArrayEquals(new float[] {500f}, a.floa());
        assertArrayEquals(new int[] {3}, a.integer());
        assertArrayEquals(new long[] {12345678l},  a.lon());
        assertArrayEquals(new short[] {(short)3}, a.shor());
        assertArrayEquals(new String[] {"3"}, a.string());
    }

    private void assertArrayEquals(Object a, Object b)
    {
        assertTrue(a.getClass().isArray());
        assertTrue(b.getClass().isArray());
        assertEquals("wrong length", Array.getLength(a), Array.getLength(b));
        assertEquals("wrong type", a.getClass().getComponentType(), b.getClass().getComponentType());
        for (int i = 0; i < Array.getLength(a); i++)
        {
            assertEquals("different value at " + i, Array.get(a, i), Array.get(b, i));
        }
        
    }

    private Map<String, Object> arrayValues()
    {
        Map<String, Object> values = new HashMap();
        values.put("bool", new boolean[] {true, false});
        values.put("byt", new byte[] {12, 3});
        values.put("clas", new String[] {String.class.getName(), Integer.class.getName()});
        values.put("e1", new String[] {E1.a.name(), E1.b.name()});
        values.put("doubl", new double[] {3.14, 2.78, 9});
        values.put("floa", new float[] {500, 37.44f});
        values.put("integer", new int[] {3, 6, 9});
        values.put("lon", new long[] {12345678l, -1});
        values.put("shor", new short[] {3, 88});
        values.put("string", new String[] {});
        return values;
    }
    
    public void testA1ArrayFromArray() throws Exception
    {
        Map<String, Object> values = arrayValues();
        
        doA1ArrayTest(values);
    }

    public void testA1ArrayFromCollection() throws Exception
    {
        Map<String, Object> values = arrayValues();
        Map<String, Object> collectionValues = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry: values.entrySet())
        {
            collectionValues.put(entry.getKey(), toList(entry.getValue()));
        }
        
        doA1ArrayTest(collectionValues);
    }

    private List<?> toList(Object value)
    {
        List result = new ArrayList();
        for (int i = 0; i < Array.getLength(value); i++)
        {
            result.add(Array.get(value, i));
        }
        return result;
    }

    private void doA1ArrayTest(Map<String, Object> values) throws ClassNotFoundException
    {
        Object o = Annotations.toObject( A1Arrays.class, values, mockBundle(), false);
        assertTrue("expected an A1Arrays", o instanceof A1Arrays);
        
        A1Arrays a = (A1Arrays) o;
        assertArrayEquals(new boolean[] {true, false}, a.bool());
        assertArrayEquals(new byte[] {12, 3}, a.byt());
        assertArrayEquals(new Class<?>[] {String.class, Integer.class}, a.clas());
        assertArrayEquals(new E1[] {E1.a, E1.b}, a.e1());
        assertArrayEquals(new double[] {3.14, 2.78, 9}, a.doubl());
        assertArrayEquals(new float[] {500f, 37.44f}, a.floa());
        assertArrayEquals(new int[] {3, 6, 9}, a.integer());
        assertArrayEquals(new long[] {12345678l, -1},  a.lon());
        assertArrayEquals(new short[] {(short)3, 88}, a.shor());
        assertArrayEquals(new String[] {}, a.string());
    }

    public @interface B1 {
        boolean bool();
        byte byt();
        Class<?> clas();
        E1 e1();
        double doubl();
        float floa();
        int integer();
        long lon();
        short shor();
        String string();
        A1 a1();
        A1[] a1array();
    }
    
    public void testB1() throws Exception
    {
        Map<String, Object> values = b1Values();
        
        Object o = Annotations.toObject( B1.class, values, mockBundle(), true);
        assertTrue("expected an B1 " + o, o instanceof B1);
        B1 b = (B1) o;
        checkB1(b);        
    }

    private void checkB1(B1 b)
    {
        checkA1(b.a1());
        assertEquals(3, b.a1array().length);
        checkA1(b.a1array()[0]);
        checkA1(b.a1array()[1]);
        checkA1(b.a1array()[2]);
    }

    private Map<String, Object> b1Values()
    {
        Map<String, Object> a1Values = allValues();
        Map<String, Object> values = allValues();
        nest(values, "a1", 0, a1Values);
        nest(values, "a1array", 0, a1Values);
        nest(values, "a1array", 1, a1Values);
        nest(values, "a1array", 2, a1Values);
        return values;
    }

    private void nest(Map<String, Object> values, String key, int i,
        Map<String, Object> a1Values)
    {
        for (Map.Entry<String, Object> entry: a1Values.entrySet())
        {
            values.put(key + "." + i + "." + entry.getKey(), entry.getValue());
        }
    }

    public @interface C1 {
        boolean bool();
        byte byt();
        Class<?> clas();
        E1 e1();
        double doubl();
        float floa();
        int integer();
        long lon();
        short shor();
        String string();
        B1 b1();
        B1[] b1array();
    }

    public void testC1() throws Exception
    {
        Map<String, Object> b1Values = b1Values();
        Map<String, Object> values = allValues();
        nest(values, "b1", 0, b1Values);
        nest(values, "b1array", 0, b1Values);
        nest(values, "b1array", 1, b1Values);
        nest(values, "b1array", 2, b1Values);
        
        Object o = Annotations.toObject( C1.class, values, mockBundle(), true);
        assertTrue("expected an B1 " + o, o instanceof C1);
        C1 c = (C1) o;
        checkB1(c.b1());  
        assertEquals(3, c.b1array().length);
        checkB1(c.b1array()[0]);
        checkB1(c.b1array()[1]);
        checkB1(c.b1array()[2]);
        
    }


}