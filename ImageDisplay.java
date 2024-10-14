
import java.awt.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;


public class ImageDisplay {

	JFrame frame;
	JLabel lbIm1;
	BufferedImage imgOne;

	// Modify the height and width values here to read and display an image with
  	// different dimensions. 
	

	// int width = 256;
	// int height = 170;

	// int width = 128;
	// int height = 128;
	

	/** Read Image RGB
	 *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
	 */
	private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
	{
		try
		{
			int frameLength = width*height*3;

			File file = new File(imgPath);
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			raf.seek(0);

			long len = frameLength;
			byte[] bytes = new byte[(int) len];

			raf.read(bytes);

			int ind = 0;
			for(int y = 0; y < height; y++)
			{
				for(int x = 0; x < width; x++)
				{
					byte a = 0;
					
					byte r = bytes[ind];
					byte g = bytes[ind+(height*width)];
					byte b = bytes[ind+(height*width*2)]; 

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					//int pix = 0xff32A852;
					img.setRGB(x,y,pix);
					ind++;
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}


	private void hsvSegmentation(int width, int height, int h1, int h2, BufferedImage imgIn, BufferedImage imgOut)
	{

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Get the color from the original image
				int color = imgIn.getRGB(x, y);
				// Components will be in the range of 0..255:
				byte r_B = (byte)((color & 0xff0000) >> 16);
				byte g_B = (byte)((color & 0xff00) >> 8);
				byte b_B = (byte)(color & 0xff);
				
				int r = (int)r_B;
				int g = (int)g_B;
				int b = (int)b_B;

				//Fixing the negative values, bytes being signed is causing problems in calculatiosn later on
				if(r < 0){
					r += 256;
				}
				if(g < 0){
					g += 256;
				}
				if(b < 0){
					b += 256;
				}
				
				// System.out.println("R: " + r + "G: " + g + "B: " + b);
				//RGB to HSV calculations based on formulas from https://mattlockyer.github.io/iat455/documents/rgb-hsv.pdf
				
				double r1 = r/255.0;
				double g1 = g/255.0;
				double b1 = b/255.0;
		
				double max = Math.max(r1, Math.max(g1, b1));
				double min = Math.min(r1, Math.min(g1, b1));
				
				double delta = max - min;
		
				double h = 0;
				if(delta == 0){
					h = 0;
				} else if(max == r1){
					h = 60 * (((g1 - b1)/delta) % 6);
				} else if(max == g1){
					h = 60 * (((b1 - r1)/delta) + 2);
				} else if(max == b1){
					h = 60 * (((r1 - g1)/delta) + 4);
				}
				
				double h_1 = h;

				// Make sure h is in the range of 0 to 360
				if(h_1 < 0){
					h_1 = h_1 + 360;
				}
				double v = max;
				double v_1 = v * 100;
		
				double s = 0;
				if(v == 0){
					s = 0;
				} else {
					s = delta / v;
				}
				double s_1 = s * 100;
				
				// System.out.println("H: " + h1 + "S: " + s1 + "V: " + v1);

				if((int)h_1 >= h1 && (int)h_1 <= h2){
					imgOut.setRGB(x, y, color);
				} else {
					// Grayscale based on luminoisty https://www.baeldung.com/cs/convert-rgb-to-grayscale
					// int gray = (int)(0.3*r + 0.59*g + 0.11*b);
					// int pix = (0xff000000 | (gray << 16) | (gray << 8) | gray);

					//Grayscale based on v value
					int gray = (int)(v_1 * 255 / 100);
					int pix = (0xff000000 | (gray << 16) | (gray << 8) | gray);


					// System.out.println("H: " + h_1 + "S: " + s_1 + "V: " + v_1);

					// double r_2 = 0;
					// double g_2 = 255;
					// double b_2 = 17;

					// int pix = ((((int)r_2) << 16) + (((int)g_2) << 8) + ((int)b_2));

					imgOut.setRGB(x, y, pix);
				}

			}
		}

	}


	private int getR(int color){
		return ((color & 0xff0000) >> 16);
	}
	private int getG(int color){
		return ((color & 0xff00) >> 8);
	}
	private int getB(int color){
		return (color & 0xff);
	}



	public static int clamp(int value, int min, int max) {
		return (int)Math.max(min, Math.min(max, value));
	}

	static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	   }


	public void showIms(String filePath, int width, int height, int outWidth, int outHeight, int h1, int h2 ) {

		// Read in the specified image
		BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		readImageRGB(width, height, filePath, res);


		BufferedImage imgSegOut = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		hsvSegmentation(width, height, h1, h2, res, imgSegOut);
		res = imgSegOut;


		// Use label to display the image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		// lbIm1 = new JLabel(new ImageIcon(imgOne));
		// lbIm1 = new JLabel(new ImageIcon(imgDS1Out));
		lbIm1 = new JLabel(new ImageIcon(res));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		frame.pack();
		frame.setVisible(true);

		File outputfile = new File("output.png");
		try {
			ImageIO.write(res, "png", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// Check if the correct number of arguments is passed
        if (args.length != 3) {
            System.out.println("Incorrect Param Length: See README for usage instructions.");

			// Output the number of arguments
			System.out.println(args.length);

			// Output the arguments
			for (String arg : args) {
				System.out.println(arg);
			}
            return;
        }

        // Parse the command-line arguments
        String filePath = args[0];
        int width = 512;
        int height = 512;
		int h1 = 0;
		int h2 = 0;

        try {
            h1 = Integer.parseInt(args[1]);
            h2 = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("Error: h1, h2 should be integers.");
            return;
        }

        // Output the parameters to verify
        System.out.println("File Path: " + filePath);
        System.out.println("Width: " + width);
        System.out.println("Height: " + height);
		System.out.println("h1: " + h1);
		System.out.println("h2: " + h2);

        // You can add your program logic here based on the parameters
		ImageDisplay ren = new ImageDisplay();
		int outWidth = 512;
		int outHeight = 512;

		ren.showIms(filePath, width, height, outWidth, outHeight, h1, h2);
		// double[][] m = new double[3][3];
		System.out.println();

	}

}
