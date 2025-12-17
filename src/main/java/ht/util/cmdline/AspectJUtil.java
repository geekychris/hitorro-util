package ht.util.cmdline;

//import com.sun.tools.attach.VirtualMachine;

import ht.util.core.Console;
import org.aspectj.weaver.loadtime.Agent;

import java.lang.instrument.Instrumentation;

public class AspectJUtil {

    public static void setupAspectJ() {
        if (!isAspectJAgentLoaded()) {
            System.err.println("WARNING: AspectJ weaving agent not loaded");
        } else {
            Instrumentation i = getInstrumentation();
            Console.println("hello");
        }
    }

    public static boolean isAspectJAgentLoaded() {
        try {
            Agent.getInstrumentation();
        } catch (NoClassDefFoundError e) {
            System.out.println(e);
            return false;
        } catch (UnsupportedOperationException e) {
            System.out.println(e);
            //return dynamicallyLoadAspectJAgent();
        }
        return true;
    }

    public static Instrumentation getInstrumentation() {
        try {
            return Agent.getInstrumentation();
        } catch (NoClassDefFoundError e) {
            System.out.println(e);
        } catch (UnsupportedOperationException e) {
            System.out.println(e);
        }
        return null;
    }

    /*public static boolean dynamicallyLoadAspectJAgent ()
    {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        try
        {
            VirtualMachine vm = VirtualMachine.attach(pid);
            String jarFilePath = System.getProperty("AGENT_PATH");
            if (jarFilePath == null)
            {
                jarFilePath = getJarFilePath();
            }
            vm.loadAgent(jarFilePath);
            vm.detach();
        }
        catch (Exception e)
        {
            System.out.println(e);
            return false;
        }
        return true;
    }

    public static String getJarFilePath ()
    {
        ClassPathShallowIterator iter = new ClassPathShallowIterator();
        return iter.filter(new StringContainsOperator("aspectjweaver-1", true)).getFirstItem();
    }*/
}
