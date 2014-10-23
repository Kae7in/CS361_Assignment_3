import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class Steganography {
    public static void main(String[] args) throws Exception {

        // flag input
        boolean encode = false;
        boolean decode = false;

        // check flag to determine whether we're encoding or decoding
        if (args[0].equalsIgnoreCase("-E"))
            encode = true;
        else if (args[0].equalsIgnoreCase("-D"))
            decode = true;
 
        String inputImageName;
        String messageName;

        inputImageName = args[1];
        messageName = args[2];

        // image input
        File imageFile = new File(inputImageName);
        FileInputStream inputStream;

        if (imageFile.exists()) {
            inputStream = new FileInputStream(imageFile);
        } else {
            System.out.println("Image file (first file given) not found.");
            return;
        }

        // get stats on the image 
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(inputImageName));
        } catch (IOException e) {
            System.out.println("Image not found in try/catch. Should not be here.");
            return;
        }
        int height = img.getHeight();
        int width = img.getWidth();

        // 3 bytes for each pixel
        // TODO: for the sample picture, this number is 54 bytes short.
        long amountPixel = height * width;
        long imageNumBytes = amountPixel * 3; 


        // TODO: all of the available() methods are pretty pricey, even worse in a loop.
        // BUT, what if available space is great than an int can store? Maybe have a variable
        // limit = stream.available() and update it every 1000 loops or something and manually decrement it
        if (encode){
            
            // create a FileInputStream for the message we want to encode
            File messageToEncode = new File(messageName);
            FileInputStream messageStream;
            if (messageToEncode.exists()) {
                messageStream= new FileInputStream(messageToEncode);
            } else {
                System.out.println("Message to encode (second file given) not found.");
                return;
            }
            
            // create encoded image file name with "-steg" and new extension
            StringBuffer sb = new StringBuffer(inputImageName);
            sb.insert(sb.length() - 4, "-steg");
            String encodedImageName = sb.toString();

            // check if output image file already exist, delete it if it does exist
            File encodedImage = new File(encodedImageName);
            if (encodedImage.exists()){
                encodedImage.delete();
                encodedImage = new File(encodedImageName);
            }

            FileOutputStream writer = new FileOutputStream(encodedImage);

            long messageNumBytes = messageToEncode.length();


            // TODO: does he want us to replace a whole byte with '0' (uses one image pixel)
            // or does he want us to encode it like the message? (this uses 8 image pixels)

            // TODO: are we supposed to use getRBG? When you use that,
            //  the alpha part of the pixel starts mattering

            // Now it's time to encode the message.
            // We change the last bit of every image byte 
            //  to the bit we want to represent from the message
            // This method makes the least impact on the image.

            // TODO: the condition is + 1, assuming writing a whole byte as 0
            if (imageNumBytes >= messageNumBytes + 1) {
                // there is room for the whole message plus 
                //  a 0 byte to represent the end of the message

                while (messageStream.available() > 0) {
                    int messageByte = messageStream.read();

                    for (int i = 7; i > -1; i--) {
                        int messageBit = messageByte >>> i;
                        int imageByte = inputStream.read();

                        if (messageBit == 1) { 
                            // '|' so the one is always transfered
                            writer.write(imageByte | 1);
                            System.out.println( imageByte | 1);
                        } else {
                            // if 0, '&'' it with 11111110
                            writer.write( imageByte & 0xFE);
                            System.out.println(imageByte & 0xFE);
                        }
                        
                    }
                }

                // TODO: assuming we just write a whole byte as 0
                // writer.write(0);

                for (int i = 0; i < 8; i++) {
                    int imageByte = inputStream.read();
                    writer.write( imageByte & 0xFE);
                }
                // to skip over the replaced byte
                // inputStream.read();

                // write the rest of the bytes into the new image
                while (inputStream.available() > 0) {
                    writer.write(inputStream.read());
                }
                System.out.println("Message was small enough.");
            
            } else {

                // else the message is too big to fit into the image
                // so we fit as much as we can
                // TODO: this assuming that we replace a whole byte with 0
                int messageByte = 0;
                int i = 7;

                while (inputStream.available() - 1 > 0) {
                    if (messageByte != 0) {

                        int messageBit = messageByte >>> i;
                        int imageByte = inputStream.read();

                        if (messageBit == 1) { 
                            // '|' so the one is always transfered
                            writer.write(imageByte | 1);
                            System.out.println( imageByte | 1);
                        } else {
                            // if 0, '&'' it with 11111110
                            writer.write(imageByte & 0xFE);
                            System.out.println(imageByte & 0xFE);
                        }

                        i--;
                    } else {
                       messageByte = messageStream.read();
                       i = 7;
                    }
                    
                }

                writer.write(0);
                System.out.println("Message is biiiig.");

            }
            
            messageStream.close();
            writer.close();

        } else if (decode){
            File decodedMessageFile = null;
            //decode dis ish

            // check if output message file already exist
            FileWriter writer = null;
            decodedMessageFile = new File(messageName);
            if (decodedMessageFile.exists()){
                decodedMessageFile.delete();
                decodedMessageFile = new File(messageName);
            }
            writer = new FileWriter(decodedMessageFile);
        }


    // This prints the image height and width and a specific pixel. 
        inputStream.close();
        System.out.println(height  + "  " +  width);

    }
}