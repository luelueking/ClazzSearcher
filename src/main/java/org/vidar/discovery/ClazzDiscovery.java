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
    public void discover(List<Path> pathList, ClazzRule clazzRule) throws IOException {
        // 加载所有方法信息
        Map<MethodReference.Handle, MethodReference> methodMap = DataLoader.loadMethods();
        LOGGER.info("加载所有方法信息完毕");
        // 加载所有类信息
        Map<ClassReference.Handle, ClassReference> classMap = DataLoader.loadClasses();
        LOGGER.info("加载所有类信息完毕");
        // 加载所有父子类、超类、实现类关系
        InheritanceMap inheritanceMap = InheritanceMap.load();
        LOGGER.info("加载所有父子类、超类、实现类关系");
        // 加载方法调用信息
        HashMap<MethodReference.Handle, GraphCall> callMap = DataLoader.loadCalls();
        LOGGER.info("加载方法调用信息完毕");



        LOGGER.info("开始寻找目标类...");
        ArrayList<Object> res = new ArrayList<>();
        List<String> implementsList = clazzRule.getImplementsList();
        LOGGER.info("你所寻找的类继承的接口有：");
        implementsList.forEach(LOGGER::info);
        if (implementsList.size() != 0) {
            Iterator<Map.Entry<ClassReference.Handle, ClassReference>> iterator = classMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<ClassReference.Handle, ClassReference> next = iterator.next();
                ClassReference.Handle key = next.getKey();
                AtomicBoolean is = new AtomicBoolean(true);
                for (String s : implementsList) {
                    if (!inheritanceMap.isSubclassOf(key,new ClassReference.Handle(s))) {
                        is.set(false);
                    }
                }
                if (is.get() == true) {
                    res.add(key);
                    System.out.println("找到一个类：" + key);
                }
            }
        }


    }

}
