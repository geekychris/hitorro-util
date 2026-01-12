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
package com.hitorro.util.typesystem;

import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.annotation.FullTextAttributeMetaInfo;
import com.hitorro.util.typesystem.annotation.ImplClassMeta;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;
import com.hitorro.util.typesystem.listeners.BaseOnTriggerGeneric;

import jakarta.persistence.*;

import java.io.IOException;
import java.lang.ref.WeakReference;

@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.BaseType,
        onTriggers = {@ImplClassMeta(className = BaseOnTriggerGeneric.class, trigger = OnTrigger.TriggerType.OnLoad)},
        isView = false,
        isPersisted = false,
        schemaVersion = 1)

@MappedSuperclass
public abstract class BaseType<T extends BaseSession> implements HTSerializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "system_id")
    protected Long id;
    
    @Column(name = "stale")
    protected boolean stale = false;
    
    @Column(name = "commitCount")
    protected int commitCount = 0;
    
    // NEVER PERSIST THIS, THIS IS FOR session management
    @Transient
    protected transient WeakReference<T> session;
    
    // NOT TO BE PERSISTED
    @Transient
    protected long tempSerializationID;

    public int getCommitCount() {
        return commitCount;
    }

    public void incrementCommitCount() {
        commitCount++;
    }

    /**
     * Stale objects are ones retrieved and if your olding onto it you also probably have another reference that is
     * newer...you shouldnt be using me anymore!!!
     */
    public void setStale() {
        stale = true;
    }

    public boolean isConnectedToSession() {
        T sess = this.getSession();
        if (sess == null) {
            return false;
        }
        return sess.isObjectPartOfSession(this);
    }

    public boolean getStale() {
        return stale;
    }

    public boolean setSession(T session) {
        T old = getSession();
        if (old != null && old != session) {
            return false;
        }
        this.session = new WeakReference(session);
        return true;
    }

    /**
     * Get a session that was previously associated with this object.
     *
     * @return
     */
    public T getSession() {
        if (session == null) {
            return null;
        }
        return session.get();
    }

    public int hashCode() {
        Long id = getId();
        if (id != null) {
            return id.hashCode();
        }
        return super.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof BaseType) {
            Long id = getId();
            if (id != null) {
                Long otherId = ((BaseType) o).getId();

                if (otherId != null && id.equals(otherId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * subclass this to put your own cleanup logic
     *
     * @param session
     */
    public void delete(T session) {
        if (session != null) {
            session.delete(this);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getIdTempSerializationId() {
        return tempSerializationID;
    }

    public void setIdTempSerializationId(long id) {
        tempSerializationID = id;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        // we store the id, but will retrieve as m_tempSerializationID id
        os.writeLong(id);
    }

    public void deserialize(HTObjectInputStream os)
            throws IOException, ClassNotFoundException, StoreException {
        tempSerializationID = os.readLong();
    }

    public abstract int getSerializationVersion();

    public boolean isPersisted() {
        return true;
    }

    public boolean hasGuid() {
        return false;
    }

    public boolean hasSoftGuid() {            //org.w3c.dom.ElementTraversal
        return true;
    }

    /**
     * Guid that is really a symbolic reference for objects that support lookup by an alternative key.  For example,
     * User has a unique guid that is unique over all instances.  It also has
     *
     * @return
     */
    public String getSoftGuid() {
        return getSoftGuid(TypeManagerBase.get().getTypeForBaseType(this));
    }

    public String getSoftGuid(TypeIntf t) {
        return t.getSoftGuid(this);
    }

    /**
     * Case where we dont really have a classic guid
     *
     * @return
     */
    @FullTextAttributeMetaInfo(displayName = "guid",
            isFullTextIndexable = true, luceneIndexingFilters = "STANDARD,CASE,PORTERSTEM",
            luceneFieldName = "guid", stringLiteral = true, allField = false, stored = true)
    public String getGuid() {
        return getSoftGuid();
    }

    /**
     * Called by server startup if the schema version is found to be different
     *
     * @return false if failed.
     */
    public boolean upgradeAllInstances(long currentSchemaVersion) {
        return false;
    }
}
