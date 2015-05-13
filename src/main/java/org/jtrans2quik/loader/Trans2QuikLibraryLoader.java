package org.jtrans2quik.loader;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import org.jtrans2quik.wrapper.Trans2QuikLibrary;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arsentii Nerushev on 14.05.2015.
 * @version 1.0.0
 */
public class Trans2QuikLibraryLoader {

    private static final Map NAME_MAPPING = new HashMap();
    public static final Trans2QuikLibrary LIBRARY;

    static {
        NAME_MAPPING.put(Library.OPTION_FUNCTION_MAPPER, StdCallLibrary.FUNCTION_MAPPER);
        NAME_MAPPING.put(Library.OPTION_CALLING_CONVENTION, Function.C_CONVENTION);
        System.setProperty("java.library.path", "lib/win32-x86");
        LIBRARY = (Trans2QuikLibrary) Native.loadLibrary(
                "TRANS2QUIK",
                Trans2QuikLibrary.class,
                NAME_MAPPING);
    }

    private Trans2QuikLibraryLoader() {}
}
