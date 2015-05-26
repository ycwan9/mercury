package it.skarafaz.mercury.manager;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.skarafaz.mercury.data.Command;

public class SSHManager {
    private static final int TIMEOUT = 10000;
    private static final Logger logger = LoggerFactory.getLogger(SSHManager.class);
    private JSch jsch;
    private Session session;
    private String host;
    private Integer port;
    private String user;
    private String password;
    private String command;
    private Boolean sudo;


    public SSHManager(Command command) {
        jsch = new JSch();
        host = command.getServer().getHost();
        port = command.getServer().getPort();
        user = command.getServer().getUser();
        password = command.getServer().getPassword();
        this.command = command.getCmd();
        sudo = command.isSudo();
    }

    public boolean connect() {
        if (port == 0){
            return true;
        }
        boolean success = true;
        try {
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no"); // TODO known_hosts mng
            session.connect(TIMEOUT);
        } catch (JSchException e) {
            logger.error(e.getMessage().replace("\n", " "));
            success = false;
        }
        return success;
    }

    public boolean sendCommand() {
        boolean success = true;
        if (port){
            try {
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                if (sudo) {
                    channel.setCommand("echo " + password + " | sudo -S -p '' " + command);
                } else {
                    channel.setCommand(command);
                }
                channel.connect(TIMEOUT);
                channel.disconnect();
            } catch (JSchException e) {
                logger.error(e.getMessage().replace("\n", " "));
                success = false;
            }
        }
        return success;
    }

    public void disconnect() {
        if (port){
            session.disconnect();
        }
    }
}
