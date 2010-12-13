package app1.module.action.myprod;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.citrus.service.requestcontext.rundata.RunDataclass MyAction { 
    @Autowired
    RunData rundata;
    
    public void doSomething() throws Exception {
        rundata.setAttribute("handler", "doSomething");
    }
    
    public void doPerform() throws Exception {
        rundata.setAttribute("handler", "doPerform");
    }
    
    public void beforeExecution() {
        rundata.setAttribute("before", "yes");
    }

    public void afterExecution() {
        rundata.setAttribute("after", "yes");
    }
}
