package cn.bugstack.sdk.infrastructure.git;

import cn.bugstack.sdk.types.utils.RandomStringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @program: openai_code_review
 * @description:
 * @author: lcz
 * @create: 2026-03-18 17:34
 **/


public class GitCommand {
    private final Logger logger = LoggerFactory.getLogger(GitCommand.class);

    private final String gitHubReviewLogUri;
    private final String gitHubToken;
    private final String project;
    /*分支*/
    private final String branch;
    /*作者*/
    private final String author;
    /*提交信息*/
    private final String message;

    public GitCommand(String gitHubReviewLogUri, String gitHubToken, String project, String branch, String author, String message) {
        this.gitHubReviewLogUri = gitHubReviewLogUri;
        this.gitHubToken = gitHubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getGitHubReviewLogUri() {
        return gitHubReviewLogUri;
    }

    public String getGitHubToken() {
        return gitHubToken;
    }

    public String getProject() {
        return project;
    }

    public String getBranch() {
        return branch;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    /*检出*/
    public String diff() throws Exception {
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        logProcessBuilder.directory(new File("."));// 设置工作空间
        Process logProcess = logProcessBuilder.start();// 启动
        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream())); // 读取
        String latestCommitHash = logReader.readLine();
        logReader.close();
        logProcess.waitFor();

        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommitHash + "^", latestCommitHash);
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();

        StringBuilder diffCode = new StringBuilder();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        while ((line=diffReader.readLine())!=null){
            diffCode.append(line).append("\n");
        }
        diffReader.close();

        int exitCode = diffProcess.waitFor();
        if (exitCode!=0) {
            throw new RuntimeException("Failed to get diff, exit code:"+exitCode);
        }
        return diffCode.toString();
    }

    /*写入日志*/
    public String commitAndPush(String recommend) throws Exception {
        Git git = Git.cloneRepository()
                .setURI(gitHubReviewLogUri+".git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitHubToken,""))
                .call();

        // 创建分支
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/"+dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        // 创建文件
       String fileName =  project +"-"+branch+"-"+author+System.currentTimeMillis()+"-"+ RandomStringUtils.generateRandomString(4)+".md";
        File newFile = new File(dateFolder,fileName);
        try(FileWriter fileWriter = new FileWriter(newFile)){
            fileWriter.write(recommend);
        }

        // 提交内容
        git.add().addFilepattern(dateFolderName+"/"+fileName).call();
        git.commit().setMessage(message).call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitHubToken,"")).call();

        logger.info("openai-code-review git commit and push done! {}",fileName);
        return gitHubReviewLogUri+"/blob/master/"+dateFolderName+"/"+fileName;

    }


}
