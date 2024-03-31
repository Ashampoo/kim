import com.ashampoo.kim.Kim;
import com.ashampoo.kim.format.ImageMetadata;
import com.ashampoo.kim.input.ByteReader;
import com.ashampoo.kim.input.JvmInputStreamByteReader;
import com.ashampoo.kim.model.MetadataUpdate;
import com.ashampoo.kim.output.ByteArrayByteWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        File testFile = new File("testphoto.jpg");

        /*
         * You can also use Kim_jvmKt.readMetadata(Kim.INSTANCE, testFile);
         * which does exactly what the code below does under the hood.
         * Unfortunately the syntax is not so pretty as it is for Kotlin.
         */
        try (FileInputStream inputStream = new FileInputStream(testFile)) {

            ByteReader byteReader =
                new JvmInputStreamByteReader(inputStream, testFile.length());

            ImageMetadata imageMetadata = Kim.readMetadata(byteReader);

            System.out.println("---");
            System.out.println(imageMetadata);
            System.out.println("---");
        }

        try (FileInputStream inputStream = new FileInputStream(testFile)) {

            ByteReader byteReader =
                new JvmInputStreamByteReader(inputStream, testFile.length());

            ByteArrayByteWriter byteWriter = new ByteArrayByteWriter();

            MetadataUpdate update = new MetadataUpdate.TakenDate(System.currentTimeMillis());

            Kim.update(byteReader, byteWriter, update);

            byte[] updatesBytes = byteWriter.toByteArray();

            try (FileOutputStream fos = new FileOutputStream("testphoto_mod.jpg")) {

                fos.write(updatesBytes);
            }
        }
    }
}
