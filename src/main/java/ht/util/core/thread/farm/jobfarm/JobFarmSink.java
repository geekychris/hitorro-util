package ht.util.core.thread.farm.jobfarm;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.sinks.Sink;

import java.io.IOException;

/**
 * Responsible for filling a container with results if they are for this agent. Only places things in the container if
 * they are of the correct type.  This is a little naive at some level as you must expect a job to of completely drained
 * its content before moving onto the next job
 */
public class JobFarmSink<ELEMENT, RESULT, JOB> implements Sink<JobFarmElement<ELEMENT, RESULT, JOB>> {

    private JobResultContainer<RESULT> results;
    private JobFarmEnqueueAgent ea;
    private JOB job;

    public void setResultContainer(JobResultContainer<RESULT> results, JobFarmEnqueueAgent ea, JOB job) {
        this.results = results;
        this.ea = ea;
        this.job = job;
    }

    public boolean add(JobFarmElement<ELEMENT, RESULT, JOB> o) {
        if (o.getAgent() == ea && o.getCurrentJob() == job) {
            results.add(o.getResult());
        }
        return true;
    }

    @Override
    public boolean init(final JsonNode node) {
        return false;
    }

    @Override
    public boolean start() throws IOException {
        return false;
    }

    @Override
    public boolean stop() throws IOException {
        return false;
    }

    public void close() {
    }
}
