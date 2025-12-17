package ht.util.json.keys;


import ht.util.core.ArrayUtil;
import ht.util.core.string.StringUtil;

/**
 *
 */
public class PropertyParts {
    private String[] parts;
    private String path;
    private boolean[] isNumber;

    public PropertyParts(String path) {
        setPath(path);
    }

    public boolean lastPartIndexed() {
        return isNumber[isNumber.length - 1];
    }

    public String getName() {
        if (lastPartIndexed()) {
            return parts[parts.length - 2];
        }
        return parts[parts.length - 1];
    }


    public int getIndex() {
        if (lastPartIndexed()) {
            return Integer.parseInt(parts[parts.length - 1]);
        }
        return -1;
    }

    private void computeParts(String path) throws PropertyException {
        if (!StringUtil.ensureBalance(path, '[', ']')) {
            throw new PropertyException("Unbalanced ");
        }
        int count = StringUtil.countInstances(path, '[');
        String p[] = path.split("\\.");
        if (ArrayUtil.nullOrEmpty(p)) {
            p = new String[1];
            p[0] = path;
        }
        int total = p.length + count;
        parts = new String[total];
        isNumber = new boolean[total];
        int ind = 0;
        for (int i = 0; i < p.length; i++) {
            int index = p[i].indexOf('[');
            if (index == -1) {
                parts[ind++] = p[i];
            } else {
                String s = p[i];
                parts[ind++] = s.substring(0, index);
                parts[ind] = s.substring(index + 1, s.length() - 1);
                isNumber[ind++] = true;
            }
        }
    }

    public String[] getParts() {
        return parts;
    }

    public boolean[] getNumber() {
        return isNumber;
    }

    public String getPath() {
        return path;
    }

    /**
     * Allow redefinition of the path
     *
     * @param path
     */
    public void setPath(String path) {
        computeParts(path);
        this.path = path;
    }
}
