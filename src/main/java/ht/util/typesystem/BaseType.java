package ht.util.typesystem;

import ht.util.io.StoreException;
import ht.util.typesystem.annotation.FullTextAttributeMetaInfo;
import ht.util.typesystem.annotation.ImplClassMeta;
import ht.util.typesystem.annotation.TypeClassMetaInfo;
import ht.util.typesystem.listeners.BaseOnTriggerGeneric;

import java.io.IOException;
import java.lang.ref.WeakReference;

@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.BaseType,
        onTriggers = {@ImplClassMeta(className = BaseOnTriggerGeneric.class, trigger = OnTrigger.TriggerType.OnLoad)},
        isView = false,
        isPersisted = false,
        schemaVersion = 1)

public abstract class BaseType<T extends BaseSession> implements HTSerializable {
    protected Long id;
    protected boolean stale = false;
    protected int commitCount = 0;
    // NEVER PERSIST THIS, THIS IS FOR session management
    protected transient WeakReference<T> session;
    // NOT TO BE PERSISTED
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
