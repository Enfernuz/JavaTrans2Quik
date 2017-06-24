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
 * @version 1.0.1
 */
public final class Trans2QuikLibraryLoader {

    public static final Trans2QuikLibrary LIBRARY;

    static {
        
        final Map<Object, Object> nameMapping = new HashMap<Object, Object>();
        nameMapping.put(Library.OPTION_FUNCTION_MAPPER, StdCallLibrary.FUNCTION_MAPPER);
        nameMapping.put(Library.OPTION_CALLING_CONVENTION, Function.C_CONVENTION);
        LIBRARY = (Trans2QuikLibrary) Native.loadLibrary(
                "TRANS2QUIK",
                Trans2QuikLibrary.class,
                nameMapping);
    }

    private Trans2QuikLibraryLoader() {
        throw new AssertionError("This should never be invoked.");
    }
}
