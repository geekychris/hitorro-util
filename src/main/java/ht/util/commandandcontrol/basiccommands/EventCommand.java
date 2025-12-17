package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.Command;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.RestOperations;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.core.events.LocalEventHub;
import ht.util.json.keys.StringProperty;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
 * <p/>
 * Send a simple event
 */
@CommandDef(command = "env.sendevent", description = "Send an event to anyone listening on the topic")
public class EventCommand extends Command {
    @CommandArgument(required = true)
    public static final StringProperty Topic = new StringProperty("topic", "event topic", null);
    @CommandArgument(required = true)
    public static final StringProperty SubTopic = new StringProperty("subtopic", "sub topic", null);

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        String topic = Topic.apply(args);
        String subtopic = SubTopic.apply(args);
        LocalEventHub.get().event(topic, subtopic, null);
        writeSuccess(response, "sent");
        return false;
    }
}
