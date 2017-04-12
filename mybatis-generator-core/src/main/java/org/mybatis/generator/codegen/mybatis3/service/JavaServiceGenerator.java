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
public class JavaServiceGenerator extends AbstractJavaGenerator {
    @Override
    public List<CompilationUnit> getCompilationUnits() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.20", //$NON-NLS-1$
                introspectedTable.getFullyQualifiedTable().toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();
        String fullQualifiedJavaType = context.getJavaControllerGeneratorConfiguration().getTargetPackage()
                .replaceAll("controller", "service");

//        FullyQualifiedJavaType type = new FullyQualifiedJavaType(
//                introspectedTable.getMyBatis3JavaMapperType());
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(
                fullQualifiedJavaType + "." + table.getDomainObjectName() + "Service");
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(interfaze);
        interfaze.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        interfaze.addImportedType(new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType()));
        FullyQualifiedJavaType baseType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        interfaze.addImportedType(baseType);
        interfaze.addImportedType(new FullyQualifiedJavaType("com.parsec.universal.dao.CommonDaoWrap"));
        interfaze.addImportedType(new FullyQualifiedJavaType("com.parsec.universal.utils.Result"));

        //get
        Method method = new Method();
        method.setReturnType(baseType);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("get");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "id"));
        interfaze.addMethod(method);

        //add
        method = new Method();
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("add");
        String validPropertyName = getValidPropertyName(baseType.getShortName());
        method.addParameter(new Parameter(baseType, getValidPropertyName(baseType.getShortName())));
        interfaze.addMethod(method);

        //update
        method = new Method();
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("update");
        method.addParameter(new Parameter(baseType, getValidPropertyName(baseType.getShortName())));
        interfaze.addMethod(method);

        //del
        method = new Method();
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("delete");
        method.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), "id"));
        interfaze.addMethod(method);

        //list
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(new FullyQualifiedJavaType("com.parsec.universal.utils.Result"));
        method.setName("list");
        method.addParameter(new Parameter(baseType, getValidPropertyName(baseType.getShortName())));

        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        listType.addTypeArgument(baseType);
        interfaze.addMethod(method);

        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
//        if (context.getPlugins().clientGenerated(interfaze, null,
//                introspectedTable)) {
        answer.add(interfaze);
//        }
        return answer;

    }
}
