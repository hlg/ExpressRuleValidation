package expressrules;

import org.antlr.v4.runtime.ANTLRInputStream;

import java.io.IOException;
import java.io.InputStream;

public class CaseInsensitiveANTLRInputStream extends ANTLRInputStream {

    public CaseInsensitiveANTLRInputStream(InputStream input) throws IOException {
        super(input) ;
    }

    public CaseInsensitiveANTLRInputStream(String input) {
        super(input);
    }

    @Override
    public int LA(int i) {
        int c = super.LA(i);
        if(c<0) return c;
        return Character.toLowerCase(c);
    }
}
