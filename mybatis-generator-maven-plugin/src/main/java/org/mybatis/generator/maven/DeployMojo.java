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
import org.eclipse.sisu.Parameters;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wuhao on 23/03/2017.
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY)
public class DeployMojo extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "parsec.host", required = true)
    private String host;

    @Parameter(property = "parsec.server_path", required = true)
    private String serverPath;

    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        File file = new File("/tmp/com.parsec");
        if (!file.exists()) {
            file.mkdirs();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("server.sh")));
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file + "/server.sh")));
            String data;
            while ((data = reader.readLine()) != null) {
                bufferedWriter.write(data);
                bufferedWriter.newLine();
            }
            reader.close();
            bufferedWriter.close();

            reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("deploy.sh")));
            bufferedWriter = null;
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file + "/deploy.sh")));
            while ((data = reader.readLine()) != null) {
                bufferedWriter.write(data);
                bufferedWriter.newLine();
            }
            reader.close();
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            String target = new String("/bin/bash " + file + "/deploy.sh ${host} ${serverPath} ${basedir} ${cmd} "
                    .replaceAll("\\$\\{host}", host)
                    .replaceAll("\\$\\{serverPath}", serverPath)
                    .replaceAll("\\$\\{basedir}", project.getBasedir().getPath())
                    .replaceAll("\\$\\{cmd}", "deploy"));
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(target);
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = "";
            while (true) {
                while ((line = reader1.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        ProcessBuilder pb = new ProcessBuilder("/bin/bash a.sh", "myArg1", "myArg2");
//        Process p = Runtime.getRuntime().exec("ssh root@115.29.239.213 tail -f /mnt/springboot/gemini/nohup.out");
        Process p = Runtime.getRuntime().exec("/bin/bash" +
                " /Users/wuhao/IdeaProjects/generator-mybatis-generator-1.3.5/core/mybatis-generator-maven-plugin/src/main/resources/deploy.sh " +
                "root@115.29.239.213 /mnt/springboot/gemini " +
                "/Users/wuhao/IdeaProjects/generator-mybatis-generator-1.3.5/core/mybatis-generator-maven-plugin/ " +
                "tail");
//        pb.directory(new File("/Users/wuhao/IdeaProjects/generator-mybatis-generator-1.3.5/core/mybatis-generator-maven-plugin"));
        BufferedReader read = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        String line;
        while ((line = read.readLine()) != null) {
            System.out.println("line = " + line);

        }

    }


}
