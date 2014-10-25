import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class Steganography_jerry {
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
        long amountPixel = height * width;
        long imageNumBytes = amountPixel * 3; 
        String imageType = inputImageName.substring(inputImageName.indexOf('.') + 1);

        System.out.println("Filename: " + inputImageName);
        System.out.printf("Image width: %d\n", width);
        System.out.printf("Image height: %d\n", height);
        System.out.printf("Number of pixels in file: %d\n", amountPixel);

        if (encode) {
            
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

            long messageNumBits = messageToEncode.length() * 8;


            // Now it's time to encode the message.
            // We change the last bit of every image byte 
            //  to the bit we want to represent from the message
            // This method makes the least impact on the image.

            if (imageNumBytes >= messageNumBits + 8) {
                // there is room for the whole message plus 
                //  a 0 byte to represent the end of the message

                int x = 0;
                int y = 0;
                
                int newPixRGBPlace = 2; // iterates through RBG
                int messageBitsRemaining = 8;
                int messageByte = -1;
                int newPixel = encodedImage.getRGB(x, y);
                int shiftMsgBit = -1;


                while (messageStream.available() > 0) {
                    messageBitsRemaining = 8;
                    messageByte = messageStream.read();

                    while (messageBitsRemaining > 0) {
                        
                        // this means we have iterated through RGB
                        if (newPixRGBPlace == -1) {
                            encodedImage.setRGB(x, y, newPixel);
                            newPixRGBPlace = 2;

                            x++;
                            if (x >= width) {
                                x = 0;
                                y++;
                            }
                            newPixel = encodedImage.getRGB(x,y);
                        }

                        if (shiftMsgBit == -1) {
                            shiftMsgBit = 7;
                        }

                        int currentMessageBit = (messageByte >>> shiftMsgBit) & 1;

                        // these change the bit to 0 or 1, the position depending on newPixRBGPlace
                        if (currentMessageBit == 1) { 
                            // '|' so the one is always transfered
                            if (newPixRGBPlace == 2) {
                                newPixel = newPixel | (0x00010000);
                            } else if (newPixRGBPlace == 1) {
                                newPixel = newPixel | (0x00000100);
                            } else if (newPixRGBPlace == 0) {
                                newPixel = newPixel | (0x00000001);
                            }
                        } else {
                            // transfer all bits but always transfer a 0 depending on newPixRGBPlace
                            if (newPixRGBPlace == 2) {
                                newPixel = newPixel & (0xFFFEFFFF);
                            } else if (newPixRGBPlace == 1) {
                                newPixel = newPixel & (0xFFFFFEFF);
                            } else if (newPixRGBPlace == 0) {
                                newPixel = newPixel & (0xFFFFFFFE);
                            }

                        }

                        messageBitsRemaining--;
                        shiftMsgBit--;
                        newPixRGBPlace--;
                    }
                }


                // write the 0 byte in

                // this limit denotes the limit of pixels to write to
                int limit;

                // if 1, need to write 2 0's. this leaves 2 pixels for 0's
                if (newPixRGBPlace == 1) {
                    newPixel = newPixel & (0xFFFFFEFE);
                    limit = 2;
                } else if (newPixRGBPlace == 0){
                    // if 0, can only write 1 zero in the current pixel.
                    // set limit to 3 since we need 3 bytes to write 7 zeroes
                    newPixel = newPixel & (0xFFFFFFFE);
                    limit = 3;
                } else {// if (newPixRGBPlace == -1) {
                    limit = 3;
                }


                encodedImage.setRGB(x, y, newPixel);
                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }

                // since the encoded message was originally a copy of the input image,
                //  we don't need to copy over the other pixels.
                for (int i = 0; i < limit; i++) {

                    newPixel = encodedImage.getRGB(x, y) & (0xFFFEFEFE);
                    encodedImage.setRGB(x, y, newPixel);
                    x++;
                    if (x >= width) {
                        x = 0;
                        y++;
                    }
                }
            
            } else {

                // else the message is too big to fit into the image
                // so we fit as much as we can
                if (imageNumBytes < 6) {
                    System.out.println("Picture too small to encode a single character + a zero byte");
                    return;
                }


                int x = 0;
                int y = 0;
                
                int newPixRGBPlace = 2; // iterates through RBG
                int messageBitsRemaining = 8;
                int messageByte = -1;
                int newPixel = encodedImage.getRGB(x, y);
                int shiftMsgBit = -1;

                // leave room for 0 byte
                long limitOfBytes = imageNumBytes - 8;

                // need to do this so that a partial byte is not written in 
                // (a case is if we have 6 pixels, which is 18 bytes. That leaves 8 bits
                // for a character and 8 for a zero, which leaves 2 free bits. Those 2 bits
                // will screw everything up since it's not a full character.)
                while (limitOfBytes % 8 != 0) {
                    limitOfBytes--;
                }

                while (limitOfBytes > 0) {
                    messageBitsRemaining = 8;
                    messageByte = messageStream.read();

                    while (messageBitsRemaining > 0) {
                        
                        // this means we have iterated through RGB
                        if (newPixRGBPlace == -1) {
                            encodedImage.setRGB(x, y, newPixel);
                            newPixRGBPlace = 2;

                            x++;
                            if (x >= width) {
                                x = 0;
                                y++;
                            }

                            newPixel = encodedImage.getRGB(x,y);
                        }

                        if (shiftMsgBit == -1) {
                            shiftMsgBit = 7;
                        }

                        int currentMessageBit = (messageByte >>> shiftMsgBit) & 1;

                        // these change the bit to 0 or 1, the position depending on newPixRBGPlace
                        if (currentMessageBit == 1) { 
                            // '|' so the one is always transfered
                            if (newPixRGBPlace == 2) {
                                newPixel = newPixel | (0x00010000);
                            } else if (newPixRGBPlace == 1) {
                                newPixel = newPixel | (0x00000100);
                            } else if (newPixRGBPlace == 0) {
                                newPixel = newPixel | (0x00000001);
                            }
                        } else {
                            // transfer all bits but always transfer a 0 depending on newPixRGBPlace
                            if (newPixRGBPlace == 2) {
                                newPixel = newPixel & (0xFFFEFFFF);
                            } else if (newPixRGBPlace == 1) {
                                newPixel = newPixel & (0xFFFFFEFF);
                            } else if (newPixRGBPlace == 0) {
                                newPixel = newPixel & (0xFFFFFFFE);
                            }
                        }

                        messageBitsRemaining--;
                        shiftMsgBit--;
                        newPixRGBPlace--;
                        limitOfBytes--;
                    }
                }


                // write the 0 byte in

                // this limit denotes the limit of pixels to write to
                int limit;

                // if 1, need to write 2 0's. this leaves 2 pixels for 0's
                if (newPixRGBPlace == 1) {

                    newPixel = newPixel & (0xFFFFFEFE);
                    limit = 2;
                } else if (newPixRGBPlace == 0){
                    // if 0, can only write 1 zero in the current pixel.
                    // set limit to 3 since we need 3 bytes to write 7 zeroes

                    newPixel = newPixel & (0xFFFFFFFE);
                    limit = 3;
                } else {// if (newPixRGBPlace == -1) {
                    limit = 3;
                }


                encodedImage.setRGB(x, y, newPixel);
                x++;
                if (x >= width) {
                    x = 0;
                    y++;
                }

                // since the encoded message was originally a copy of the input image,
                //  we don't need to copy over the other pixels.
                for (int i = 0; i < limit; i++) {
                    
                    // newPixel = img.getRGB(x, y) & (0xFFFEFEFE);
                    newPixel = encodedImage.getRGB(x, y) & (0xFFFEFEFE);
                    encodedImage.setRGB(x, y, newPixel);
                    x++;
                    if (x >= width) {
                        x = 0;
                        y++;
                    }
                }
                System.out.println("Message too large for picture. Message was partially encoded.");

            }
            ImageIO.write(encodedImage, imageType, encodedImageFile);
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
            int bitShift = 7; // how many to shift over to build the byte
            int imagePixel;
            int temp;

            // iterate through all pixels
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {

                    imagePixel = img.getRGB(x, y);

                    // iterate through RGB
                    for (int i = 2; i > -1; i--) {
                        if (bitShift == -1) {

                            decodedChar = decodedChar & 0xFF;
                            if (decodedChar == 0) {
                                // if found, we are done
                                writer.close();
                                return;
                            }

                            writer.write(decodedChar);
                            bitShift = 7;
                            decodedChar = 0;
                        }
                        
                        if (i == 2) {
                            temp = imagePixel & 0x00010000;
                            temp = temp >> 16;
                            temp = temp & 1;
                            decodedChar = decodedChar | (temp << bitShift);
                        } else if (i == 1) {
                            temp = imagePixel & 0x00000100;
                            temp = temp >> 8;
                            temp = temp & 1;
                            decodedChar = decodedChar | (temp << bitShift);
                        } else {
                            temp = imagePixel & 0x00000001;
                            
                            decodedChar = decodedChar | (temp << bitShift);
                        }
                        bitShift--;
                    } 
                }
            }
        }
    }
}