package com.martin;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

/**
 * <Description> <br>
 *
 * @author chen.tengfei <br>
 * @version 1.0<br>
 * @taskId <br>
 * @CreateDate 2017/8/31 <br>
 * @see com.martin <br>
 * @since R9.0<br>
 */
public class ConvertService {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationConfig.xml");

        Table2JavaBean table2JavaBean = context.getBean(Table2JavaBean.class);
        StringBuffer buffer = table2JavaBean.getClassStr("SUBS");
        System.out.println(buffer);
    }
}
