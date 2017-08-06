
package bio.comp.jlu.asap.api;


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public enum DataType {

    READS,
    CONTIGS,
    GENOME;


    @Override
    public String toString() {

        return super.toString().toLowerCase();

    }

}
