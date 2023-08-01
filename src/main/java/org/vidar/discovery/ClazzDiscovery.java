package org.vidar.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vidar.data.*;
import org.vidar.rules.ClazzRule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhchen
 */
public class ClazzDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClazzDiscovery.class);

    private Map<MethodReference.Handle, MethodReference> methodMap;

    private Map<ClassReference.Handle, ClassReference> classMap;

    private InheritanceMap inheritanceMap;

    private Map<MethodReference.Handle, GraphCall> callMap;
    Set<ClassReference.Handle> res = new HashSet<>();

    public void discover(List<Path> pathList, ClazzRule clazzRule) throws IOException {
        init();
        LOGGER.info("开始寻找目标类...");
        // 标志是否被filter过
        boolean flag = false;
        if (clazzRule.getMethods() != null) {
            filterByMethods(clazzRule);
            flag = true;
        }
        filterByParent(clazzRule, flag);
        filterByAnnotations(clazzRule, flag);
        System.out.println("以下是搜索结果：");
        res.forEach(System.out::println);
    }

    private void filterByAnnotations(ClazzRule clazzRule, boolean flag) {
        // 限定注解
        if (flag) { // 被filter过
            if (res.size() == 0) {
                return;
            }
            Set<ClassReference.Handle> toRemove = new HashSet<>();
            List<String> annotations = clazzRule.getAnnotations();
            if (annotations != null && annotations.size() != 0) {
                for (ClassReference.Handle clz : res) {
                    ClassReference classReference = classMap.get(clz);
                    Set<String> clzAnnotations = classReference.getAnnotations();
                    for (String annotation : annotations) {
                        if (!clzAnnotations.contains(annotation)) {
                            toRemove.add(clz);
                        }
                    }
                }
                if (toRemove.size() != 0) {
                    res.removeAll(toRemove);
                }
            }
        } else {
            List<String> annotations = clazzRule.getAnnotations();
            if (annotations != null && annotations.size() != 0) {
                for (Map.Entry<ClassReference.Handle, ClassReference> next : classMap.entrySet()) {
                    ClassReference clzRef = next.getValue();
                    Set<String> clzAnnotations = clzRef.getAnnotations();
                    boolean hasAll = true;
                    if (clzAnnotations != null && clzAnnotations.size() != 0) {
                        for (String annotation : annotations) {
                            // 必须含有所有注解，有一个没有都不行
                            if (!clzAnnotations.contains(annotation)) {
                                hasAll = false;
                                break;
                            }
                        }
                    }
                    if (hasAll) {
                        res.add(next.getKey());
                    }
                }
            }
        }

    }


    private void filterByParent(ClazzRule clazzRule, boolean flag) {
        if (flag) {
            if (res.size() == 0) {
                return;
            }
            List<String> parentLists = new ArrayList<>();
            if (clazzRule.getImplementsList() != null && clazzRule.getImplementsList().size() != 0) {
                LOGGER.info("你所寻找的class实现的接口有：");
                clazzRule.getImplementsList().forEach(p -> {
                    LOGGER.info(p);
                    parentLists.add(p);
                });
            }
            if (clazzRule.getExtendsList() != null && clazzRule.getExtendsList().size() != 0) {
                LOGGER.info("你所寻找的class继承的类有：");
                clazzRule.getExtendsList().forEach(p -> {
                    LOGGER.info(p);
                    parentLists.add(p);
                });
            }
            if (parentLists.size() != 0) {
                for (ClassReference.Handle clz : res) {
                    for (String parent : parentLists) {
                        if (!inheritanceMap.isSubclassOf(clz, new ClassReference.Handle(parent))) {
                            res.remove(clz);
                        }
                    }
                }
            }
        } else { // 没有被filter过
            List<String> parentLists = new ArrayList<>();
            if (clazzRule.getImplementsList() != null && clazzRule.getImplementsList().size() != 0) {
                LOGGER.info("你所寻找的class实现的接口有：");
                clazzRule.getImplementsList().forEach(p -> {
                    LOGGER.info(p);
                    parentLists.add(p);
                });
            }
            if (clazzRule.getExtendsList() != null && clazzRule.getExtendsList().size() != 0) {
                LOGGER.info("你所寻找的class继承的类有：");
                clazzRule.getExtendsList().forEach(p -> {
                    LOGGER.info(p);
                    parentLists.add(p);
                });
            }
            // 限定parent
            if (parentLists.size() != 0) {
                for (Map.Entry<ClassReference.Handle, ClassReference> next : classMap.entrySet()) {
                    ClassReference.Handle key = next.getKey();
                    AtomicBoolean is = new AtomicBoolean(true);
                    for (String s : parentLists) {
                        if (!inheritanceMap.isSubclassOf(key, new ClassReference.Handle(s))) {
                            is.set(false);
                        }
                    }
                    if (is.get()) {
                        res.add(key);
                    }
                }
            }
        }
    }

    private void filterByMethods(ClazzRule clazzRule) {
        // 限定method
        List<ClazzRule.Method> methods = clazzRule.getMethods();
        if (methods.size() != 0) {
            methods.forEach(m -> LOGGER.info("你希望target中存在：" + m + "方法"));
            for (Map.Entry<MethodReference.Handle, MethodReference> next : methodMap.entrySet()) {
                MethodReference.Handle key = next.getKey();
                AtomicBoolean is = new AtomicBoolean(false);
                for (ClazzRule.Method m : methods) {
                    if (m.getName() != null && !key.getName().equals(m.getName())) {
                        continue;
                    }
                    if (m.getDesc() != null && !key.getDesc().equals(m.getDesc())) {
                        continue;
                    }
                    if (m.getIsStatic() != null && methodMap.get(key).isStatic() != m.getIsStatic()) {
                        continue;
                    }
                    if (m.getAccess() != null && !methodMap.get(key).getAccessModifier().equals(m.getAccess())) {
                        continue;
                    }
                    is.set(true);
                    if (m.getCalls() != null) {
                        GraphCall graphCall = callMap.get(key);
                        List<MethodReference.Handle> callMethods = graphCall.getCallMethods();
                        out:
                        for (ClazzRule.Call call : m.getCalls()) {
                            for (MethodReference.Handle callMethod : callMethods) {
                                if (callMethod.getClassReference().getName().equals(call.getClassRef()) &&
                                        callMethod.getName().equals(call.getName()) &&
                                        callMethod.getDesc().equals(call.getDesc())) {
                                    continue out;
                                }
                            }
                            is.set(false);
                        }
                    }
                    break;
                }
                if (is.get()) {
                    res.add(key.getClassReference());
                }
            }
        }
    }

    private void init() throws IOException {
        // 加载所有方法信息
        methodMap = DataLoader.loadMethods();
        LOGGER.info("加载所有方法信息完毕");
        // 加载所有类信息
        classMap = DataLoader.loadClasses();
        LOGGER.info("加载所有类信息完毕");
        // 加载所有父子类、超类、实现类关系
        inheritanceMap = InheritanceMap.load();
        LOGGER.info("加载所有父子类、超类、实现类关系");
        // 加载方法调用信息
        callMap = DataLoader.loadCalls();
        LOGGER.info("加载方法调用信息完毕");
    }

}
