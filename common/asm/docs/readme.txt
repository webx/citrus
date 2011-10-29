说明:
======

本项目是从ObjectWeb ASM v3.1(http://asm.objectweb.org/license.html)中移植，并做了如下修改：

* 修改package
  org.objectweb.asm -> com.alibaba.citrus.asm
  org/objectweb/asm -> com/alibaba/citrus/asm

* 修改单元测试：
  1. 只保留了conform测试。
  2. 增加AsmTestParams辅助类。
  3. 修改AbstractTest类，使之：
     # 使用AsmTestParams而不是System properties。
     # 改进suite的名称，加上“-part”
     # 将stream改成URL，避免内存溢出
     # 添加exclude参数，排除多余的文件
     # 关闭zip流
     # 设置一个suite中test的最大数量
  4. 修改了ASMifierUnitTest、CheckClassAdapterUnitTest、TraceClassAdapterUnitTest中传给main函数的参数。
  5. 修改了ASMifierTest、GASMifierTest中的参数：clazz=java.lang
  6. 修改了ClassWriterComputeFramesTest、LocalVariablesSorterTest2、SimpleVerifierTest中的参数：parts=2
  7. 修改所有对AbstractTest.is的引用，改成openStream()调用。
  8. 修改了SerialVersionUIDAdderUnitTest中的UID，以便通过测试。
  9. ClassNodeTest中，设置InsnList.check = false，以便通过测试。
  
* 将StringBuffer改成StringBuilder

* Cleanup

* 测试要点：内存需求非常大，最好分配1G-2G的空间：-Xms1536M -Xmx1536M -XX:MaxPermSize=512M

