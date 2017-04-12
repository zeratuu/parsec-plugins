/**
 *   ~ Copyright (c) 2017. 秒差距科技
 */
package org.mybatis.generator.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.Scanner;

/**
 * Created by wuhao on 31/03/2017.
 */
@Mojo(name = "ops-helper")
public class OpsCmdMojo extends AbstractMojo {
    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;
    @Parameter(property = "parsec.host", required = true)
    private String host;

    @Parameter(property = "parsec.server_path", required = true)
    private String serverPath;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        File file = new File("/tmp/" + project.getGroupId());
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
        Scanner scanner = new Scanner(System.in);
        System.out.println("支持的指令：{log logPath/logfileName|start|stop|restart}");
        try {
            String target = new String("/bin/bash " + file + "/deploy.sh ${host} ${serverPath} ${basedir}  "
                    .replaceAll("\\$\\{host}", host)
                    .replaceAll("\\$\\{serverPath}", serverPath)
                    .replaceAll("\\$\\{basedir}", project.getBasedir().getPath())
                    /*.replaceAll("\\$\\{cmd}", "deploy")*/);
            while (true) {
                String s = scanner.nextLine();
                if (s.indexOf("log") > -1) {
                    s = s.trim();
                    String[] split = s.split(" ");
                    target += ("tail " + (split.length > 1 ? split[split.length - 1] : ""));
                    break;
                } else if (s.equals("start")) {
                    target += "start";
                    break;
                } else if (s.equals("stop")) {
                    target += "stop";
                    break;
                } else if (s.equals("restart")) {
                    target += "restart";
                    break;
                } else {
                    System.out.println("输入错误，请重新输入");
                }
            }
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
}
