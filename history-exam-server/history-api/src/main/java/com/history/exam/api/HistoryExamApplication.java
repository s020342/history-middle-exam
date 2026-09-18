package com.history.exam.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 历史中考训练小程序后端启动类
 * <p>单体聚合部署：扫描全部业务模块包，开启定时任务。</p>
 */
@SpringBootApplication(scanBasePackages = "com.history.exam")
@MapperScan("com.history.exam.**.mapper")
@EnableScheduling
public class HistoryExamApplication {

    /**
     * 应用启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(HistoryExamApplication.class, args);
    }
}
