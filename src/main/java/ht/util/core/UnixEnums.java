package ht.util.core;

/**
 *
 */
public class UnixEnums {

    public enum SystemResources {
        RLIMIT_CPU(0, "cpu time per process"),
        RLIMIT_FSIZE(1, "file size"),
        RLIMIT_DATA(2, "data segment size"),
        RLIMIT_STACK(3, "stack size"),
        RLIMIT_CORE(4, "core file size"),
        RLIMIT_AS(5, "address space (resident set size)"),
        RLIMIT_RSS(5, "source compatibility alias"),
        RLIMIT_MEMLOCK(6, "locked-in-memory address space"),
        RLIMIT_NPROC(7, "number of processes"),
        RLIMIT_NOFILE(8, "number of open files"),

        RLIM_NLIMITS(9, "total number of resource limits"),

        _RLIMIT_POSIX_FLAG(0x1000, "Set bit for strict POSIX");

        private int resourceOrd;
        private String description;

        SystemResources(int ordinal, String description) {
            resourceOrd = ordinal;
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public int getResourceOrdinal() {
            return resourceOrd;
        }
    }

    /*
     * Possible values of the first parameter to getrlimit()/setrlimit(), to
     * indicate for which resource the operation is being performed.
     */


}

