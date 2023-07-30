# ClazzSearcher
一款使用Yaml定义搜索规则来搜索Class的工具
### Quick Start
下面是yml的规则搜索模版(如果不需要引入某条rule则不写)
```yaml
importList: # target中所import的类,支持正则
  - java.io.File
  - java.io.IOException
  - org.apache.log4j.Level
  - org.*

access : public/protected/default/private # target的访问修饰符

name: "TemplateClazzName" # target的类名,支持正则匹配

type: class/interface/enum/annotation # target的类型,四选一

extendsList: # target所继承的类
  - java.lang.Object

implementsList: # target所实现的接口
  - interface1
  - interface2

annotations: # target所拥有的注解
  - annotation1
  - annotation2

fields: # target拥有的field
  - {
    "name": "field1",
    "type": "java.lang.String",
    "access": public/protected/default/private,
  }
  - {
    "name": "field2",
    "type": "java.lang.Object",
    "access": public/protected/default/private,
  }
methods: # target拥有的method
  - {
    "clazz": "clazz1",
    "name": "method1",
    "desc": "()Ljava/lang/String;", # method描述
    "access": false,
    "call" : [ # method中call的方法
      "java/lang/reflect/Method.invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;",
      "org/xml/sax/helpers/XMLFilterImpl.parse(Lorg/xml/sax/InputSource;)V",
    ]

  }
  - {
    "clazz": "clazz1",
    "name": "method2",
    "desc": "(Ljava/io/Reader;)Ljava/lang/Object;",
    "access": true,
    "call" : [
      "javax/xml/ws/EndpointReference.toString()Ljava/lang/String;"
    ]
  }
```
例如下面一段规则
```yaml
access : public
type: class
implementsList:
  - org/springframework/beans/PropertyValues
  - java/io/Serializable
extendsList:
  - org/springframework/beans/MutablePropertyValues
```
运行`ClazzSearchApplication`加上参数`--f example.yml --boot d3forest-1.0-SNAPSHOT.jar`
```
[main] INFO org.vidar.discovery.ClazzDiscovery - 加载所有方法信息完毕
[main] INFO org.vidar.discovery.ClazzDiscovery - 加载所有类信息完毕
[main] INFO org.vidar.discovery.ClazzDiscovery - 加载所有父子类、超类、实现类关系
[main] INFO org.vidar.discovery.ClazzDiscovery - 加载方法调用信息完毕
[main] INFO org.vidar.discovery.ClazzDiscovery - 开始寻找目标类...
[main] INFO org.vidar.discovery.ClazzDiscovery - 你所寻找的class实现的接口有：
[main] INFO org.vidar.discovery.ClazzDiscovery - org/springframework/beans/PropertyValues
[main] INFO org.vidar.discovery.ClazzDiscovery - java/io/Serializable
[main] INFO org.vidar.discovery.ClazzDiscovery - 你所寻找的class继承的类有：
[main] INFO org.vidar.discovery.ClazzDiscovery - org/springframework/beans/MutablePropertyValues
找到一个类：ClassReference.Handle(name=org/springframework/web/bind/ServletRequestParameterPropertyValues)
找到一个类：ClassReference.Handle(name=org/springframework/web/filter/GenericFilterBean$FilterConfigPropertyValues)
找到一个类：ClassReference.Handle(name=org/springframework/web/servlet/HttpServletBean$ServletConfigPropertyValues)
```

### TODOList
- 还很多没做，还在开发ing