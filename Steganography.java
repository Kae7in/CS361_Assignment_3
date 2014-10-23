import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.FileInputStream;

public class Steganography {
    public static void main(String[] args) {

        // flag input
        boolean encode = false;
        boolean decode = false;

        // check flag to determine whether we're encoding or decoding
        if (args[0].equals("-E"))
            encode = true;
        else if (args[0].equals("-D"))
            decode = true;
 
        String inputImageName;
        String messageName;

        inputImageName = args[1];
        messageName = args[2];

        // image input
        FileInputStream inputstream = new FileInputStream(new File(inputImageName));

        if (encode){
            // create encoded image file name with "-steg" and new extension
            StringBuffer sb = new StringBuffer(inputImageName);
            sb.insert(sb.length() - 4, "-steg");
            String encodedImageName = sb.toString();

            File encodedImage = null;
            //write dat message into this ish

            // check if output image file already exist
            FileWriter writer = null;
            encodedImage = new File(encodedImageName);
            if (encodedImage.exists()){
                encodedImage.delete();
                encodedImage = new File(encodedImageName);
            }
            writer = new FileWriter(encodedImage);
        }else if (decode){
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






        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("inputImage.bmp"));
        } catch (IOException e) {

        }
        int height = img.getHeight();
        int width = img.getWidth();

        int amountPixel = 0;

    // This prints the image height and width and a specific pixel. 

        System.out.println(height  + "  " +  width + " " + img.getRGB(30, 30));

    }
}