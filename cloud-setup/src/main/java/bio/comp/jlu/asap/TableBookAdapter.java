
package bio.comp.jlu.asap;


import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;


public interface TableBookAdapter {


    public boolean acceptFile( File file );


    public FileNameExtensionFilter getFileExtensionFilter();


    public TableBook importTableBook( File file )
            throws IOException;


    public void exportTableBook( TableBook tableBook, File file )
            throws IOException;


}
