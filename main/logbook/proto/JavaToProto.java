package logbook.proto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.CaseFormat;

/**
 * @author Nekopanda
 *
 */
public class JavaToProto {

    private static String NAME = "JavaToProto Generator";
    private static String VERSION = "v0.2";

    private static String OPEN_BLOCK = " {";
    private static String CLOSE_BLOCK = "}";
    private static String MESSAGE = "message";
    private static String ENUM = "enum";
    private static String NEWLINE = "\n";
    private static char TAB = '\t';
    private static String COMMENT = "//";
    private static String SPACE = " ";
    private static String PATH_SEPERATOR = ".";
    private static String OPTIONAL = "optional";
    private static String REQUIRED = "required";
    private static String REPEATED = "repeated";
    private static String LINE_END = ";";
    private static String SUFFIX = "Pb";

    private static char[] TABS = new char[] { TAB, TAB, TAB, TAB, TAB, TAB, TAB, TAB, TAB, TAB, TAB, TAB };
    private static String[] NAMES = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k" };

    private String header;
    private String proto;
    private String builder;
    private int depth;
    private int builderNumber;
    private final List<Class> roots = new ArrayList<Class>();
    private final Map<String, Message> typeMap;

    private static class MessageField {
        private final Message type;
        private final String name;
        private final int number;
        private final boolean repeat;
        private final boolean map;

        MessageField(Message type, String name, int number, boolean repeat, boolean map) {
            this.type = type;
            this.name = name;
            this.number = number;
            this.repeat = repeat;
            this.map = map;
        }

        public Message getType() {
            return this.type;
        }

        public String getName() {
            return this.name;
        }

        public int getNumber() {
            return this.number;
        }

        public boolean isRepeat() {
            return this.repeat;
        }

        public boolean isMap() {
            return this.map;
        }
    }

    private class Message {
        String javaName; // ShipDtoPb
        String javaTypeName; // ShipDto
        String protoName; // ShipDtoPb
        final List<MessageField> fields = new ArrayList<MessageField>();

        Message(String javaName, String typeName, String protoName) {
            this.javaTypeName = typeName;
            this.javaName = javaName;
            this.protoName = protoName;
        }

        Message(String javaName, String typeName) {
            this.javaTypeName = typeName;
            this.javaName = javaName;
            this.protoName = javaName;
        }

        public String genNewBuilder(StringBuilder builder) {
            int nextNumber = JavaToProto.this.builderNumber++;
            String builderName = "builder" + ((nextNumber == 0) ? "" : String.valueOf(nextNumber));
            builder.append(this.javaName).append(".Builder ")
                    .append(builderName).append(" = ").append(this.javaName)
                    .append(".newBuilder();\n");
            return builderName;
        }

        public String genProtoBuilder(
                StringBuilder builder,
                String objName,
                boolean useGetter)
        {
            String builderName = this.genNewBuilder(builder);

            for (MessageField f : this.fields) {
                builder.append(TABS, 0, JavaToProto.this.depth);
                String uc = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, f.getName());
                if (f.isRepeat()) {
                    String suffix = f.isMap() ? ".entrySet()" : "";
                    String valName = NAMES[JavaToProto.this.depth++];
                    builder.append("if(").append(objName).append(".").append(f.getName()).append(" != null) { ");
                    builder.append("for(").append(f.getType().javaTypeName).append(" ")
                            .append(valName).append(" : ").append(objName).append(".")
                            .append(f.getName()).append(suffix).append(") {\n").append(TABS, 0, JavaToProto.this.depth);
                    f.getType().genToProto(builder, builderName + ".add" + uc, valName);
                    JavaToProto.this.depth--;
                    builder.append("\n").append(TABS, 0, JavaToProto.this.depth).append("}").append(" }");
                }
                else {
                    String getter = objName + "." + (useGetter ? ("get" + uc + "()") : f.getName());
                    f.getType().genToProto(builder, builderName + ".set" + uc, getter);
                }
                builder.append("\n");
            }

            return builderName;
        }

        public void addChild(MessageField field) {
            this.fields.add(field);
        }

        public void genDefinition(StringBuilder builder) {
            builder.append(MESSAGE)
                    .append(SPACE).append(this.protoName).append(OPEN_BLOCK)
                    .append(NEWLINE);

            for (MessageField f : this.fields) {
                String lu = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, f.getName());
                String repeat = f.isRepeat() ? REPEATED : OPTIONAL;
                builder.append("\t").append(repeat).append(" ").append(f.getType().protoName).append(" ")
                        .append(lu).append(" = ").append(f.getNumber()).append(";\n");
            }

            builder.append(CLOSE_BLOCK).append(NEWLINE).append(NEWLINE);
        }

        public void genToProtoMethod(StringBuilder builder) {
            builder.append("public ").append(this.javaName).append(" toProto() {\n");
            JavaToProto.this.builderNumber = 0;
            JavaToProto.this.depth = 1;
            builder.append(TABS, 0, JavaToProto.this.depth);
            String b = this.genProtoBuilder(builder, "this", false);
            builder.append("\treturn ").append(b).append(".build();\n}");
        }

        public void genToProto(StringBuilder builder, String setMethod, String obj) {
            builder.append(setMethod).append("(").append(obj).append(".toProto()").append(");");
        }

        public boolean isPrimitive() {
            return false;
        }
    }

    private class JavaType extends Message {
        Class type;

        JavaType(String name, Class type) {
            super(name + SUFFIX, type.getSimpleName());
            this.type = type;
        }

        @Override
        public void genToProto(StringBuilder builder, String setMethod, String obj) {
            builder.append("if(").append(obj).append(" != null) { ")
                    .append(setMethod).append("(").append(obj).append(".toProto()").append("); }");
        }
    }

    private class PrimitiveType extends Message {
        PrimitiveType(String javaName, String protoName) {
            super(javaName, javaName, protoName);
        }

        @Override
        public void genDefinition(StringBuilder builder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void genToProto(StringBuilder builder, String setMethod, String obj) {
            builder.append(setMethod).append("(").append(obj).append(");");
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }
    }

    private class NullablePrimitiveType extends Message {
        NullablePrimitiveType(String javaName, String protoName) {
            super(javaName, javaName, protoName);
        }

        @Override
        public void genDefinition(StringBuilder builder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void genToProto(StringBuilder builder, String setMethod, String obj) {
            builder.append("if(").append(obj).append(" != null) { ")
                    .append(setMethod).append("(").append(obj).append("); }");
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }
    }

    private class PairType extends Message {
        Class type1;
        Class type2;

        PairType(String name, Class type1, Class type2) {
            super(name + SUFFIX, "Map.Entry<" + type1.getSimpleName() + "," + type2.getSimpleName() + ">");
            this.type1 = type1;
            this.type2 = type2;
        }

        @Override
        public void genToProto(StringBuilder builder, String setMethod, String obj) {
            String b = this.genProtoBuilder(builder, obj, true);
            builder.append(TABS, 0, JavaToProto.this.depth).append(setMethod)
                    .append("(").append(b).append(".build()").append(");");
        }
    }

    private class EnumType extends Message {
        Class type;

        EnumType(String name, Class type) {
            super(name + SUFFIX, type.getSimpleName());
            this.type = type;
        }

        @Override
        public void genDefinition(StringBuilder builder) {
            builder.append(ENUM).append(SPACE).append(this.protoName).append(OPEN_BLOCK).append(NEWLINE);
            int i = 0;
            for (Field f : this.type.getDeclaredFields()) {
                if (f.isEnumConstant()) {
                    builder.append("\t").append(f.getName()).append(" = ").append(i).append(LINE_END)
                            .append(NEWLINE);
                    ++i;
                }
            }
            builder.append(CLOSE_BLOCK).append(NEWLINE).append(NEWLINE);
        }

        @Override
        public void genToProtoMethod(StringBuilder builder) {
            // to proto
            builder.append("public ").append(this.javaName).append(" toProto() {\n\t");
            builder.append("switch(this) {\n\t");
            for (Field f : this.type.getDeclaredFields()) {
                if (f.isEnumConstant()) {
                    builder.append("case ").append(f.getName()).append(":\n\t\treturn ").append(this.javaName)
                            .append(".").append(f.getName()).append(";\n\t");
                }
            }
            builder.append("}\n");
            builder.append("\treturn null;\n}");
            // from proto
            builder.append("\npublic static ").append(this.javaTypeName)
                    .append(" fromProto(").append(this.javaName).append(" pb) {\n\t");
            builder.append("switch(pb.getNumber()) {\n\t");
            int number = 0;
            for (Field f : this.type.getDeclaredFields()) {
                if (f.isEnumConstant()) {
                    builder.append("case ").append(number++).append(":\n\t\treturn ")
                            .append(f.getName()).append(";\n\t");
                }
            }
            builder.append("}\n");
            builder.append("\treturn null;\n}");
        }
    }

    private class ListType extends Message {
        Class type;

        ListType(String name, Class type) {
            super(name + SUFFIX, type.getSimpleName());
            this.type = type;
        }

        @Override
        public void genToProto(StringBuilder builder, String setMethod, String obj) {
            String builderName = this.genNewBuilder(builder);

            MessageField f = this.fields.get(0);
            builder.append(TABS, 0, JavaToProto.this.depth);
            String valName = NAMES[JavaToProto.this.depth++];
            builder.append("if(").append(obj).append(" != null) { ");
            builder.append("for(").append(f.getType().javaTypeName).append(" ").append(valName).append(" : ")
                    .append(obj).append(") {\n").append(TABS, 0, JavaToProto.this.depth);
            f.getType().genToProto(builder, builderName + ".addData", valName);
            JavaToProto.this.depth--;
            builder.append("\n").append(TABS, 0, JavaToProto.this.depth).append("}").append(" }");
            builder.append("\n");

            builder.append(TABS, 0, JavaToProto.this.depth).append(setMethod)
                    .append("(").append(builderName).append(".build()").append(");");
        }
    }

    /**
     * Entry Point for the CLI Interface to this Program.
     * @param args
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: \n\tjava -jar JavaToProto.jar JavaToProto <class name> [<output file name>]\n");
        }

        Class clazz;

        try {
            clazz = Class.forName(args[0]);
        } catch (Exception e) {
            System.out.println("Could not load class. Make Sure it is in the classpath!!");
            e.printStackTrace();
            return;
        }

        JavaToProto jtp = new JavaToProto(clazz);

        String protoFile = jtp.toString();

        if (args.length == 2) {
            //Write to File

            try {
                File f = new File(args[1]);
                FileWriter fw = new FileWriter(f);
                BufferedWriter out = new BufferedWriter(fw);
                out.write(protoFile);
                out.flush();
                out.close();
            } catch (Exception e) {
                System.out.println("Got Exception while Writing to File - See Console for File Contents");
                System.out.println(protoFile);
                e.printStackTrace();
            }

        } else {
            //Write to Console
            System.out.println(protoFile);
        }

    }

    /**
     * Creates a new Instance of JavaToProto to process the given class
     * @param classToProcess - The Class to be Processed - MUST NOT BE NULL!
     */
    public JavaToProto(Class classToProcess) {
        if (classToProcess == null) {
            throw new RuntimeException(
                    "You gave me a null class to process. This cannot be done, please pass in an instance of Class");
        }
        this.typeMap = this.getPrimitivesMap();
        this.roots.add(classToProcess);
    }

    public JavaToProto() {
        this.typeMap = this.getPrimitivesMap();
    }

    public void addClass(Class type) {
        this.roots.add(type);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    //region Helper Functions

    public Map<String, Message> getPrimitivesMap() {
        Map<String, Message> results = new HashMap<String, Message>();

        results.put("double", new PrimitiveType("double", "double"));
        results.put("Double", new NullablePrimitiveType("Double", "double"));
        results.put("float", new PrimitiveType("float", "float"));
        results.put("Float", new NullablePrimitiveType("Float", "float"));
        results.put("int", new PrimitiveType("int", "sint32"));
        results.put("Integer", new NullablePrimitiveType("Integer", "sint32"));
        results.put("long", new PrimitiveType("long", "sint64"));
        results.put("Long", new NullablePrimitiveType("Long", "sint64"));
        results.put("boolean", new PrimitiveType("boolean", "bool"));
        results.put("Boolean", new NullablePrimitiveType("Boolean", "bool"));
        results.put("String", new NullablePrimitiveType("String", "string"));

        results.put("Date", new NullablePrimitiveType("Date", "sint64") {
            @Override
            public void genToProto(StringBuilder builder, String setMethod, String obj) {
                builder.append("if(").append(obj).append(" != null) { ")
                        .append(setMethod).append("(").append(obj).append(".getTime()").append("); }");
            }
        });
        results.put("Calendar", new NullablePrimitiveType("Calendar", "sint64") {
            @Override
            public void genToProto(StringBuilder builder, String setMethod, String obj) {
                builder.append("if(").append(obj).append(" != null) { ")
                        .append(setMethod).append("(").append(obj).append(".getTime().getTime()").append("); }");
            }
        });

        return results;
    }

    //end region

    private String generateProto() {
        if (this.proto == null) {
            // make a type graph
            for (Class type : this.roots) {
                this.visitType(type);
            }

            StringBuilder builder = new StringBuilder();

            //File Header
            builder.append(COMMENT).append("Generated by ")
                    .append(NAME).append(SPACE).append(VERSION).append(" @ ")
                    .append(new Date()).append(NEWLINE).append(NEWLINE);
            builder.append(this.header);

            for (Message m : this.typeMap.values()) {
                if (!m.isPrimitive()) {
                    m.genDefinition(builder);
                    builder.append(NEWLINE);
                }
            }

            this.proto = builder.toString();

            builder = new StringBuilder();

            for (Message m : this.typeMap.values()) {
                if ((m instanceof JavaType) || (m instanceof EnumType)) {
                    m.genToProtoMethod(builder);
                    builder.append(NEWLINE);
                }
            }

            this.builder = builder.toString();
        }
        return this.proto;
    }

    private String getTypeName(Class type) {
        if (type.isArray()) {
            return this.getTypeName(type.getComponentType()) + "List";
        }
        if (Map.class.isAssignableFrom(type)) {
            throw new RuntimeException("Raw map type is not supported");
        }
        else if (Collection.class.isAssignableFrom(type)) {
            throw new RuntimeException("Raw list type is not supported");
        }
        return type.getSimpleName();
    }

    private String getTypeName(Type fieldType) {
        if (fieldType instanceof Class) {
            return this.getTypeName((Class) fieldType);
        }
        else if (fieldType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) fieldType;
            Class rawType = (Class) type.getRawType();

            // Map
            if (Map.class.isAssignableFrom(rawType)) {
                Type[] actualTypes = type.getActualTypeArguments();
                return "Pair" + this.getTypeName(actualTypes[0]) + this.getTypeName(actualTypes[1]);
            }

            // List
            else if (Collection.class.isAssignableFrom(rawType)) {
                return this.getTypeName(type.getActualTypeArguments()[0]) + "List";
            }

            String name = rawType.getSimpleName();
            for (Type actualType : type.getActualTypeArguments()) {
                name = name + this.getTypeName(actualType);
            }
            return name;
        }
        else {
            throw new UnsupportedOperationException("Unknown type instance");
        }
    }

    private Message addNewJavaType(String name, Class type) {
        Message m = new JavaType(name, type);
        this.typeMap.put(type.getSimpleName(), m);

        Class curType = type;
        while ((curType != Object.class) && (curType != null)) {
            Field[] fields = curType.getDeclaredFields();
            for (Field f : fields) {
                /*
                int mod = f.getModifiers();
                if (Modifier.isTransient(mod) || Modifier.isStatic(mod)) {
                    continue;
                }*/
                Tag tag = f.getAnnotation(Tag.class);
                if (tag == null) {
                    //Skip this field
                    continue;
                }
                m.addChild(this.visitField(f.getGenericType(), f.getName(), tag.value()));
            }
            curType = curType.getSuperclass();
        }

        return m;
    }

    private Message visitType(Class type) {
        String name = this.getTypeName(type);

        if (this.typeMap.containsKey(name)) {
            return this.typeMap.get(name);
        }

        // Array
        else if (type.isArray()) {
            // In Java, there are no generic array...
            Message m = new ListType(name, type);
            this.typeMap.put(name, m);
            Class innerType = type.getComponentType();
            m.addChild(new MessageField(this.visitType(innerType), "data", 1, true, false));
            return m;
        }

        // Map
        else if (Map.class.isAssignableFrom(type)) {
            throw new RuntimeException("Raw map type is not supported");
        }

        // List
        else if (Collection.class.isAssignableFrom(type)) {
            throw new RuntimeException("Raw list type is not supported");
        }

        // Enum
        else if (type.isEnum()) {
            Message m = new EnumType(type.getSimpleName(), type);
            this.typeMap.put(type.getSimpleName(), m);
            return m;
        }

        //Ok so not a primitive / scalar, not a map or collection, and we havnt already processed it
        //So it must be another pojo
        else {
            return this.addNewJavaType(type.getSimpleName(), type);
        }
    }

    private MessageField visitField(Type fieldType, String fieldName, int number) {

        if (fieldType instanceof Class) {
            Class type = (Class) fieldType;

            // Array
            if (type.isArray()) {
                // In Java, there are no generic array...
                Class innerType = type.getComponentType();
                Message m = this.visitType(innerType);
                return new MessageField(m, fieldName, number, true, false);
            }
            else {
                return new MessageField(this.visitType(type), fieldName, number, false, false);
            }
        }
        else if (fieldType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) fieldType;
            Class rawType = (Class) type.getRawType();
            String name = this.getTypeName(fieldType);

            // Map
            if (Map.class.isAssignableFrom(rawType)) {
                if (!this.typeMap.containsKey(name)) {
                    Class innerType = (Class) type.getActualTypeArguments()[0];
                    Class innerType2 = (Class) type.getActualTypeArguments()[1];
                    Message m = new PairType(name, innerType, innerType2);
                    this.typeMap.put(name, m);
                    m.addChild(this.visitField(innerType, "key", 1));
                    m.addChild(this.visitField(innerType2, "value", 2));
                }
                return new MessageField(this.typeMap.get(name), fieldName, number, true, true);
            }

            // List
            else if (Collection.class.isAssignableFrom(rawType)) {
                Class innerType = (Class) type.getActualTypeArguments()[0];
                Message m = this.visitType(innerType);
                return new MessageField(m, fieldName, number, true, false);
            }

            // Types we have come across before
            else if (this.typeMap.containsKey(name)) {
                return new MessageField(this.typeMap.get(name), fieldName, number, false, false);
            }
            else {
                throw new UnsupportedOperationException("Unsupported generic type");
            }
        }
        else {
            throw new UnsupportedOperationException("Unknown type instance");
        }
    }

    public String getBuilderMethods() {
        this.generateProto();
        return this.builder;
    }

    @Override
    public String toString()
    {
        this.generateProto();
        return this.proto;
    }

}
