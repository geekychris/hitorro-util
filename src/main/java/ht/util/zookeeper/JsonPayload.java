package ht.util.zookeeper;

import ht.jsontypesystem.JVS;

public class JsonPayload {
    public String topic;
    public String subTopic;
    public String message;


    public JVS getAsJVS() {
        return JVS.read(message);
    }

    public void setJVS(JVS jvs) {
        message = jvs.getStringRepresentation();
    }
}
