package ht.util.core.thread.farm.jobfarm;

/**
 * Element that flows through the pipeline
 */
public class JobFarmElement<ELEMENT, RESULT, JOB> {
    private ELEMENT element;
    private RESULT result;
    private JobFarmEnqueueAgent agent;
    private JOB currentJob;

    public JobFarmElement(ELEMENT elem, JobFarmEnqueueAgent agent, JOB currentJob) {
        this.element = elem;
        this.agent = agent;
        this.currentJob = currentJob;
    }

    public RESULT getResult() {
        return result;
    }

    public void setResult(RESULT r) {
        result = r;
    }

    public ELEMENT getElement() {
        return element;
    }

    public void setElement(ELEMENT e) {
        element = e;
    }

    public JobFarmEnqueueAgent getAgent() {
        return agent;
    }

    public JOB getCurrentJob() {
        return currentJob;
    }
}
