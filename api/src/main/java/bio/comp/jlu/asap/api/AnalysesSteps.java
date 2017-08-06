
package bio.comp.jlu.asap.api;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum AnalysesSteps {

    PIPELINE( "Pipeline", "pipeline", "-" ),
    PHYLOGENY( "Phylogeny", "phylogeny", "p" ),
    CORE_PAN( "Core/Pan Genome", "corepan", "c" );


    private final String name;
    private final String abbreviation;
    private final String charCode;

    AnalysesSteps( String name, String abbreviation, String charCode ) {

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
