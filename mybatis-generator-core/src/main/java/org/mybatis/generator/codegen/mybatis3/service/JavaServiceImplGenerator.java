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
package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.JavaBeansUtil.getValidPropertyName;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * Created by wuhao on 22/03/2017.
 */
public class JavaServiceImplGenerator extends AbstractJavaGenerator {
    @Override
    public List<CompilationUnit> getCompilationUnits() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.21", //$NON-NLS-1$
                introspectedTable.getFullyQualifiedTable().toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();
        String fullQualifiedJavaType = context.getJavaControllerGeneratorConfiguration().getTargetPackage()
                .replaceAll("controller", "service");

//        FullyQualifiedJavaType type = new FullyQualifiedJavaType(
//                introspectedTable.getMyBatis3JavaMapperType());
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(
                fullQualifiedJavaType + ".impl." + table.getDomainObjectName() + "ServiceImpl");
        TopLevelClass topLevelClass = new TopLevelClass(type);

        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        topLevelClass.addAnnotation("@Service");
        topLevelClass.addImportedType("org.slf4j.Logger");
        topLevelClass.addImportedType("org.slf4j.LoggerFactory");
        topLevelClass.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        topLevelClass.addImportedType("org.springframework.web.bind.annotation.*");
        topLevelClass.addImportedType("org.springframework.stereotype.Service");
        topLevelClass.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        topLevelClass.addImportedType(introspectedTable.getMyBatis3JavaMapperType());
        topLevelClass.addImportedType(introspectedTable.getBaseRecordType());
        topLevelClass.addImportedType("com.parsec.universal.dao.CommonDaoWrap");
        topLevelClass.addImportedType("com.parsec.universal.utils.Result");
        String serviceFullName = fullQualifiedJavaType + "." + table.getDomainObjectName() + "Service";
        topLevelClass.addImportedType(new FullyQualifiedJavaType(
                serviceFullName));

        topLevelClass.addSuperInterface(new FullyQualifiedJavaType(
                serviceFullName));

        //slf4j
        Field field = new Field();
        field.setType(new FullyQualifiedJavaType("org.slf4j.Logger"));
        field.setInitializationString("LoggerFactory.getLogger({project_name}.class)".
                replace("{project_name}", type.getShortName()));
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
        field.setName(new FullyQualifiedJavaType(daoInterfaceType).getShortName());
        field.setVisibility(JavaVisibility.DEFAULT);
        field.addAnnotation("@Autowired");
        topLevelClass.addField(field);

        //common dao
        field = new Field();
        field.setType(new FullyQualifiedJavaType("com.parsec.universal.dao.CommonDaoWrap"));
        field.setVisibility(JavaVisibility.DEFAULT);
        field.setName("commonDaoWrap");
        field.addAnnotation("@Autowired");
        topLevelClass.addField(field);

        FullyQualifiedJavaType baseType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        //get
        Method method = new Method();
        method.setReturnType(baseType);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("get");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "id"));
        method.addBodyLine("return commonDaoWrap.select(id," + baseType.getShortName() + ".class);");
        method.addAnnotation("@Override");
        topLevelClass.addMethod(method);

        //add
        method = new Method();
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("add");
        method.addAnnotation("@Override");
        String validPropertyName = getValidPropertyName(baseType.getShortName());
        method.addParameter(new Parameter(baseType, validPropertyName));
        method.addBodyLine("return commonDaoWrap.insert(" + validPropertyName + ");");
        topLevelClass.addMethod(method);

        //update
        method = new Method();
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("update");
        method.addAnnotation("@Override");
        method.addParameter(new Parameter(baseType, validPropertyName));
        method.addBodyLine("return commonDaoWrap.update(" + validPropertyName + ");");
        topLevelClass.addMethod(method);

        //del
        method = new Method();
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("delete");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "id"));
        method.addBodyLine("return commonDaoWrap.delete(id," + baseType.getShortName() + ".class);");
        method.addAnnotation("@Override");
        topLevelClass.addMethod(method);

        //list
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addAnnotation("@Override");
        method.setReturnType(new FullyQualifiedJavaType("com.parsec.universal.utils.Result"));
        method.setName("list");
        method.addParameter(new Parameter(baseType, validPropertyName));

        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(baseType);
        method.addBodyLine(listType.getShortName() + " list = commonDaoWrap.selectList(" + validPropertyName + ");");
        method.addBodyLine("long count = commonDaoWrap.selectCount(" + validPropertyName + ");");
        method.addBodyLine("return Result.retPageList(list, count, " + validPropertyName + ");");
        topLevelClass.addMethod(method);

        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
//        if (context.getPlugins().modelExampleClassGenerated(
//                topLevelClass, introspectedTable)) {
        answer.add(topLevelClass);
//        }
        return answer;
    }

}
