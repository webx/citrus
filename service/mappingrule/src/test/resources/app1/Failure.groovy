package app1;

import static com.alibaba.citrus.util.Assert.*;

import org.springframework.beans.factory.InitializingBean;

import com.alibaba.citrus.service.moduleloader.Module;

class Failure implements InitializingBean, Module {
    void afterPropertiesSet() {
        assertTrue(false);
    }
    
    void execute() {
    }
}
