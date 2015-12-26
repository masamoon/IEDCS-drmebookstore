package com.iedcs.player;

/**
 * Created by Andre on 23-12-2015.
 */

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import com.github.jaiimageio.jpeg2000.J2KImageReadParam;
import com.github.jaiimageio.jpeg2000.impl.J2KImageReader;
import pteidlib.PTEID_PIC;
import pteidlib.pteid;


public class ReadPic {


    public static void showPic() throws Exception{
        J2KImageReader j2KImageReader = (J2KImageReader) ImageIO
                .getImageReadersByFormatName(
                        "jpeg2000").next();

        PTEID_PIC picData = pteid.GetPic();
        if (null != picData)
        {
            try
            {
                String photo = "photo.jp2";
                FileOutputStream oFile = new FileOutputStream(photo);
                oFile.write(picData.picture);
                oFile.close();
                System.out.println("Created " + photo);
            }
            catch (FileNotFoundException excep)
            {
                System.out.println(excep.getMessage());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }


        try (ImageInputStream input = ImageIO.createImageInputStream(new File("photo.jp2"))) {

            j2KImageReader.setInput(input);


            ImageReadParam imageReadParam = j2KImageReader.getDefaultReadParam();
            imageReadParam.setSourceRegion(new Rectangle(0, 0, 400, 500));


            BufferedImage image = j2KImageReader.read(0, imageReadParam);


            Icon icon = new ImageIcon(image);
            JLabel label = new JLabel(icon);

            JFrame frame = new JFrame();

            frame.add(label);
            frame.setDefaultCloseOperation
                    (JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        }
    }
}
