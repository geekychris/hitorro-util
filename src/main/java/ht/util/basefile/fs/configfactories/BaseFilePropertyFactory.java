package ht.util.basefile.fs.configfactories;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.BaseFileSystem;
import ht.util.basefile.fs.ProtocolAdapter;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.propaccess.PropaccessError;
import ht.util.propertykeys.complex.ComplexPropertiesException;
import ht.util.propertykeys.complex.ComplexPropertyContext;
import ht.util.propertykeys.complex.ComplexPropertyFactoryInterface;

import java.io.IOException;

/**
 * Handles constructing a file using the syntax:
 *
 * <protocol>://@@credskey@@/path
 * Where credskey is a the key "filesetmanagement.creds.<key>" that defines a connection in configs
 *
 * <protocol>://@@auth_parts@@/path
 * where auth_parts is a set of / delimited parts such as username, password
 */
public abstract class BaseFilePropertyFactory<C extends FileSystemConfig, F extends BaseFile>
        implements ComplexPropertyFactoryInterface<C>, ProtocolAdapter<F> {

    public BaseFile getBaseFileFromPath(String val) throws IOException {
        String parts[] = StringUtil.tokenizeFromMultiChar(val, "@@", false);
        if (parts == null || parts.length < 2) {
            return null;
        }
        String connect[] = StringUtil.tokenizeFromSingleChar(parts[1], "/");
        if (connect == null) {
            return null;
        }
        FileSystemConfig conf;
        if (connect.length != 1) {
            // we have a connection inline
            conf = getConfigFromParts(connect);
        } else {
            try {
                conf = ComplexPropertyContext.get(Fmt.S("filesetmanagement.creds.%s", connect[0]));
            } catch (ComplexPropertiesException e) {
                throw new IOException(Fmt.S("Unable to get file system config for %s", connect[0]));
            } catch (PropaccessError propaccessError) {
                throw new IOException(Fmt.S("Unable to get file system config for %s", connect[0]));
            }
        }

        BaseFileSystem prov = conf.getFileSystem();
        if (parts.length == 3) {
            return prov.getFile(parts[2]);
        }
        if (parts.length == 2) {
            return prov.getFile("");
        }
        return null;
    }

    public abstract FileSystemConfig getConfigFromParts(String parts[]);
}
