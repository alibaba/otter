/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.shared.common.utils.sizeof;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.JarFile;

// ----------------------------------------------------------------------------
/**
 * This non-instantiable class presents an API for object sizing as described in the <a
 * href="http://www.javaworld.com/javaqa/2003-12/02-qa-1226-sizeof_p.html">article</a>. See individual methods for
 * details.
 * <P>
 * This implementation is J2SE 1.4+ only. You would need to code your own identity hashmap to port this to earlier Java
 * versions.
 * <P>
 * Security: this implementation uses AccessController.doPrivileged() so it could be granted privileges to access
 * non-public class fields separately from your main application code. The minimum set of permissions necessary for this
 * class to function correctly follows:
 * 
 * <pre>
 *    permission java.lang.RuntimePermission &quot;accessDeclaredMembers&quot;;
 *    permission java.lang.reflect.ReflectPermission &quot;suppressAccessChecks&quot;;
 * </pre>
 * 
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov</a>, 2003
 */
public abstract class ObjectProfiler {

    // public: ................................................................

    // the following constants are physical sizes (in bytes) and are JVM-dependent:
    // [the current values are Ok for most 32-bit JVMs]

    public static final int OBJECT_SHELL_SIZE  = 8; // java.lang.Object shell
    // size in bytes
    public static final int OBJREF_SIZE        = 4;
    public static final int LONG_FIELD_SIZE    = 8;
    public static final int INT_FIELD_SIZE     = 4;
    public static final int SHORT_FIELD_SIZE   = 2;
    public static final int CHAR_FIELD_SIZE    = 2;
    public static final int BYTE_FIELD_SIZE    = 1;
    public static final int BOOLEAN_FIELD_SIZE = 1;
    public static final int DOUBLE_FIELD_SIZE  = 8;
    public static final int FLOAT_FIELD_SIZE   = 4;

    /**
     * Estimates the full size of the object graph rooted at 'obj'. Duplicate data instances are correctly accounted
     * for. The implementation is not recursive.
     * 
     * @param obj input object instance to be measured
     * @return 'obj' size [0 if 'obj' is null']
     */
    public static long sizeof(final Object obj) {
        if (null == obj || isSharedFlyweight(obj)) {
            return 0;
        }

        final IdentityHashMap visited = new IdentityHashMap(80000);

        try {
            return computeSizeof(obj, visited, CLASS_METADATA_CACHE);
        } catch (RuntimeException re) {
            // re.printStackTrace();//DEBUG
            return -1;
        } catch (NoClassDefFoundError ncdfe) {
            // BUG: throws "java.lang.NoClassDefFoundError: org.eclipse.core.resources.IWorkspaceRoot" when run in WSAD
            // 5
            // see
            // http://www.javaworld.com/javaforums/showflat.php?Cat=&Board=958763&Number=15235&page=0&view=collapsed&sb=5&o=
            // System.err.println(ncdfe);//DEBUG
            return -1;
        }
    }

    /**
     * Estimates the full size of the object graph rooted at 'obj' by pre-populating the "visited" set with the object
     * graph rooted at 'base'. The net effect is to compute the size of 'obj' by summing over all instance data
     * contained in 'obj' but not in 'base'.
     * 
     * @param base graph boundary [may not be null]
     * @param obj input object instance to be measured
     * @return 'obj' size [0 if 'obj' is null']
     */
    public static long sizedelta(final Object base, final Object obj) {
        if (null == obj || isSharedFlyweight(obj)) {
            return 0;
        }
        if (null == base) {
            throw new IllegalArgumentException("null input: base");
        }

        final IdentityHashMap visited = new IdentityHashMap(40000);

        try {
            computeSizeof(base, visited, CLASS_METADATA_CACHE);
            return visited.containsKey(obj) ? 0 : computeSizeof(obj, visited, CLASS_METADATA_CACHE);
        } catch (RuntimeException re) {
            // re.printStackTrace();//DEBUG
            return -1;
        } catch (NoClassDefFoundError ncdfe) {
            // BUG: throws "java.lang.NoClassDefFoundError: org.eclipse.core.resources.IWorkspaceRoot" when run in WSAD
            // 5
            // see
            // http://www.javaworld.com/javaforums/showflat.php?Cat=&Board=958763&Number=15235&page=0&view=collapsed&sb=5&o=
            // System.err.println(ncdfe);//DEBUG
            return -1;
        }
    }

    // protected: .............................................................

    // package: ...............................................................

    // private: ...............................................................

    /*
     * Internal class used to cache class metadata information.
     */
    private static final class ClassMetadata {

        ClassMetadata(final int primitiveFieldCount, final int shellSize, final Field[] refFields){
            m_primitiveFieldCount = primitiveFieldCount;
            m_shellSize = shellSize;
            m_refFields = refFields;
        }

        // all fields are inclusive of superclasses:

        final int     m_primitiveFieldCount;

        final int     m_shellSize;          // class shell size

        final Field[] m_refFields;          // cached non-static fields (made accessible)

    } // end of nested class

    private static final class ClassAccessPrivilegedAction implements PrivilegedExceptionAction {

        /** {@inheritDoc} */
        public Object run() throws Exception {
            return m_cls.getDeclaredFields();
        }

        void setContext(final Class cls) {
            m_cls = cls;
        }

        private Class m_cls;

    } // end of nested class

    private static final class FieldAccessPrivilegedAction implements PrivilegedExceptionAction {

        /** {@inheritDoc} */
        public Object run() throws Exception {
            m_field.setAccessible(true);
            return null;
        }

        void setContext(final Field field) {
            m_field = field;
        }

        private Field m_field;

    } // end of nested class

    private ObjectProfiler(){
    } // this class is not extendible

    /*
     * The main worker method for sizeof() and sizedelta().
     */
    private static long computeSizeof(Object obj, final IdentityHashMap visited,
                                      final Map /* <Class,ClassMetadata> */metadataMap) {
        // this uses depth-first traversal; the exact graph traversal algorithm
        // does not matter for computing the total size and this method could be
        // easily adjusted to do breadth-first instead (addLast() instead of
        // addFirst()),
        // however, dfs/bfs require max queue length to be the length of the
        // longest
        // graph path/width of traversal front correspondingly, so I expect
        // dfs to use fewer resources than bfs for most Java objects;

        if (null == obj || isSharedFlyweight(obj)) {
            return 0;
        }

        final LinkedList queue = new LinkedList();

        visited.put(obj, obj);
        queue.add(obj);

        long result = 0;

        final ClassAccessPrivilegedAction caAction = new ClassAccessPrivilegedAction();
        final FieldAccessPrivilegedAction faAction = new FieldAccessPrivilegedAction();

        while (!queue.isEmpty()) {
            obj = queue.removeFirst();
            final Class objClass = obj.getClass();

            int skippedBytes = skipClassDueToSunJVMBug(objClass);
            if (skippedBytes > 0) {
                result += skippedBytes; // can't do better than that
                continue;
            }

            if (objClass.isArray()) {
                final int arrayLength = Array.getLength(obj);
                final Class componentType = objClass.getComponentType();

                result += sizeofArrayShell(arrayLength, componentType);

                if (!componentType.isPrimitive()) {
                    // traverse each array slot:
                    for (int i = 0; i < arrayLength; ++i) {
                        final Object ref = Array.get(obj, i);

                        if ((ref != null) && !visited.containsKey(ref)) {
                            visited.put(ref, ref);
                            queue.addFirst(ref);
                        }
                    }
                }
            } else { // the object is of a non-array type
                final ClassMetadata metadata = getClassMetadata(objClass, metadataMap, caAction, faAction);
                final Field[] fields = metadata.m_refFields;

                result += metadata.m_shellSize;

                // traverse all non-null ref fields:
                for (int f = 0, fLimit = fields.length; f < fLimit; ++f) {
                    final Field field = fields[f];

                    final Object ref;
                    try { // to get the field value:
                        ref = field.get(obj);
                    } catch (Exception e) {
                        throw new RuntimeException("cannot get field [" + field.getName() + "] of class ["
                                                   + field.getDeclaringClass().getName() + "]: " + e.toString());
                    }

                    if ((ref != null) && !visited.containsKey(ref)) {
                        visited.put(ref, ref);
                        queue.addFirst(ref);
                    }
                }
            }
        }

        return result;
    }

    /*
     * A helper method for manipulating a class metadata cache.
     */
    private static ClassMetadata getClassMetadata(final Class cls, final Map /* <Class,ClassMetadata> */metadataMap,
                                                  final ClassAccessPrivilegedAction caAction,
                                                  final FieldAccessPrivilegedAction faAction) {
        if (null == cls) {
            return null;
        }

        ClassMetadata result;
        synchronized (metadataMap) {
            result = (ClassMetadata) metadataMap.get(cls);
        }
        if (result != null) {
            return result;
        }

        int primitiveFieldCount = 0;
        int shellSize = OBJECT_SHELL_SIZE; // java.lang.Object shell
        final List /* Field */refFields = new LinkedList();

        final Field[] declaredFields;
        try {
            caAction.setContext(cls);
            declaredFields = (Field[]) AccessController.doPrivileged(caAction);
        } catch (PrivilegedActionException pae) {
            throw new RuntimeException("could not access declared fields of class " + cls.getName() + ": "
                                       + pae.getException());
        }

        for (int f = 0; f < declaredFields.length; ++f) {
            final Field field = declaredFields[f];
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            /*
             * Can't do that: HashMap data is transient, for example... if (Modifier.isTransient(field.getModifiers()))
             * { shellSize += OBJREF_SIZE; continue; }
             */

            final Class fieldType = field.getType();
            if (fieldType.isPrimitive()) {
                // memory alignment ignored:
                shellSize += sizeofPrimitiveType(fieldType);
                ++primitiveFieldCount;
            } else {
                // prepare for graph traversal later:
                if (!field.isAccessible()) {
                    try {
                        faAction.setContext(field);
                        AccessController.doPrivileged(faAction);
                    } catch (PrivilegedActionException pae) {
                        throw new RuntimeException("could not make field " + field + " accessible: "
                                                   + pae.getException());
                    }
                }

                // memory alignment ignored:
                shellSize += OBJREF_SIZE;
                refFields.add(field);
            }
        }

        // recurse into superclass:
        final ClassMetadata superMetadata = getClassMetadata(cls.getSuperclass(), metadataMap, caAction, faAction);
        if (superMetadata != null) {
            primitiveFieldCount += superMetadata.m_primitiveFieldCount;
            shellSize += superMetadata.m_shellSize - OBJECT_SHELL_SIZE;
            refFields.addAll(Arrays.asList(superMetadata.m_refFields));
        }

        final Field[] _refFields = new Field[refFields.size()];
        refFields.toArray(_refFields);

        result = new ClassMetadata(primitiveFieldCount, shellSize, _refFields);
        synchronized (metadataMap) {
            metadataMap.put(cls, result);
        }

        return result;
    }

    /*
     * Computes the "shallow" size of an array instance.
     */
    private static int sizeofArrayShell(final int length, final Class componentType) {
        // this ignores memory alignment issues by design:

        final int slotSize = componentType.isPrimitive() ? sizeofPrimitiveType(componentType) : OBJREF_SIZE;

        return OBJECT_SHELL_SIZE + INT_FIELD_SIZE + OBJREF_SIZE + length * slotSize;
    }

    /*
     * Returns the JVM-specific size of a primitive type.
     */
    private static int sizeofPrimitiveType(final Class type) {
        if (type == int.class) {
            return INT_FIELD_SIZE;
        } else if (type == long.class) {
            return LONG_FIELD_SIZE;
        } else if (type == short.class) {
            return SHORT_FIELD_SIZE;
        } else if (type == byte.class) {
            return BYTE_FIELD_SIZE;
        } else if (type == boolean.class) {
            return BOOLEAN_FIELD_SIZE;
        } else if (type == char.class) {
            return CHAR_FIELD_SIZE;
        } else if (type == double.class) {
            return DOUBLE_FIELD_SIZE;
        } else if (type == float.class) {
            return FLOAT_FIELD_SIZE;
        } else {
            throw new IllegalArgumentException("not primitive: " + type);
        }
    }

    // class metadata cache:
    private static final Map CLASS_METADATA_CACHE = new WeakHashMap(101);

    static final Class[]     sunProblematicClasses;
    static final Map        /* <Class, Integer> */sunProblematicClassesSizes;

    static {
        Map classesSizes = new HashMap();
        classesSizes.put("java.lang.Class", Integers.valueOf(OBJECT_SHELL_SIZE));// not really a pb, but since this is
        // shared, so there's no point in going
        // further
        // 1.3+
        classesSizes.put("java.lang.Throwable", Integers.valueOf(OBJECT_SHELL_SIZE + 4 * OBJREF_SIZE));
        // 1.4+
        classesSizes.put("sun.reflect.UnsafeStaticFieldAccessorImpl", Integers.valueOf(OBJECT_SHELL_SIZE));// unknown
        classesSizes.put("sun.reflect.UnsafeStaticBooleanFieldAccessorImpl", Integers.valueOf(OBJECT_SHELL_SIZE));// unknown
        classesSizes.put("sun.reflect.UnsafeStaticByteFieldAccessorImpl", Integers.valueOf(OBJECT_SHELL_SIZE));// unknown
        classesSizes.put("sun.reflect.UnsafeStaticShortFieldAccessorImpl", Integers.valueOf(OBJECT_SHELL_SIZE));// unknown
        classesSizes.put("sun.reflect.UnsafeStaticIntegerFieldAccessorImpl", Integers.valueOf(OBJECT_SHELL_SIZE));// unknown
        classesSizes.put("sun.reflect.UnsafeStaticLongFieldAccessorImpl", Integers.valueOf(OBJECT_SHELL_SIZE));// unknown
        classesSizes.put("sun.reflect.UnsafeStaticCharacterFieldAccessorImpl", Integers.valueOf(OBJECT_SHELL_SIZE));// unknown
        classesSizes.put("sun.reflect.UnsafeStaticFloatFieldAccessorImpl", Integers.valueOf(OBJECT_SHELL_SIZE));// unknown
        classesSizes.put("sun.reflect.UnsafeStaticDoubleFieldAccessorImpl", Integers.valueOf(OBJECT_SHELL_SIZE));// unknown
        classesSizes.put("sun.reflect.UnsafeStaticObjectFieldAccessorImpl", Integers.valueOf(OBJECT_SHELL_SIZE));// unknown
        // 1.5+
        classesSizes.put("java.lang.Enum", Integers.valueOf(OBJECT_SHELL_SIZE));// not really a pb, but since this is
        // shared, so there's no point in going
        // further
        classesSizes.put("sun.reflect.ConstantPool", Integers.valueOf(OBJECT_SHELL_SIZE + OBJECT_SHELL_SIZE));
        sunProblematicClassesSizes = Collections.unmodifiableMap(classesSizes);

        List classes = new ArrayList(sunProblematicClassesSizes.size());
        Iterator iter = sunProblematicClassesSizes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String className = (String) entry.getKey();
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException cnfe) {
                // } catch (ExceptionInInitializerError eiie) {
                // } catch (NoClassDefFoundError ncdfe) {
                // } catch (UnsatisfiedLinkError le) {
            } catch (LinkageError le) {
                // BEA JRockit 1.4 also throws NoClassDefFoundError and UnsatisfiedLinkError
            }
        }
        sunProblematicClasses = (Class[]) classes.toArray(new Class[0]);
    }

    /**
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5012949 Implementation note: we can compare classes with ==
     * since they will always be loaded from the same ClassLoader (they are "low" in the hierarchy)
     */
    private static int skipClassDueToSunJVMBug(Class clazz) {
        for (int i = 0; i < sunProblematicClasses.length; ++i) {
            Class sunPbClass = sunProblematicClasses[i];
            if (clazz == sunPbClass) {
                return ((Integer) sunProblematicClassesSizes.get(clazz.getName())).intValue();
            }
        }
        return 0;
    }

    /*
     * Very very incomplete, but better than nothing...
     */
    // See http://download.oracle.com/javase/7/docs/api/constant-values.html for JDK's String constants
    private static boolean isSharedFlyweight(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj == Boolean.TRUE || obj == Boolean.FALSE) {
            return true;
        }
        if (/* obj == Locale.ROOT || *//* Java 6 */
        obj == Locale.ENGLISH || obj == Locale.FRENCH || obj == Locale.GERMAN || obj == Locale.ITALIAN
                || obj == Locale.JAPANESE || obj == Locale.KOREAN || obj == Locale.CHINESE
                || obj == Locale.SIMPLIFIED_CHINESE || obj == Locale.TRADITIONAL_CHINESE || obj == Locale.FRANCE
                || obj == Locale.GERMANY || obj == Locale.ITALY || obj == Locale.JAPAN || obj == Locale.KOREA
                || obj == Locale.CHINA || obj == Locale.PRC || obj == Locale.TAIWAN || obj == Locale.UK
                || obj == Locale.US || obj == Locale.CANADA || obj == Locale.CANADA_FRENCH) {
            return true;
        }
        if (obj == Collections.EMPTY_SET || obj == Collections.EMPTY_LIST || obj == Collections.EMPTY_MAP) {
            return true;
        }
        if (obj == BigInteger.ZERO || obj == BigInteger.ONE) {
            return true;
        }
        if (obj == System.in || obj == System.out || obj == System.err) {
            return true;
        }
        if (obj == String.CASE_INSENSITIVE_ORDER) {
            return true;
        }
        if (obj == JarFile.MANIFEST_NAME) {
            return true;
        }
        return false;
    }
}

final class Integers {

    private Integers(){
    }

    private static final int     cache_low  = -128;
    private static final int     cache_high = 127;
    private static final Integer cache[]    = new Integer[(cache_high - cache_low) + 1];

    static {
        for (int i = 0; i < cache.length; ++i) {
            cache[i] = new Integer(i + cache_low);
        }
    }

    /**
     * Returns a <tt>Integer</tt> instance representing the specified <tt>int</tt> value. If a new <tt>Integer</tt>
     * instance is not required, this method should generally be used in preference to the constructor
     * {@link #Integer(int)}, as this method is likely to yield significantly better space and time performance by
     * caching frequently requested values.
     * 
     * @param i an <code>int</code> value.
     * @return a <tt>Integer</tt> instance representing <tt>i</tt>.
     * @since 1.5
     */
    public static Integer valueOf(int i) {
        if (i >= cache_low && i <= cache_high) { // must cache
            return cache[i - cache_low];
        } else {
            return new Integer(i);
        }
    }
}
