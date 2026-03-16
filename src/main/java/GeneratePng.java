import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class GeneratePng {
    public static void main(String[] args) throws Exception {
        BufferedImage img = new BufferedImage(300, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(52, 152, 219));
        g.fillRect(0, 0, 300, 150);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("MicroSpringBoot", 55, 70);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.drawString("Imagen PNG de prueba", 60, 100);
        g.dispose();
        new File("src/main/resources/public").mkdirs();
        ImageIO.write(img, "png", new File("src/main/resources/public/test.png"));
        System.out.println("test.png creado!");
    }
}

