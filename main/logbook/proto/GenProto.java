/**
 * 
 */
package logbook.proto;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import logbook.dto.BattleExDto;

/**
 * @author Nekopanda
 *
 */
public class GenProto {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        JavaToProto jtp = new JavaToProto();
        jtp.addClass(BattleExDto.class);
        jtp.setHeader("package logbook;\n" + "option java_package = \"logbook.proto\";\n\n");
        OutputStreamWriter fproto = new OutputStreamWriter(new FileOutputStream("logbook-ex.proto"), "MS932");
        OutputStreamWriter fcode = new OutputStreamWriter(new FileOutputStream("logbook-ex.code"), "MS932");
        String proto = jtp.toString();
        System.out.println(proto);
        fproto.write(proto);
        fproto.close();
        String java = jtp.getBuilderMethods();
        System.out.println(java);
        fcode.write(java);
        fcode.close();
    }

}
