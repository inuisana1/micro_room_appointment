package cn.akira.util;

import cn.akira.pojo.User;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class FtpUtil {
    /**
     * 读取指定ftp上文本文件的内容
     *
     * @param ftpIp    ftp地址
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @param dirPath  文件所在目录(以"/"开头)
     * @param fileName 文件名
     * @return 文件内容
     * @throws IOException 万一出错了呢？
     */
    public static String readTxtFile(String ftpIp,
                                     int port, String username,
                                     String password,
                                     String dirPath,
                                     String fileName) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(ftpIp, port);     //连接ftp
        ftpClient.login(username, password);//登录ftp
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {//是否连接成功,成功true,失败false
            ftpClient.changeWorkingDirectory(dirPath);//找到指定目录
            ftpClient.enterLocalPassiveMode();
            InputStream inputStream = ftpClient.retrieveFileStream(fileName);//根据目录获取指定文件
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            StringBuilder stringBuilder = new StringBuilder(150);
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } else throw new IOException("ftp连接失败");
    }

    public static String getUserIconBase64(User user) throws SAXException, ParserConfigurationException, XPathExpressionException, IOException, TransformerException {
        String headIconBase64;
        if (user.getHdFileName() != null) {//如果用户设置过头像
            Map<String, Object> ftpMap = ConfigUtil.getHeadIconFtpInfo(); //用户头像的FTP信息
            headIconBase64 = FtpUtil.readTxtFile(//从FTP中获取用户头像信息
                    (String) ftpMap.get("hostname"),
                    (int) ftpMap.get("port"),
                    (String) ftpMap.get("username"),
                    (String) ftpMap.get("password"),
                    Integer.toString(user.getUserId()),
                    user.getHdFileName()
            );
        } else {//若用户未设置过头像，则使用默认头像
            headIconBase64 = ConfigUtil.getConfigTagValue("others", "defaultHeadIconBase64");
        }

        return headIconBase64;
    }
}
