/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core;

import com.hitorro.util.log.Logger;

public class Log {
    public static final Logger util = Logger.getLogger("ht.util");
    public static final Logger servicecontext = Logger.getLogger("ht.servicecontext");
    public static final Logger ssh = Logger.getLogger("ht.ssh");
    public static final Logger exec = Logger.getLogger("com.hitorro.util.exec");
    public static final Logger commands = Logger.getLogger("ht.commands");
    public static final Logger hibernate = Logger.getLogger("ht.hibernate");
    public static final Logger dmssession = Logger.getLogger("ht.dmssession");
    public static final Logger test = Logger.getLogger("ht.test");
    public static final Logger rpc = Logger.getLogger("ht.rpc");
    public static final Logger resourcecache = Logger.getLogger("ht.resourcecache");
    public static final Logger spam = Logger.getLogger("ht.spam");
    public static final Logger queue = Logger.getLogger("ht.queue");
    public static final Logger httpfetcher = Logger.getLogger("ht.htmlfetcher");
    public static final Logger jdbc = Logger.getLogger("ht.jdbc");
    public static final Logger breadcrumb = Logger.getLogger("ht.breadcrumb");
    public static final Logger dbms = Logger.getLogger("ht.db");
    public static final Logger mail = Logger.getLogger("ht.mail");
    public static final Logger audit = Logger.getLogger("ht.audit");
    public static final Logger type = Logger.getLogger("ht.type");
    public static final Logger coordination = Logger.getLogger("ht.coordination");


    public static final Logger indexer = Logger.getLogger("ht.indexer");


    public static final Logger workflow = Logger.getLogger("ht.workflow");
    public static final Logger statemachine = Logger.getLogger("ht.statemachine");
    public static final Logger streamer = Logger.getLogger("ht.streamer");

    public static final Logger filesystem = Logger.getLogger("ht.filesystem");

    /**
     * Logger for scheduled jobs.
     */
    public static final Logger scheduledJobs = Logger.getLogger("ht.scheduledJobs");

    public static final Logger extractors = Logger.getLogger("ht.extractors");
    public static final Logger sentence = Logger.getLogger("ht.sentence");

    public static final Logger mstparser = Logger.getLogger("ht.mstparser");

    public static final Logger unitTime = Logger.getLogger("ht.unittime");

    public static final Logger docprocessor = Logger.getLogger("ht.docprocessor");
    public static final Logger docmap = Logger.getLogger("ht.docmap");
    public static final Logger idmanager = Logger.getLogger("ht.persistedbloomfilter");
    public static final Logger decisiontrees = Logger.getLogger("ht.decisiontrees");
    public static final Logger ftpfs = Logger.getLogger("ht.ftpfs");
    public static final Logger jmx = Logger.getLogger("ht.jmx");

    public static final Logger fetcher = Logger.getLogger("ht.fetcher");

    public static final Logger io = Logger.getLogger("ht.io");
    public static final Logger iterator = Logger.getLogger("ht.iterator");

    public static final Logger importer = Logger.getLogger("ht.importer");

    public static final Logger compression = Logger.getLogger("ht.compression");
    public static final Logger tapestry = Logger.getLogger("ht.tapestry");
    public static final Logger lang = Logger.getLogger("ht.lang");
    public static final Logger json = Logger.getLogger("ht.json");

    public static final Logger mongo = Logger.getLogger("ht.mongo");

    public static final Logger tld = Logger.getLogger("ht.tld");

    public static final Logger text = Logger.getLogger("ht.text");
}
