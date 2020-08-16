package com.ddf.boot.common.core.exception1;


import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>生成实体的简单工具类</p>
 * <ul>
 * <li>修改{@code SOURCE_ROOT} 为自己项目源码的根路径</li>
 * <li>数据库连接相关信息、忽略字段等已设置</li>
 * <li>运行主程序</li>
 * <li>请输入表名，输入要生成实体的数据库表名，个人习惯，不习惯实体带一些前缀造成几个大写开头，所以给分割掉了，想要的自己修改</li>
 * <li>请输入包名，输入要将该实体生成到哪个包下面，只需要输入包即可，项目根目录在第一步已设置，则程序运行结束后会将类源码生成到指定包下</li>
 * <li>可能会有部分类需要自行导包</li>
 * <li>可能会有部分数据库类型还未做转换，如出现'暂未支持的数据类型'错误，请查看{@link #castSqlType(int, String)}</li>
 * </ul>
 *
 * @author dongfang.ding on 2019/3/1
 */
public class EntityGenerateUtil {

    private static final String LINE = System.getProperty("line.separator");
    private static final String BLANK = "    ";
    private static final String TWO_LINE = (System.getProperty("line.separator") + System.getProperty("line.separator"));

    /**
     * 项目源码根路径地址,根据包名生成java代码的时候需要用到需要生成到哪个目录下
     */
//    private static final String SOURCE_ROOT = System.getProperty("bootUser.dir") + "/src/main/java";
    private static final String SOURCE_ROOT = "D:\\auto-pay\\provider-common\\core-model\\src\\main\\java";

    /**
     * 需要忽略生成的字段名,如BaseDomain的字段其它表都要忽略这几个字段
     */
    private static Set<String> ignoreColumn = new HashSet<>();

    static {
//        ignoreColumn.add("id");
////        ignoreColumn.add("create_by");
////        ignoreColumn.add("create_time");
////        ignoreColumn.add("modify_by");
////        ignoreColumn.add("modify_time");
////        ignoreColumn.add("removed");
////        ignoreColumn.add("version");

        ignoreColumn.add("id");
        ignoreColumn.add("create_date");
        ignoreColumn.add("update_date");
        ignoreColumn.add("merchant_create_user_id");
        ignoreColumn.add("merchant_update_user_id");
        ignoreColumn.add("platform_create_user_id");
        ignoreColumn.add("platform_update_user_id");
        ignoreColumn.add("is_del");
        ignoreColumn.add("version");

    }

    private static final String CATLOG = "company_pay_auto_v2";

    /**
     * 初始化数据库连接对象
     *
     * @return
     * @throws Exception
     */
    private static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        return DriverManager.getConnection("jdbc:mysql://192.168.3.188:3306/company_pay_auto_v2?characterEncoding" +
                "=utf8&useSSL=true&serverTimezone=GMT%2B8&zeroDateTimeBehavior=convertToNull", "root", "h8fCueq9ujqao3Y");
    }


    public static void main(String[] args) throws Exception {
//        generateEntity(TableType.LINE_HUMP);
        String packageName = "com.company.pay.core.model.datao";
        doAction(TableType.LINE_HUMP, packageName);
    }


    enum TableType {
        /**
         * 经典下划线表名直接转驼峰, 如user_info 对应生成实体类UserInfo
         */
        LINE_HUMP,

        /**
         * 经典下划线表名，但舍弃第一个下划线前的前缀部分,如t_user_info 对应生成实体类UserInfo
         */
        PREFIX_DISCARD_LINE_HUMP
    }

    /**
     * 生成实体源码
     *
     * @param tableType
     * @throws Exception
     */
    private static void generateEntity(TableType tableType) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入要表名: ");
        String tableName = scanner.nextLine();
        while (StringUtils.isEmpty(tableName)) {
            System.out.println("输入表名不能为空,请重新输入！");
            tableName = scanner.nextLine();
        }
        int columnType;
        System.out.println("请输入包名");
        String packageName = scanner.nextLine();
        while (StringUtils.isEmpty(packageName)) {
            System.out.println("输入包名不能为空,请重新输入！");
            packageName = scanner.nextLine();
        }
        doAction(tableType, packageName);
    }

    private static void doAction(TableType tableType, String packageName) throws Exception {
        Connection conn = getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        int columnType;

        ResultSet tables = metaData.getTables(CATLOG, "%", "%", new String[]{"TABLE"});
        while (tables.next()) {
            String tableRemarks = "", columnName, fieldType, columnRemarks, fieldName;
            String tableName = tables.getString("table_name");
            ResultSet columns = metaData.getColumns(CATLOG, "%", tableName, "%");
            tableRemarks = tables.getString("REMARKS");
            String className;
            if (TableType.PREFIX_DISCARD_LINE_HUMP.equals(tableType)) {
                className = lineToHump(tableName.substring(tableName.indexOf("_")));
            } else {
                className = lineToHump(tableName);
            }
            className = className.substring(0, 1).toUpperCase() + className.substring(1);
            StringBuffer sbl = initClass(packageName, tableName, tableRemarks, className);
            boolean isDateImport = false;

            while (columns.next()) {
                columnName = columns.getString("COLUMN_NAME");
                if (ignoreColumn.contains(columnName)) {
                    continue;
                }
                fieldName = lineToHump(columnName);
                columnType = columns.getInt("DATA_TYPE");
                fieldType = castSqlType(columnType, columnName, tableName);

                columnRemarks = columns.getString("REMARKS");
                sbl.append(LINE);
                sbl.append(BLANK).append("@ApiModelProperty(\"").append(columnRemarks).append("\")").append(LINE);
                if (" Date ".equals(fieldType)) {
                    isDateImport = true;
                }
                sbl.append(BLANK).append("private").append(fieldType).append(fieldName).append(";").append(LINE);
            }
            sbl.append("}");
            String packageDir = packageNameToFileDir(packageName);
            new File(SOURCE_ROOT + File.separator + packageDir).mkdirs();
            System.out.println("SOURCE_ROOT: " + SOURCE_ROOT);
            File file = new File(SOURCE_ROOT + File.separator + packageDir + File.separator + className + ".java");
            file.createNewFile();
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                String sourceStr = sbl.toString();
                if (isDateImport) {
                    sourceStr = String.format(sourceStr, "import java.util.Date;" + LINE);
                } else {
                    sourceStr = String.format(sourceStr, "");
                }
                outputStreamWriter.write(sourceStr);
            }
        }
    }

    /**
     * 将数据库类型转换为实体类型，可能会有遗漏，这里只转换了大部分，如不合适需要自己修改
     *
     * @param type
     * @param columnName
     * @return
     */
    private static String castSqlType(int type, String columnName, String tableName) {
        if (Types.TINYINT == type || Types.SMALLINT == type || Types.BIT == type) {
            return " Byte ";
        } else if (Types.INTEGER == type) {
            return " Integer ";
        } else if (Types.FLOAT == type || Types.DOUBLE == type || Types.NUMERIC == type || Types.DECIMAL == type) {
            return " BigDecimal ";
        } else if (Types.VARCHAR == type || Types.CHAR == type || Types.CLOB == type || Types.NVARCHAR == type
                || Types.LONGVARCHAR == type) {
            return " String ";
        } else if (Types.DATE == type || Types.TIME == type || Types.TIMESTAMP == type) {
            return " Date ";
        } else if (Types.BIGINT == type) {
            return " Long ";
        }
        throw new RuntimeException("暂未支持的数据类型" + type + "|" + tableName + " | " + columnName);
    }

    /**
     * 下划线转驼峰
     */
    private static String lineToHump(String str) {
        final String regex = "_(\\w)";
        Pattern linePattern = Pattern.compile(regex);
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * 初始化包名，要导入的包、类名和通用注解等
     *
     * @param packageName 要生成的包名
     * @param tableName   表名
     * @param className   类名
     * @return
     */
    public static StringBuffer initClass(String packageName, String tableName, String tableRemarks, String className) {
        StringBuffer sbl = new StringBuffer(200);
        sbl.append("package ").append(packageName).append(";").append(TWO_LINE);
        sbl.append("import lombok.*;").append(LINE);
        sbl.append("import io.swagger.annotations.ApiModel;").append(LINE);
        sbl.append("import io.swagger.annotations.ApiModelProperty;").append(LINE);
        sbl.append("import java.io.Serializable;").append(LINE);
        sbl.append("%s");
        sbl.append(LINE);
        sbl.append("/**").append(LINE);
        sbl.append(" * ").append(tableRemarks).append(LINE);
        sbl.append(" */").append(LINE);
        sbl.append("@ToString(callSuper = true)").append(LINE);
        sbl.append("@NoArgsConstructor").append(LINE);
        sbl.append("@AllArgsConstructor ").append(LINE);
        sbl.append("@Data").append(LINE);
        sbl.append("@ApiModel(\"").append(tableRemarks).append("\")").append(LINE);
        sbl.append("public class ").append(className).append(" extends BaseModel implements Serializable {").append(LINE);
        return sbl;
    }

    /**
     * 将输入的包名转换为文件路径
     *
     * @param packageName
     * @return
     */
    private static String packageNameToFileDir(String packageName) {
        return packageName.replaceAll("\\.", "\\" + File.separator);
    }
}
