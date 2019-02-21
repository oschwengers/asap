
package bio.comp.jlu.asap.api;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum GenomeSteps {

    PIPELINE( "Pipeline", "pipeline", "-" ),
    QC( "QC", "qc", "q" ),
    TAXONOMY( "Taxonomy", "taxonomy", "t" ),
    MAPPING( "Mapping", "mapping", "m" ),
    SNP_DETECTION( "SNP Detection", "snps", "s" ),
    ASSEMBLY( "Assembly", "assembly", "a" ),
    ABR( "Antibiotic Resistances", "abr", "r" ),
    VF( "Virulence Factors", "vf", "v" ),
    MLST( "MLST", "mlst", "l" ),
    SCAFFOLDING( "Scaffolding", "scaffolding", "f" ),
    ANNOTATION( "Annotation", "annotation", "n" );
//    PLASMIDS( "Plasmids", "plasmids", "p" ),
//    PHAGES( "Phages", "phages", "h" ),
//    CRISPRS( "CRISPRs", "crisprs", "c" );



    private final String name;
    private final String abbreviation;
    private final String charCode;

    GenomeSteps( String name, String abbreviation, String charCode ) {

        this.name = name;
        this.abbreviation = abbreviation;
        this.charCode = charCode;

    }


    public String getName() {

        return name;

    }


    public String getAbbreviation() {

        return abbreviation;

    }


    public String getCharCode() {

        return charCode;

    }


    @Override
    public String toString() {

        return name;

    }

}
