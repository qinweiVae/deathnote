package com.qinwei.deathnote.context.annotation;

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
public class PropertyDescriptorService {

    private Worker male;

    private Map<String, Worker> workerMap;

    private Collection<Worker> workerCollection;

    private Worker[] workerArray;

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

    public void setMale(Worker male) {
        this.male = male;
    }

    public void setWorkerMap(Map<String, Worker> workerMap) {
        this.workerMap = workerMap;
    }

    public void setWorkerCollection(Collection<Worker> workerCollection) {
        this.workerCollection = workerCollection;
    }

    public void setWorkerArray(Worker[] workerArray) {
        this.workerArray = workerArray;
    }
}
