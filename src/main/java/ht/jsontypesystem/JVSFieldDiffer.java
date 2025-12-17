package ht.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.diff.GenericDiffer;
import ht.util.core.diff.GenericDifferCallback;
import ht.util.json.keys.propaccess.Propaccess;
import ht.util.json.keys.propaccess.PropaccessComp;
import ht.util.json.keys.propaccess.PropaccessError;
import ht.util.json.keys.propaccess.PropaccessIterator;

public abstract class JVSFieldDiffer implements GenericDifferCallback<Propaccess> {
    private JVS jvs1, jvs2;

    public JVSFieldDiffer(BaseFile f1, BaseFile f2) throws Exception {
        jvs1 = JVS.read(f1);
        jvs2 = JVS.read(f2);
    }

    public JVSFieldDiffer(JVS jvs1, JVS jvs2) {
        this.jvs1 = jvs1;
        this.jvs2 = jvs2;
    }

    public void executeDiff() {
        GenericDiffer differ = new GenericDiffer();
        PropaccessIterator iter1 = jvs1.getPropertyIter();
        PropaccessIterator iter2 = jvs2.getPropertyIter();
        differ.diff(iter1, iter2, PropaccessComp.comp, this);
    }

    @Override
    public void call(final Propaccess a, final Propaccess b, final GenericDiffer.Mode mode) {
        if (mode == GenericDiffer.Mode.Modify) {
            try {
                JsonNode n1 = jvs1.get(a);
                JsonNode n2 = jvs2.get(b);
                if (!n1.equals(n2)) {
                    changed(a);
                }
            } catch (PropaccessError propaccessError) {
                //
            }


        } else if (mode == GenericDiffer.Mode.Add) {
            added(b);
        } else {
            removed(a);
        }
    }

    public abstract void added(Propaccess path);

    public abstract void removed(Propaccess path);

    public abstract void changed(Propaccess path);
}
