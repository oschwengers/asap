
package bio.comp.jlu.asap.api;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;



/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum ReferenceType {

    FASTA( "fasta", new String[]{ "fa", "fas", "fasta" } ),
    GENBANK( "genbank", new String[]{ "gb", "gbk", "gbff", "genbank" } ),
    EMBL( "embl", new String[]{ "el", "ebl", "embl" } );

    private final String type;
    private final String[] suffices;

    ReferenceType( String type, String[] suffices ) {

        this.type  = type;
        this.suffices = suffices;

    }


    public String getType() {

        return type;

    }


    public List<String> getSuffices() {

        return Collections.unmodifiableList( Arrays.asList( suffices ) );

    }


    @Override
    public String toString() {

        return type;

    }


    public static ReferenceType getEnum( String str ) {

        str = str.toLowerCase();
        if( str.contains( "." ) ) {
            str = str.substring( str.lastIndexOf( '.' ) + 1 );
        }

        for( ReferenceType rt : ReferenceType.values() ) {
            if( rt.getSuffices().contains( str ) )
                return rt;
        }

        return null;

    }

}
