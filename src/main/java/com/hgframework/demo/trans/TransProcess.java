package com.hgframework.demo.trans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Created by Administrator on 2018/1/24 0024.
 */
public class TransProcess {

    //事务上下文
    private TransContext context;

    //分布式事务配置中心，从中获取事务配置信息
    private DConfigCenter configCenter;

    //事务状态管理
    //事务推进 commit,
    //事务回滚，原子性独立

    /**
     * 事务提交
     */
    public void commit() {

    }

    /**
     * 事务回滚
     */
    public void rollback() {

    }


    interface InnerProcess {

        void commit();

        void rollback();
    }

    static class DefaultInnerProcess implements InnerProcess {
        @Override
        public void commit() {
            System.out.println("commit");
        }

        @Override
        public void rollback() {
            System.out.println("rollback");
        }
    }

    static class ScanPackage {
        //包基本路径
        private String basePackage;

        public void doScan() {

        }
    }


    /**
     * 通过反射机制，动态代理机制实现事务执行器
     */
    static class ProcessHandler implements InvocationHandler {
        //代理目标对象
        private Object target;

        public ProcessHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("开始执行");
            Object ret = method.invoke(target, args);
            System.out.println("结束执行");
            return ret;
        }
    }

    static class StringUtil {

        /**
         * "file:/home/whf/cn/fh" -> "/home/whf/cn/fh"
         * "jar:file:/home/whf/foo.jar!cn/fh" -> "/home/whf/foo.jar"
         */
        public static String getRootPath(URL url) {
            String fileUrl = url.getFile();
            int pos = fileUrl.indexOf('!');
            if (-1 == pos) {
                return fileUrl;
            }
            return fileUrl.substring(5, pos);
        }

        /**
         * "cn.fh.lightning" -> "cn/fh/lightning"
         *
         * @param name
         * @return
         */
        public static String dotToSplash(String name) {
            return name.replaceAll("\\.", "/");
        }

        /**
         * "Apple.class" -> "Apple"
         */
        public static String trimExtension(String name) {
            int pos = name.indexOf('.');
            if (-1 != pos) {
                return name.substring(0, pos);
            }
            return name;
        }

        /**
         * /application/home -> /home
         *
         * @param uri
         * @return
         */
        public static String trimURI(String uri) {
            String trimmed = uri.substring(1);
            int splashIndex = trimmed.indexOf('/');
            return trimmed.substring(splashIndex);
        }
    }

    static class ClasspathPackageScanner {

        private Logger logger = LoggerFactory.getLogger(ClasspathPackageScanner.class);
        private String basePackage;
        private ClassLoader cl;

        /**
         * Construct an instance and specify the base package it should scan.
         *
         * @param basePackage The base package to scan.
         */
        public ClasspathPackageScanner(String basePackage) {
            this.basePackage = basePackage;
            this.cl = getClass().getClassLoader();
        }

        /**
         * Construct an instance with base package and class loader.
         *
         * @param basePackage The base package to scan.
         * @param cl          Use this class load to locate the package.
         */
        public ClasspathPackageScanner(String basePackage, ClassLoader cl) {
            this.basePackage = basePackage;
            this.cl = cl;
        }

        /**
         * Get all fully qualified names located in the specified package
         * and its sub-package.
         *
         * @return A list of fully qualified names.
         * @throws IOException
         */
        public List<String> getFullyQualifiedClassNameList() throws IOException {
            logger.info("开始扫描包{}下的所有类", basePackage);
            return doScan(basePackage, new ArrayList<>());
        }

        /**
         * Actually perform the scanning procedure.
         *
         * @param basePackage
         * @param nameList    A list to contain the result.
         * @return A list of fully qualified names.
         * @throws IOException
         */
        private List<String> doScan(String basePackage, List<String> nameList) throws IOException {
            // replace dots with splashes
            String splashPath = StringUtil.dotToSplash(basePackage);
            // get file path
            URL url = cl.getResource(splashPath);
            String filePath = StringUtil.getRootPath(url);

            // Get classes in that package.
            // If the web server unzips the jar file, then the classes will exist in the form of
            // normal file in the directory.
            // If the web server does not unzip the jar file, then classes will exist in jar file.
            List<String> names = null; // contains the name of the class file. e.g., Apple.class will be stored as "Apple"
            if (isJarFile(filePath)) {
                // jar file
                if (logger.isDebugEnabled()) {
                    logger.debug("{} 是一个JAR包", filePath);
                }

                names = readFromJarFile(filePath, splashPath);
            } else {
                // directory
                if (logger.isDebugEnabled()) {
                    logger.debug("{} 是一个目录", filePath);
                }
                names = readFromDirectory(filePath);
            }

            for (String name : names) {
                if (isClassFile(name)) {
                    //nameList.add(basePackage + "." + StringUtil.trimExtension(name));
                    nameList.add(toFullyQualifiedName(name, basePackage));
                } else {
                    // this is a directory
                    // check this directory for more classes
                    // do recursive invocation
                    doScan(basePackage + "." + name, nameList);
                }
            }

            if (logger.isDebugEnabled()) {
                for (String n : nameList) {
                    logger.debug("找到{}", n);
                }
            }

            return nameList;
        }

        /**
         * Convert short class name to fully qualified name.
         * e.g., String -> java.lang.String
         */
        private String toFullyQualifiedName(String shortName, String basePackage) {
            StringBuilder sb = new StringBuilder(basePackage);
            sb.append('.');
            sb.append(StringUtil.trimExtension(shortName));

            return sb.toString();
        }

        private List<String> readFromJarFile(String jarPath, String splashedPackageName) throws IOException {
            if (logger.isDebugEnabled()) {
                logger.debug("从JAR包中读取类: {}", jarPath);
            }

            JarInputStream jarIn = new JarInputStream(new FileInputStream(jarPath));
            JarEntry entry = jarIn.getNextJarEntry();
            List<String> nameList = new ArrayList<>();
            while (null != entry) {
                String name = entry.getName();
                if (name.startsWith(splashedPackageName) && isClassFile(name)) {
                    nameList.add(name);
                }
                entry = jarIn.getNextJarEntry();
            }

            return nameList;
        }

        private List<String> readFromDirectory(String path) {
            File file = new File(path);
            String[] names = file.list();
            if (null == names) {
                return null;
            }
            return Arrays.asList(names);
        }

        private boolean isClassFile(String name) {
            return name.endsWith(".class");
        }

        private boolean isJarFile(String name) {
            return name.endsWith(".jar");
        }
    }

    private static void testScan() throws Exception {
        ClasspathPackageScanner scan = new ClasspathPackageScanner("com.hgframework.demo.trans");
        scan.getFullyQualifiedClassNameList();
    }

    public static void main(String[] args) throws Exception {
        //获取代理的class对象，通过class对象生成构造器等
//        Class clazz = Proxy.getProxyClass(TransProcess.class.getClassLoader(), InnerProcess.class);
//        Constructor constructor= clazz.getConstructor(InvocationHandler.class);
//        InnerProcess process=constructor.newInstance(new )
//        InnerProcess innerProcess = new DefaultInnerProcess();
//        //通过代理类生成对象
//        InnerProcess process = (InnerProcess) Proxy.newProxyInstance(TransProcess.class.getClassLoader(), new Class[]{InnerProcess.class}, new ProcessHandler(innerProcess));
//        process.commit();

        testScan();


    }

}
