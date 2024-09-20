package com.bda.common;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static com.bda.stock.WebCrawlerUtils.*;

/**
 * @author: anran.ma
 * @created: 2024/9/14
 * @description:
 **/
public class SFTPUtil {
    public static void createRemoteDir(Config config) {
        ChannelSftp sftp = null;
        Session session = null;
        String[] directories = config.getPath().split("/");
        String currentPath = "";
        try {
            // 配置 连接信息
            JSch jsch = new JSch();
            session = jsch.getSession(config.getUsername(), config.getHost(), config.getPort());
            session.setPassword(config.getPassword());
            // 配置 Session
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            sftp = (ChannelSftp) session.openChannel("sftp");
            // 连接
            sftp.connect();
            for (String dir : directories) {
                currentPath += "/" + dir;
                // 检查目录是否存在
                try {
                    sftp.cd(currentPath);
                } catch (SftpException e) {
                    // 如果目录不存在，创建它
                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        sftp.mkdir(currentPath);
                        System.out.println("目录创建成功: " + currentPath);
                    }
                }
            }
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            if (sftp != null && sftp.isConnected()) sftp.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    /**
     * @param localPath
     * @param config
     */
    public static void uploadFile(String localPath, Config config,boolean isReal,boolean isDelete) {
        ChannelSftp sftp = null;
        FileInputStream fis = null;
        Session session = null;
        try {
            // 配置 连接信息
            JSch jsch = new JSch();
            session = jsch.getSession(config.getUsername(), config.getHost());
            session.setPassword(config.getPassword());
            // 配置 Session
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect();
            // 列出远程目录中的所有文件
            List<String> dataFiles = new ArrayList<>();
            File dir = new File(localPath);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                for (File file : Objects.requireNonNull(files)) {
                    if (!file.getName().contains(HK_PREFIX) && !file.getName().contains(HSI_PREFIX) && !file.getName().contains(MAC_PREFIX))  continue;
                    if (!isReal && file.getName().startsWith(SECOND_PREFIX)) continue;
                    dataFiles.add(localPath + "/" + file.getName());
                }
            }
            for (String path : dataFiles) {
                File file = new File(path);
                if (!file.exists()) continue;
                // 读取本地文件
                fis = new FileInputStream(path);
                // 上传文件到远程服务器
                sftp.put(fis, config.getPath() + "/" + file.getName());
                System.out.println("文件上传成功: " + config.getPath() + "/" + file.getName());
                if (isDelete) file.delete();
                System.out.println("本地文件已删除: " + path);
            }
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sftp != null && sftp.isConnected()) sftp.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }
}
