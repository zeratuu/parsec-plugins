/**
 *   ~ Copyright (c) 2017. 秒差距科技
 */
package org.mybatis.generator.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wuhao on 23/03/2017.
 */
@Mojo(name = "project-generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ProjectGeneratorMojo extends AbstractMojo {
    /**
     * Maven Project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File pomFile = project.getFile();
        SAXReader saxReader = new SAXReader();
        Document document;
        try {

            document = saxReader.read(new FileInputStream(pomFile));
            Element rootEl = document.getRootElement();
            //先移除已有的
            rootEl.remove(rootEl.element("dependencies"));
            rootEl.remove(rootEl.element("parent"));
            rootEl.remove(rootEl.element("packaging"));
            rootEl.remove(rootEl.element("properties"));

            rootEl.addElement("packaging").setText("jar");

            Element parent = rootEl.addElement("parent");
            parent.addElement("groupId").setText("com.parsec");
            parent.addElement("artifactId").setText("universal");
            parent.addElement("version").setText("0.0.1-SNAPSHOT");

            Element dependencies = rootEl.addElement("dependencies");
            Element dependency = dependencies.addElement("dependency");
            dependency.addElement("groupId").setText("com.parsec");
            dependency.addElement("artifactId").setText("universal");
            dependency.addElement("version").setText("0.0.1-SNAPSHOT");

            Element properties = rootEl.addElement("properties");
            properties.addElement("project.build.sourceEncoding").setText("UTF-8");
            properties.addElement("project.reporting.outputEncoding").setText("UTF-8");
            properties.addElement("java.version").setText("1.8");
            properties.addElement("start-class").setText(project.getGroupId() + "."
                    + project.getArtifactId() + "." + lineToHump(project.getArtifactId()) + "Application");

            Element build = rootEl.element("build");
//            rootEl.add(build);
            rootEl.remove(build);
            rootEl.add(build);
            Element springBootPlugin = build.element("plugins").addElement("plugin");
            springBootPlugin.addElement("groupId").setText("org.springframework.boot");
            springBootPlugin.addElement("artifactId").setText("spring-boot-maven-plugin");
            springBootPlugin.addComment("<dependencies>\n" +
                    "                <dependency>\n" +
                    "                <groupId>org.springframework</groupId>\n" +
                    "                <artifactId>springloaded</artifactId>\n" +
                    "                <version>1.2.6.RELEASE</version>\n" +
                    "                </dependency>\n" +
                    "                </dependencies>");


            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter xmlWriter = new XMLWriter(new FileWriter(pomFile), format);
            xmlWriter.write(document);
            xmlWriter.close();
            File file = new File("src/main/resources");
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File("src/main/java/${groupId}/".replaceAll("\\$\\{groupId}", project.getGroupId().replaceAll("\\.", "/")) + project.getArtifactId());
            if (!file.exists()) {
                file.mkdirs();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("application.properties")));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("src/main/resources/application.properties")));
            String data;
            while ((data = reader.readLine()) != null) {
                bufferedWriter.write(data);
                bufferedWriter.newLine();
            }
            reader.close();
            bufferedWriter.close();

            reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("generatorConfig.jtemp")));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("src/main/resources/generatorConfig.xml")));
            while ((data = reader.readLine()) != null) {
                bufferedWriter.write(data.replaceAll("\\$\\{artifactId}", project.getArtifactId())
                        .replaceAll("\\$\\{groupId}", project.getGroupId()));
                bufferedWriter.newLine();
            }
            reader.close();
            bufferedWriter.close();

            reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("Application.jtemp")));
            String pathname = "src/main/java/${groupId}/".replaceAll("\\$\\{groupId}", project.getGroupId()).replaceAll("\\.", "/")
                    + project.getArtifactId() + "/" + lineToHump(project.getArtifactId()) + "Application.java";
            file = new File(pathname);
//            file = new File("src/main/java/com/parsec/${groupId}/${ProjectName}Application.java".
//                    replaceAll("\\$\\{ProjectName}", lineToHump(project.getArtifactId()))
//                    .replaceAll("\\$\\{groupId}", project.getGroupId()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file)));

            while ((data = reader.readLine()) != null) {
                bufferedWriter.write(
                        data.replaceAll("\\$\\{groupId}", project.getGroupId())
                                .replaceAll("\\$\\{artifactId}", project.getArtifactId())
                                .replaceAll("\\$\\{ProjectName}", lineToHump(project.getArtifactId())));
                bufferedWriter.newLine();
            }
            reader.close();
            bufferedWriter.close();


            reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("logback.jtemp")));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("src/main/resources/logback.xml")));
            while ((data = reader.readLine()) != null) {
                bufferedWriter.write(data.replaceAll("\\$\\{artifactId}", project.getArtifactId())
                        .replaceAll("\\$\\{groupId}", project.getGroupId()));
                bufferedWriter.newLine();
            }
            reader.close();
            bufferedWriter.close();


        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //下划线或横杠转驼峰,且首字母大写
    public static String lineToHump(String str) {
        Pattern linePattern = Pattern.compile("[_-](\\w)");
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        //首字母大写
        char[] cs = sb.toString().toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);
//        return sb.toString();
    }

}
