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
package com.hitorro.util.typesystem.annotation;

import com.hitorro.util.typesystem.valuesource.ValueSourceForClassStandardImpl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeClassMetaInfo {
    //****************** BASE (00 - 2F)********************************
    String BaseType = "00";
    String VersionableObject = "01";

    String Content = "02";
    String ContentType = "03";
    String ObjectVersions = "04";

    String Extension = "05";
    String NamedLong = "06";

    String Store = "07";
    String User = "08";
    String ContentTag = "09";
    String Permission = "0A";
    String Role = "0B";
    String Container = "10";


    // domain value classification mechanisms
    String DomainValue = "11";
    String DomainInfo = "12";
    String Category = "13";

    String PersistedSerializedObject = "14";

    String TypeMap = "15";

    String WorkFlowItem = "16";
    String WorkFlowItemEntry = "17";
    String OutstandingWorkflowItem = "18";

    String UserPreferences = "19";
    String UserMark = "1A";
    String Bag = "1B";
    String EnqueueElement = "1C";

    //******************** DMS BASE (30 - 4F) ******************************

    // dms base
    String Document = "30";
    String RssFeedIn = "33";
    String Post = "34";
    String Comment = "36";
    String Client = "37";
    String Forum = "39";
    String SubjectArea = "3B";

    String Show = "3f";
    String Reference = "40";
    String RssItemCacheProxy = "41";
    String ExternalContent = "43";

    //******************** App (50-6F)******************************

    String ScheduledJob = "50";
    String Index = "51";
    String Folder = "52";

    //********************DMS BASE (70-8F)******************************

    //********************RPC*******************************************

    String RPCRequest = "70";
    String StatusMessage = "72";
    String FileUpload = "73";

    String InstanceCapability = "74";
    String PortMap = "76";

    String QueueDefinition = "78";
    String NodeGroupMember = "79";
    String NodeGroup = "7a";

    //****************** TEXT ********************************************
    String TermTuple = "90";
    String TermTupleGroup = "91";

    String TermTupleSet = "92";

    String Link = "93";
    String LinkSet = "94";
    String IndexLink = "95";
    String PhraseElement = "96";
    //****************** Social Work *************************************

    String Conversation = "B1";
    String MemoryConversation = "B2";

    //****************** Other Applications *************************************
    String ScpFile = "D0";


    //***************** Features **************************************************
    String FeatureValue = "E0";
    String FeatureSetKey = "E1";
    String RealmId = "E2";


    String Max = "F0";

    /**
     * @return short type name
     */
    String shortTypeName();

    /**
     * listFiles of triggers that get
     *
     * @return Triggers registered with this class.
     */
    ImplClassMeta[] onTriggers() default {};

    AdapterClassMeta[] adapters() default {};

    /**
     * The version of the schema that this object represents.  This is a place holder for cases where we do schema
     * versioning and want something to auto-update the db schema.
     *
     * @return schema version
     */
    int schemaVersion() default 1;

    /**
     * Class is one that is persisted in hibernate.
     *
     * @return true if persisted in hibernate
     */
    boolean isPersisted();

    /**
     * Indicates that this class is really a view
     *
     * @return
     */
    boolean isView();

    boolean isDynamic() default false;

    String softLinkField() default "";

    Class guidAccessor() default Object.class;

    Class valueSourceClass() default ValueSourceForClassStandardImpl.class;

    boolean disableFastAccessors() default false;
}
