/**
 *    Copyright 2006-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.codegen.AbstractJavaGenerator;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getJavaBeansField;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getValidPropertyName;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.List;

/**
 * Created by wuhao on 20/03/2017.
 */
public class JavaControllerGenerator extends AbstractJavaGenerator {
    @Override
    public List<CompilationUnit> getCompilationUnits() {
////        // TODO: 20/03/2017 生成controller关联单元
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString(
                "Progress.19", table.toString())); //$NON-NLS-1$
        CommentGenerator commentGenerator = context.getCommentGenerator();
        String fullQualifiedJavaType = context.getJavaControllerGeneratorConfiguration().getTargetPackage();

        FullyQualifiedJavaType type = new FullyQualifiedJavaType(
                fullQualifiedJavaType + "." + table.getDomainObjectName() + "Controller");
        TopLevelClass topLevelClass = new TopLevelClass(type);

        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        topLevelClass.addAnnotation("@RestController");
        topLevelClass.addImportedType("org.slf4j.Logger");
        topLevelClass.addImportedType("org.slf4j.LoggerFactory");
        topLevelClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        topLevelClass.addImportedType("org.springframework.web.bind.annotation.*");
        topLevelClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        topLevelClass.addImportedType(type.getFullyQualifiedName().replace("controller", "service").replace("Controller", "Service"));
        topLevelClass.addImportedType(introspectedTable.getMyBatis3JavaMapperType());
        topLevelClass.addImportedType(introspectedTable.getBaseRecordType());
        topLevelClass.addImportedType("com.parsec.universal.utils.Result");
        topLevelClass.addImportedType("com.parsec.universal.dao.CommonDaoWrap");

        commentGenerator.addJavaFileComment(topLevelClass);

        //slf4j
        Field field = new Field();
        field.setType(new FullyQualifiedJavaType("org.slf4j.Logger"));
        field.setInitializationString("LoggerFactory.getLogger({project_name}.class)".
                replace("{project_name}", table.getDomainObjectName() + "Controller"));
        field.setName("logger");
        field.setFinal(true);
        field.setStatic(true);
        field.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(field);

        //dao
        field = new Field();
        String daoInterfaceType = introspectedTable.getMyBatis3JavaMapperType();
        field.setType(new FullyQualifiedJavaType(daoInterfaceType));
        daoInterfaceType = daoInterfaceType.substring(daoInterfaceType.lastIndexOf(".") + 1);
        field.setName(getValidPropertyName(daoInterfaceType));
        field.setVisibility(JavaVisibility.DEFAULT);
        field.addAnnotation("@Autowired");
        topLevelClass.addField(field);

        //service
        field = new Field();
        String serviceType = type.getFullyQualifiedName().replace("controller", "service").replace("Controller", "Service");
        serviceType = serviceType.substring(serviceType.lastIndexOf(".") + 1);
        field.setType(new FullyQualifiedJavaType(serviceType));
        field.setVisibility(JavaVisibility.DEFAULT);
        String serviceValidType = getValidPropertyName(serviceType);
        field.setName(serviceValidType);
        field.addAnnotation("@Autowired");
        topLevelClass.addField(field);

        //common dao
        field = new Field();
        field.setType(new FullyQualifiedJavaType("com.parsec.universal.dao.CommonDaoWrap"));
        field.setVisibility(JavaVisibility.DEFAULT);
        field.setName("commonDaoWrap");
        field.addAnnotation("@Autowired");
        topLevelClass.addField(field);


        String baseRecord = introspectedTable.getBaseRecordType();
        String baseRecordType = baseRecord.substring(baseRecord.lastIndexOf(".") + 1);
        String validPropertyName = getValidPropertyName(baseRecordType);
        // get method
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("get");
        method.addAnnotation("@GetMapping(\"" + validPropertyName + "/{id}\")");
        Parameter parameter = new Parameter(FullyQualifiedJavaType.getIntInstance(), "id", "@PathVariable");
        method.addParameter(parameter);
        method.addBodyLine("return Result.returnSuccess(" + serviceValidType + ".get(id),\"\");");
        method.setReturnType(new FullyQualifiedJavaType("com.parsec.universal.utils.Result"));
        topLevelClass.addMethod(method);

        // add method
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("add");
        method.addAnnotation("@PostMapping(\"" + validPropertyName + "\")");
        parameter = new Parameter(new FullyQualifiedJavaType(baseRecord), validPropertyName);
        method.addParameter(parameter);
        method.addBodyLine("return Result.returnSuccess(" + serviceValidType + ".add(" + validPropertyName + "),\"\");");
        method.setReturnType(new FullyQualifiedJavaType("com.parsec.universal.utils.Result"));
        topLevelClass.addMethod(method);

        // update method
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("update");
        method.addAnnotation("@PutMapping(\"" + validPropertyName + "\")");
        parameter = new Parameter(new FullyQualifiedJavaType(baseRecord), validPropertyName);
        method.addParameter(parameter);
        method.addBodyLine("return Result.returnSuccess(" + serviceValidType + ".update(" + validPropertyName + "),\"\");");
        method.setReturnType(new FullyQualifiedJavaType("com.parsec.universal.utils.Result"));
        topLevelClass.addMethod(method);

        // delete method
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("delete");
        method.setReturnType(new FullyQualifiedJavaType("com.parsec.universal.utils.Result"));
        method.addAnnotation("@DeleteMapping(\"" + validPropertyName + "/{id}\")");
        parameter = new Parameter(FullyQualifiedJavaType.getIntInstance(), "id", "@PathVariable");
        method.addParameter(parameter);
        method.addBodyLine("return Result.returnSuccess(" + serviceValidType + ".delete(id),\"\");");
        topLevelClass.addMethod(method);

        //list
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("list");
        method.setReturnType(new FullyQualifiedJavaType("com.parsec.universal.utils.Result"));
        method.addAnnotation("@GetMapping(\"" + validPropertyName + "s\")");
        parameter = new Parameter(FullyQualifiedJavaType.getIntInstance(), "pageNo", "@RequestParam(value = \"pageNo\", defaultValue = \"1\")");
        method.addParameter(parameter);
        parameter = new Parameter(FullyQualifiedJavaType.getIntInstance(), "pageSize", "@RequestParam(value = \"pageSize\", defaultValue = \"10\")");
        method.addParameter(parameter);
        method.addBodyLine(baseRecordType + " " + validPropertyName + " = new " + baseRecordType + "();");
        method.addBodyLine(validPropertyName + ".setPageNo(pageNo);");
        method.addBodyLine(validPropertyName + ".setPageSize(pageSize);");
        method.addBodyLine("return " + serviceValidType + ".list(" + validPropertyName + ");");
        topLevelClass.addMethod(method);


        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
//        if (context.getPlugins().modelExampleClassGenerated(
//                topLevelClass, introspectedTable)) {
        answer.add(topLevelClass);
//        }
        return answer;

    }
}
