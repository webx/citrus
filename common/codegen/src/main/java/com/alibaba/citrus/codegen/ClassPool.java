/*
 * Copyright 2010 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.alibaba.citrus.codegen;

import static com.alibaba.citrus.codegen.util.CodegenConstant.*;
import static com.alibaba.citrus.codegen.util.CodegenUtil.*;
import static com.alibaba.citrus.codegen.util.TypeUtil.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.alibaba.citrus.asm.ClassReader;
import com.alibaba.citrus.asm.ClassWriter;
import com.alibaba.citrus.asm.util.TraceClassVisitor;

/**
 * 动态生成和保存类或接口。
 * 
 * @author Michael Zhou
 */
public class ClassPool {
    private final BytecodeLoader classLoader;
    private boolean debugging;
    private File debuggingLocation;
    private int classWriterFlags;
    private String packageName;

    /**
     * 创建一个<code>ClassPool</code>，使用装载<code>ClassPool</code>类的
     * <code>ClassLoader</code>。
     */
    public ClassPool() {
        this(null);
    }

    /**
     * 创建一个<code>ClassPool</code>，使用指定的<code>ClassLoader</code>。
     */
    public ClassPool(ClassLoader parentClassLoader) {
        if (parentClassLoader == null) {
            parentClassLoader = getClass().getClassLoader();
        }

        this.classLoader = new BytecodeLoader(parentClassLoader);
        this.classWriterFlags = DEFAULT_CLASS_WRITER_FLAGS;
        this.packageName = DEFAULT_PACKAGE_NAME;
    }

    /**
     * 取得<code>ClassLoader</code>。
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * 是否保存class源文件和目标文件？
     */
    public boolean isDebugging() {
        return debugging;
    }

    /**
     * 设置参数：是否保存class源文件和目标文件？
     */
    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    /**
     * Class的源代码和二进制文件将被输出到该目录。
     */
    public File getDebuggingLocation() {
        return debuggingLocation;
    }

    /**
     * 设置class的源代码和二进制文件的输出目录。
     */
    public void setDebuggingLocation(File debuggingLocation) {
        this.debuggingLocation = debuggingLocation;
    }

    /**
     * 取得<code>ClassWriter</code>的标志位。
     */
    public int getClassWriterFlags() {
        return classWriterFlags;
    }

    /**
     * 设置<code>ClassWriter</code>的标志位。
     */
    public void setClassWriterFlags(int classWriterFlags) {
        this.classWriterFlags = classWriterFlags;
    }

    /**
     * 取得生成代码的package名称。
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * 设置生成代码的package名称。
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * 基于指定的<code>basename</code>生成一个唯一类名，返回用来创建该类型的<code>ClassBuilder</code>。
     */
    public ClassBuilder createClass(String basename, Class<?> superclass, Class<?>[] interfaces) {
        return createClassOrInterface(-1, false, basename, superclass, interfaces, -1, null);
    }

    /**
     * 基于指定的<code>basename</code>生成一个唯一类名，返回用来创建该类型的<code>ClassBuilder</code>。
     */
    public ClassBuilder createClass(int access, String basename, Class<?> superclass, Class<?>[] interfaces) {
        return createClassOrInterface(access, false, basename, superclass, interfaces, -1, null);
    }

    /**
     * 基于指定的<code>basename</code>生成一个唯一类名，返回用来创建该类型的<code>ClassBuilder</code>。
     */
    public ClassBuilder createClass(int access, String basename, Class<?> superclass, Class<?>[] interfaces,
                                    int classVersion, String source) {
        return createClassOrInterface(access, false, basename, superclass, interfaces, classVersion, source);
    }

    /**
     * 基于指定的<code>basename</code>生成一个唯一接口名，返回用来创建该类型的<code>ClassBuilder</code>。
     */
    public ClassBuilder createInterface(String basename, Class<?>[] interfaces) {
        return createClassOrInterface(-1, true, basename, null, interfaces, -1, null);
    }

    /**
     * 基于指定的<code>basename</code>生成一个唯一接口名，返回用来创建该类型的<code>ClassBuilder</code>。
     */
    public ClassBuilder createInterface(int access, String basename, Class<?>[] interfaces) {
        return createClassOrInterface(access, true, basename, null, interfaces, -1, null);
    }

    /**
     * 基于指定的<code>basename</code>生成一个唯一接口名，返回用来创建该类型的<code>ClassBuilder</code>。
     */
    public ClassBuilder createInterface(int access, String basename, Class<?>[] interfaces, int classVersion,
                                        String source) {
        return createClassOrInterface(access, true, basename, null, interfaces, classVersion, source);
    }

    /**
     * 基于指定的<code>basename</code>生成一个唯一类名，返回用来创建该类型的<code>ClassBuilder</code>。
     */
    private ClassBuilder createClassOrInterface(int access, boolean isInterface, String basename, Class<?> superclass,
                                                Class<?>[] interfaces, int classVersion, String source) {
        String className = generateClassName(basename, getPackageName());
        ClassWriter cw = new DebuggingClassWriter();

        return new PooledClassBuilder(cw, access, isInterface, className, superclass, interfaces, classVersion, source);
    }

    /**
     * 用来装载bytecode的<code>ClassLoader</code>。
     */
    private static class BytecodeLoader extends ClassLoader {
        public BytecodeLoader(ClassLoader parentClassLoader) {
            super(parentClassLoader);
        }

        public Class<?> defineClass(String className, byte[] bytes) {
            return defineClass(className, bytes, 0, bytes.length, getClass().getProtectionDomain());
        }
    }

    /**
     * 动态生成类或接口。
     */
    private class PooledClassBuilder extends ClassBuilder {
        public PooledClassBuilder(ClassWriter cw, int access, boolean isInterface, String className,
                                  Class<?> superclass, Class<?>[] interfaces, int classVersion, String source) {
            super(cw, access, isInterface, className, superclass, interfaces, classVersion, source);
        }

        @Override
        public Class<?> defineClass(String className, byte[] bytes) {
            return classLoader.defineClass(className, bytes);
        }
    }

    /**
     * 支持debugging的类创建器。
     */
    private class DebuggingClassWriter extends ClassWriter {
        private String className;

        public DebuggingClassWriter() {
            super(classWriterFlags);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if (debugging) {
                this.className = getTypeFromInternalName(name).getClassName();
            }

            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public byte[] toByteArray() {
            byte[] bytes = super.toByteArray();

            if (className != null) {
                if (debuggingLocation == null) {
                    debuggingLocation = new File(System.getProperty("java.io.tmpdir"));
                }

                File baseFile = new File(debuggingLocation, className.replace('.', File.separatorChar));
                File classDir = baseFile.getParentFile();

                classDir.mkdirs();

                if (classDir.exists() && classDir.isDirectory()) {
                    try {
                        // 输出class文件
                        File classFile = new File(classDir, baseFile.getName() + ".class");
                        OutputStream out = new BufferedOutputStream(new FileOutputStream(classFile));

                        try {
                            out.write(bytes);
                        } finally {
                            out.close();
                        }

                        // 输出asm文件
                        File asmFile = new File(classDir, baseFile.getName() + ".asm");
                        out = new BufferedOutputStream(new FileOutputStream(asmFile));

                        try {
                            ClassReader cr = new ClassReader(bytes);
                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
                            TraceClassVisitor tcv = new TraceClassVisitor(null, pw);

                            cr.accept(tcv, 0);
                            pw.flush();
                        } finally {
                            out.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }

            return bytes;
        }
    }
}
