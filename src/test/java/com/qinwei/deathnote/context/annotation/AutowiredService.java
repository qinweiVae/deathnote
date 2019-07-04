package com.qinwei.deathnote.context.annotation;

import com.qinwei.deathnote.beans.bean.Domain1;
import com.qinwei.deathnote.beans.bean.Domain2;
import com.qinwei.deathnote.support.spi.Worker;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;

/**
 * @author qinwei
 * @date 2019-06-25
 */
@Component
@Slf4j
public class AutowiredService {

    @Autowired
    private Worker male;

    @Autowired
    private Map<String, Worker> workerMap;

    @Autowired
    private Collection<Worker> workerCollection;

    @Autowired
    private Worker[] workerArray;

    private Domain1 domain1;

    private Domain2 domain2;

    public void work() {
        log.info("----------");
        log.info("{}", male);
        male.work();
    }

    public void workMap() {
        log.info("----------");
        for (Map.Entry<String, Worker> entry : workerMap.entrySet()) {
            log.info(entry.getKey() + " : " + entry.getValue());
            entry.getValue().work();
        }
    }

    public void workCollection() {
        log.info("----------");
        for (Worker worker : workerCollection) {
            log.info("{}", worker);
            worker.work();
        }
    }

    public void workArray() {
        log.info("----------");
        for (Worker worker : workerArray) {
            log.info("{}", worker);
            worker.work();
        }
    }

    @Autowired
    public void populateField(Worker male, Domain1 domain1, Domain2 domain2, @Value("${author}") String author) {
        this.male = male;
        this.domain1 = domain1;
        this.domain2 = domain2;
        domain2.setBrand(author);
        log.info("{}", domain2);
    }
}
