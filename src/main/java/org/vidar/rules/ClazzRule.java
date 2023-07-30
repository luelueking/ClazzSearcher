package org.vidar.rules;

import lombok.Data;

import java.util.List;

/**
 * @author zhchen
 */
@Data
public class ClazzRule {
    private List<String> importList;
    private String name;
    private String access;
    private String type;
    private String extend;
    private List<String> implementsList;
    private List<String> annotations;
    private List<Field> fields;
    private List<Method> methods;


    @Data
    public static class Field {
        private String name;
        private String type;
        private String access;
    }

    @Data
    public static class Method {
        private String clazz;
        private String name;
        private String desc;
        private boolean access;
        private List<String> call;
    }

}
