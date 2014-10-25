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


        // get stats on the image 
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(inputImageName));
        } catch (IOException e) {
            System.out.println("Image given was not found (first argument).");
            return;
        }
        int height = img.getHeight();
        int width = img.getWidth();

        // 3 bytes for each pixel
        long pixelsInImage = height * width;
        long bytesInImage = pixelsInImage * 3; //this excludes alpha channel bytes 
        String imageType = inputImageName.substring(inputImageName.indexOf('.') + 1);

        System.out.println("Filename: " + inputImageName);
        System.out.printf("Image width: %d\n", width);
        System.out.printf("Image height: %d\n", height);
        System.out.printf("Number of pixels in file: %d\n", bytesInImage);

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

            // this creates a copy of the input message with the encoded name 
            // as the name.
            ImageIO.write(img, imageType, new File(encodedImageName));

            File encodedImageFile = new File(encodedImageName);
            BufferedImage encodedImage = ImageIO.read(encodedImageFile);

            long bitsInMessageLeft = messageToEncode.length() * 8;


            // Now it's time to encode the message.
            // We change the last bit of every image channel
            // to the bit we want to represent from the message.
            // This method makes the least impact on the image.

            int maxPossibleBitsLeft = (pixelsInImage * 3) - ((pixelsInImage * 3) % 8) //number of possible bits we can store to make all of them result in characters
            int [] zeroBits = [0, 0, 1, 1, 0, 0, 0, 0];
            // iterate over each pixel
            while (bitsInMessageLeft > 0) {
                int messageByte1 = messageStream.read();
                int messageByte2 = messageStream.read();
                int asciiCharacter = ((messageByte1 << 8) | messageByte2);
                int pixel = img.getRGB(i, j);
                int zeroBitIndex = 0;

                // ***LOTS OF FREAKING CODE HERE***
                if (maxPossibleBitsLeft > 0){
                    
                }
            }

            if (bitsInMessageLeft > 0){
                System.out.println("Not enough pixels to store message."
                System.out.println("Bits left to write: " + bitsInMessageLeft);
                System.out.println("Message truncated.");
            }
            
            messageStream.close();

        } else if (decode) {
            File decodedMessageFile = null;
            //decode dis ish

            // check if output message file already exist
            FileOutputStream writer = null;
            decodedMessageFile = new File(messageName);
            if (decodedMessageFile.exists()){
                decodedMessageFile.delete();
                decodedMessageFile = new File(messageName);
            }
            writer = new FileOutputStream(decodedMessageFile, true);


            int decodedChar = 0;
            int bitShift = 7;
            int imagePixel;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    imagePixel = img.getRGB(x, y);

                    for (int i = 2; i > -1; i--) {
                        if (bitShift == -1) {
                            
                            if (decodedChar == 0) {
                                // break out of the loop
                                writer.close();
                                return;
                            }

                            writer.write(decodedChar);
                            System.out.printf("%d ", decodedChar);
                            System.out.printf("%c ", decodedChar);
                            bitShift = 7;
                            decodedChar = 0;
                        }
                        decodedChar = decodedChar | (((imagePixel >> (i * 8)) & 1) << bitShift);
                        
                        bitShift--;
                    } 
                }
            }
        }
    }
}