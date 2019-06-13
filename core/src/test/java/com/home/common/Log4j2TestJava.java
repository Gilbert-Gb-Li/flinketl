package com.home.common;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

public class Log4j2TestJava {

    /** 获取log4j2配置文件，并创建slf4j对象 */
    public static void main(String[] args) {
        ConfigurationSource source;
        String relativePath = "conf/log4j2-prod.xml";
        File log4jFile = new File(relativePath);
        try {
            if (log4jFile.exists()) {
                source = new ConfigurationSource(new FileInputStream(log4jFile), log4jFile);
                Configurator.initialize(null, source);
                Logger logger = LoggerFactory.getLogger(Log4j2TestJava.class);
                logger.debug("common: this is a debug message!");
                logger.info("common: this is a info message!");
            } else {
                System.out.println("loginit failed");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

    }
}
