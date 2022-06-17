package com.lz.ht;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Administrator
 */
@EnableTransactionManagement
@MapperScan("com.lz.ht.dao")
@SpringBootApplication
public class  CbootApplication {

    public static void main(String[] args) {
        System.out.println("---------------start-----------------");
        System.out.println("---------------hot-fix2-----------------");
        System.out.println("---------------hot-fix-----------------");
        System.out.println("---------------master-fix-----------------");
        System.out.println("---------------master-push-----------------");
        System.out.println("---------------master-pull-----------------");
        System.out.println("---------------github-update-----------------");
        SpringApplication.run( CbootApplication.class, args);
        System.out.println("---------------end-----------------");
    }

}



